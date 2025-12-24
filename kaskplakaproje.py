# -*- coding: utf-8 -*-

import cv2
import os
import time
import csv
import winsound
from datetime import datetime
from ultralytics import YOLO
import easyocr
import numpy as np
import threading
from queue import Queue
import torch

import matplotlib
matplotlib.use('Agg')
os.environ['YOLO_VERBOSE'] = 'False'

try:
    from database import VeritabaniYoneticisi
    DB_AKTIF = True
except ImportError:
    DB_AKTIF = False
    print("[!] Veritabani modulu bulunamadi, sadece CSV kayit yapilacak.")

class Ayarlar:
    
    MODEL_YOLU = r"C:\Users\MEHMET DAKKAK\Desktop\KaskPlakaProjesi\best.pt"
    MODEL_YOLU = r"C:\Users\MEHMET DAKKAK\Desktop\KaskPlakaProjesi\best.pt"
    MODEL_PLAKA_YOLU = r"C:\Users\MEHMET DAKKAK\Desktop\KaskPlakaProjesi\best_plaka.pt"
    MODEL_KASKSIZ_YOLU = r"C:\Users\MEHMET DAKKAK\Desktop\KaskPlakaProjesi\best_kasksiz.pt"
    GPU_KULLAN = None
    GUVEN_ESIGI = 0.60  # Yanlis tespitleri engellemek icin artirildi
    OCR_GUVEN_ESIGI = 0.3
    DEBUG_MODE = True  # Tum tespitleri konsolda goster
    
    GORUNTU_GENISLIK = 1280
    GORUNTU_YUKSEKLIK = 720
    IMGSZ = 640
    
    FRAME_SKIP = 2
    HALF_PRECISION = False  # Model uyumlulugu icin kapatildi
    MAX_DET = 20
    OCR_THREAD_ENABLED = True
    
    CEZA_BEKLEME_SURESI = 15
    UYARI_GOSTERIM_SURESI = 3
    MIN_PLAKA_KARAKTER = 5
    
    CEZA_KLASORU = "CezaMakbuzlari"
    LOG_DOSYASI = "ceza_kayitlari.csv"
    
    SES_AKTIF = True
    SES_FREKANSI = 1000
    SES_SURESI = 500
    
    SINIF_ISIMLERI = ['Helmet', 'License_Plate', 'No_Helmet']
    IZIN_VERILEN_KARAKTERLER = 'ABCDEFGHIJKLMNOPRSTUVYZ0123456789'
    
    @staticmethod
    def gpu_kontrol():
        if torch.cuda.is_available():
            gpu_adi = torch.cuda.get_device_name(0)
            gpu_bellek = torch.cuda.get_device_properties(0).total_memory / (1024**3)
            return True, f"{gpu_adi} ({gpu_bellek:.1f} GB)"
        return False, "GPU bulunamadi - CPU kullanilacak"

class IstatistikYoneticisi:
    def __init__(self):
        self.baslangic_zamani = time.time()
        self.toplam_tespit = 0
        self.kaskli_tespit = 0
        self.kasksiz_tespit = 0
        self.plaka_tespit = 0
        self.kesilen_ceza = 0
        self.fps_listesi = []
    
    def fps_guncelle(self, fps):
        self.fps_listesi.append(fps)
        if len(self.fps_listesi) > 30:
            self.fps_listesi.pop(0)
    
    @property
    def ortalama_fps(self):
        if not self.fps_listesi:
            return 0
        return sum(self.fps_listesi) / len(self.fps_listesi)
    
    @property
    def gecen_sure(self):
        return time.time() - self.baslangic_zamani

class LogYoneticisi:
    def __init__(self, dosya_adi):
        self.dosya_adi = dosya_adi
        self._dosya_hazirla()
    
    def _dosya_hazirla(self):
        if not os.path.exists(self.dosya_adi):
            with open(self.dosya_adi, 'w', newline='', encoding='utf-8') as f:
                writer = csv.writer(f)
                writer.writerow(['Tarih', 'Saat', 'Plaka', 'Ceza_Turu', 'Durum'])
    
    def kayit_ekle(self, plaka, ceza_turu="Kasksiz Surus"):
        now = datetime.now()
        with open(self.dosya_adi, 'a', newline='', encoding='utf-8') as f:
            writer = csv.writer(f)
            writer.writerow([
                now.strftime('%Y-%m-%d'),
                now.strftime('%H:%M:%S'),
                plaka,
                ceza_turu,
                'Beklemede'
            ])

class GoruntuIslemci:
    def __init__(self, ayarlar):
        self.ayarlar = ayarlar
    
    def plaka_on_isle_gelismis(self, plaka_goruntu):
        if plaka_goruntu is None or plaka_goruntu.size == 0:
            return []
        
        islenecekler = []
        h, w = plaka_goruntu.shape[:2]
        scale = max(2.0, 300 / w) if w < 300 else 1.5
        buyuk = cv2.resize(plaka_goruntu, None, fx=scale, fy=scale, interpolation=cv2.INTER_CUBIC)
        
        gray = cv2.cvtColor(buyuk, cv2.COLOR_BGR2GRAY)
        islenecekler.append(buyuk)
        islenecekler.append(cv2.cvtColor(gray, cv2.COLOR_GRAY2BGR))
        
        filtered = cv2.bilateralFilter(gray, 11, 17, 17)
        clahe = cv2.createCLAHE(clipLimit=3.0, tileGridSize=(8,8))
        clahe_img = clahe.apply(filtered)
        islenecekler.append(cv2.cvtColor(clahe_img, cv2.COLOR_GRAY2BGR))
        
        _, otsu = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
        islenecekler.append(cv2.cvtColor(otsu, cv2.COLOR_GRAY2BGR))
        
        return islenecekler
    
    def panel_ciz(self, img, istatistikler):
        overlay = img.copy()
        cv2.rectangle(overlay, (10, 10), (320, 200), (40, 40, 40), -1)
        cv2.addWeighted(overlay, 0.7, img, 0.3, 0, img)
        
        cv2.putText(img, "SISTEM DURUMU", (20, 35),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 255), 2)
        cv2.line(img, (20, 45), (300, 45), (0, 255, 255), 1)
        
        bilgiler = [
            (f"FPS: {istatistikler.ortalama_fps:.1f}", (0, 255, 0)),
            (f"Sure: {int(istatistikler.gecen_sure//60):02d}:{int(istatistikler.gecen_sure%60):02d}", (255, 255, 255)),
            (f"Kasksiz: {istatistikler.kasksiz_tespit}", (0, 0, 255)),
            (f"Kaskli: {istatistikler.kaskli_tespit}", (0, 255, 0)),
            (f"Plaka: {istatistikler.plaka_tespit}", (255, 200, 0)),
            (f"Ceza: {istatistikler.kesilen_ceza}", (0, 165, 255)),
        ]
        
        y = 70
        for bilgi, renk in bilgiler:
            cv2.putText(img, bilgi, (25, y), cv2.FONT_HERSHEY_SIMPLEX, 0.5, renk, 1)
            y += 22
        
        return img
    
    def uyari_goster(self, img, plaka_metni, kalan_sure):
        h, w = img.shape[:2]
        kalinlik = int(5 + (kalan_sure * 2))
        cv2.rectangle(img, (0, 0), (w, h), (0, 0, 255), kalinlik)
        
        overlay = img.copy()
        cv2.rectangle(overlay, (w//2 - 350, h//2 - 100), (w//2 + 350, h//2 + 100), (0, 0, 150), -1)
        cv2.addWeighted(overlay, 0.8, img, 0.2, 0, img)
        
        cv2.putText(img, "!!! CEZA KESILDI !!!", (w//2 - 200, h//2 - 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 1.2, (255, 255, 255), 3)
        cv2.putText(img, f"PLAKA: {plaka_metni}", (w//2 - 150, h//2 + 20),
                    cv2.FONT_HERSHEY_SIMPLEX, 1, (0, 255, 255), 2)
        
        return img

class SesYoneticisi:
    def __init__(self, ayarlar):
        self.ayarlar = ayarlar
    
    def uyari_cal(self):
        if self.ayarlar.SES_AKTIF:
            try:
                winsound.Beep(self.ayarlar.SES_FREKANSI, self.ayarlar.SES_SURESI)
            except:
                pass

class KaynakYoneticisi:
    def __init__(self):
        self.kaynak = None
        self.kaynak_tipi = None
    
    def kamera_ac(self, kamera_id=0, genislik=1280, yukseklik=720):
        self.kaynak = cv2.VideoCapture(kamera_id)
        self.kaynak.set(cv2.CAP_PROP_FRAME_WIDTH, genislik)
        self.kaynak.set(cv2.CAP_PROP_FRAME_HEIGHT, yukseklik)
        self.kaynak_tipi = 'kamera'
        return self.kaynak.isOpened()
    
    def video_ac(self, dosya_yolu):
        self.kaynak = cv2.VideoCapture(dosya_yolu)
        self.kaynak_tipi = 'video'
        return self.kaynak.isOpened()
    
    def kare_oku(self):
        if self.kaynak is None:
            return False, None
        return self.kaynak.read()
    
    def kapat(self):
        if self.kaynak is not None:
            self.kaynak.release()

class OCRThreadYoneticisi:
    def __init__(self, reader, goruntu_islemci, ayarlar):
        self.reader = reader
        self.goruntu_islemci = goruntu_islemci
        self.ayarlar = ayarlar
        self.kuyruk = Queue(maxsize=5)
        self.sonuc_kuyrugu = Queue()
        self.calistir = False
        self.thread = None
    
    def baslat(self):
        self.calistir = True
        self.thread = threading.Thread(target=self._islem_dongusu, daemon=True)
        self.thread.start()
    
    def durdur(self):
        self.calistir = False
        if self.thread:
            self.thread.join(timeout=1)
    
    def plaka_ekle(self, plaka_goruntu, koordinatlar):
        if not self.kuyruk.full():
            try:
                self.kuyruk.put_nowait((plaka_goruntu, koordinatlar))
            except:
                pass
    
    def sonuc_al(self):
        if not self.sonuc_kuyrugu.empty():
            try:
                return self.sonuc_kuyrugu.get_nowait()
            except:
                pass
        return None
    
    def _islem_dongusu(self):
        while self.calistir:
            try:
                plaka_goruntu, koordinatlar = self.kuyruk.get(timeout=0.1)
                sonuc = self._plaka_oku(plaka_goruntu)
                if sonuc:
                    self.sonuc_kuyrugu.put((sonuc, koordinatlar))
            except:
                continue
    
    def _plaka_oku(self, plaka_goruntu):
        islenecekler = self.goruntu_islemci.plaka_on_isle_gelismis(plaka_goruntu)
        
        en_iyi_sonuc = ""
        en_yuksek_guven = 0
        
        for img in islenecekler:
            try:
                sonuclar = self.reader.readtext(img, allowlist=self.ayarlar.IZIN_VERILEN_KARAKTERLER)
                for (_, metin, guven) in sonuclar:
                    metin = metin.upper().replace(" ", "").replace("-", "")
                    if guven > en_yuksek_guven and len(metin) >= self.ayarlar.MIN_PLAKA_KARAKTER:
                        en_yuksek_guven = guven
                        en_iyi_sonuc = metin
            except:
                continue
        
        if en_yuksek_guven >= self.ayarlar.OCR_GUVEN_ESIGI:
            return en_iyi_sonuc
        return None

class TrafikDenetlemeSistemi:
    def __init__(self):
        self.ayarlar = Ayarlar()
        self.istatistikler = IstatistikYoneticisi()
        self.log = LogYoneticisi(self.ayarlar.LOG_DOSYASI)
        self.goruntu = GoruntuIslemci(self.ayarlar)
        self.ses = SesYoneticisi(self.ayarlar)
        self.kaynak = KaynakYoneticisi()
        
        self.db = VeritabaniYoneticisi() if DB_AKTIF else None
        
        self.model = None
        self.model_plaka = None
        self.model_kasksiz = None
        self.reader = None
        self.ocr_thread = None
        
        self.ceza_kesilen_plakalar = {}
        self.duraklatildi = False
        self.tespit_edilen_plaka = ""
        self.son_uyari_zamani = 0
        
        self.son_kasksiz_frame = None
        self.son_kasksiz_koordinat = None
        
        self.gpu_aktif = False
        self.device = 'cpu'
        self.frame_sayac = 0
        self.son_tespitler = None
        
        self.pencere_adi = "AKILLI TRAFIK DENETLEME v2.0 [OPTIMIZE]"
        
        if not os.path.exists(self.ayarlar.CEZA_KLASORU):
            os.makedirs(self.ayarlar.CEZA_KLASORU)
    
    def basla(self, video_dosyasi=None):
        print("\n" + "="*60)
        print("   AKILLI TRAFIK DENETLEME SISTEMI v2.0")
        print("="*60 + "\n")
        
        self.gpu_aktif, gpu_bilgi = Ayarlar.gpu_kontrol()
        self.device = 'cuda' if self.gpu_aktif else 'cpu'
        print(f"[{'OK' if self.gpu_aktif else '!'}] {gpu_bilgi}")
        
        print("[*] AI Modeli yukleniyor...")
        if not os.path.exists(self.ayarlar.MODEL_YOLU):
            print(f"[!] Model dosyasi bulunamadi: {self.ayarlar.MODEL_YOLU}")
            print("[!] Lutfen best.pt dosyasini proje klasorune ekleyin.")
            return
        
        self.model = YOLO(self.ayarlar.MODEL_YOLU)
        if self.gpu_aktif and self.ayarlar.HALF_PRECISION:
            self.model.half()
        print("[OK] Ana model (Kask) yuklendi!")
        
        if os.path.exists(self.ayarlar.MODEL_PLAKA_YOLU):
            print("[*] Plaka modeli yukleniyor...")
            self.model_plaka = YOLO(self.ayarlar.MODEL_PLAKA_YOLU)
            if self.gpu_aktif and self.ayarlar.HALF_PRECISION:
                self.model_plaka.half()
            print("[OK] Plaka modeli yuklendi!")
        else:
            print("[I] Ayri plaka modeli bulunamadi (best_plaka.pt). Tespit ana model uzerinden yapilacak.")
        
        if os.path.exists(self.ayarlar.MODEL_KASKSIZ_YOLU):
            print("[*] Kasksiz surucu modeli yukleniyor (Ozellestirilmis)...")
            self.model_kasksiz = YOLO(self.ayarlar.MODEL_KASKSIZ_YOLU)
            if self.gpu_aktif and self.ayarlar.HALF_PRECISION:
                self.model_kasksiz.half()
            print("[OK] Kasksiz surucu modeli yuklendi!")
        else:
            print("[I] Ayri kasksiz surucu modeli bulunamadi (best_kasksiz.pt).")
        
        print("[*] OCR okuyucu baslatiliyor...")
        self.reader = easyocr.Reader(['en'], gpu=self.gpu_aktif)
        print("[OK] OCR hazir!")
        
        if self.ayarlar.OCR_THREAD_ENABLED:
            self.ocr_thread = OCRThreadYoneticisi(self.reader, self.goruntu, self.ayarlar)
            self.ocr_thread.baslat()
        
        if video_dosyasi:
            if not self.kaynak.video_ac(video_dosyasi):
                print("[!] Video dosyasi acilamadi!")
                return
        else:
            if not self.kaynak.kamera_ac():
                print("[!] Kamera acilamadi!")
                return
        
        print("[OK] Kaynak hazir!")
        print("\n" + "="*60)
        print("   SISTEM HAZIR - Q ile cikis yapabilirsiniz")
        print("="*60 + "\n")
        
        cv2.namedWindow(self.pencere_adi, cv2.WINDOW_NORMAL)
        cv2.resizeWindow(self.pencere_adi, self.ayarlar.GORUNTU_GENISLIK, self.ayarlar.GORUNTU_YUKSEKLIK)
        
        self._ana_dongu()
        
        if self.ocr_thread:
            self.ocr_thread.durdur()
        self.kaynak.kapat()
        cv2.destroyAllWindows()
        
        self._final_rapor()
    
    def _ana_dongu(self):
        onceki_zaman = time.time()
        
        while True:
            if self.duraklatildi:
                tus = cv2.waitKey(100) & 0xFF
                if tus == ord('p'):
                    self.duraklatildi = False
                elif tus == ord('q') or tus == 27:
                    break
                continue
            
            basarili, img = self.kaynak.kare_oku()
            if not basarili:
                break
            
            simdiki_zaman = time.time()
            fps = 1 / (simdiki_zaman - onceki_zaman + 0.001)
            onceki_zaman = simdiki_zaman
            self.istatistikler.fps_guncelle(fps)
            
            img = self._tespit_yap(img)
            
            if self.ocr_thread:
                sonuc = self.ocr_thread.sonuc_al()
                if sonuc:
                    plaka_metni, koordinatlar = sonuc
                    if plaka_metni and len(plaka_metni) >= self.ayarlar.MIN_PLAKA_KARAKTER:
                        self._ceza_kaydet(img, plaka_metni)
                        x1, y1, x2, y2 = koordinatlar
                        cv2.putText(img, plaka_metni, (x1, y2 + 25),
                                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)
            
            img = self.goruntu.panel_ciz(img, self.istatistikler)
            
            if time.time() - self.son_uyari_zamani < self.ayarlar.UYARI_GOSTERIM_SURESI:
                kalan = self.ayarlar.UYARI_GOSTERIM_SURESI - (time.time() - self.son_uyari_zamani)
                img = self.goruntu.uyari_goster(img, self.tespit_edilen_plaka, kalan)
            
            cv2.imshow(self.pencere_adi, img)
            
            tus = cv2.waitKey(1) & 0xFF
            if tus == ord('q') or tus == 27:
                break
            elif tus == ord('p'):
                self.duraklatildi = True
            
            try:
                if cv2.getWindowProperty(self.pencere_adi, cv2.WND_PROP_VISIBLE) < 1:
                    break
            except:
                break
    
    def _tespit_yap(self, img):
        self.frame_sayac += 1
        tespit_yap = (self.frame_sayac % self.ayarlar.FRAME_SKIP == 0)
        
        kasksiz_var = False
        plaka_var = False  # Plaka tespit edildi mi?
        plaka_koordinatlari = []
        
        if tespit_yap:
            results = self.model(
                img, stream=True, verbose=False, show=False,
                imgsz=self.ayarlar.IMGSZ, conf=self.ayarlar.GUVEN_ESIGI,
                device=self.device, half=self.gpu_aktif and self.ayarlar.HALF_PRECISION,
                max_det=self.ayarlar.MAX_DET
            )
            
            tespitler = []
            
            
            # --- ANA MODEL (KASK) TESPITLERI ---
            for r in results:
                for box in r.boxes:
                    x1, y1, x2, y2 = map(int, box.xyxy[0])
                    conf = float(box.conf[0])
                    cls = int(box.cls[0])
                    
                    # Model sinif isimlerini kontrol et (Hata onleyici)
                    if cls < len(self.model.names):
                        sinif_adi = self.model.names[cls]
                    else:
                        continue
                    
                    # Sinif eslestirme (Ingilizce/Turkce destekli)
                    sinif = None
                    sinif_low = sinif_adi.lower()
                    
                    if 'no' in sinif_low and ('helmet' in sinif_low or 'kask' in sinif_low):
                        sinif = 'No_Helmet'
                    elif 'helmet' in sinif_low or 'kask' in sinif_low:
                        sinif = 'Helmet'
                    elif 'plate' in sinif_low or 'plaka' in sinif_low:
                        sinif = 'License_Plate'
                    
                    if sinif:
                        self.istatistikler.toplam_tespit += 1
                        tespitler.append((x1, y1, x2, y2, conf, sinif))
                        
                        if sinif == 'No_Helmet':
                            kasksiz_var = True
                            self.istatistikler.kasksiz_tespit += 1
                        elif sinif == 'Helmet':
                            self.istatistikler.kaskli_tespit += 1
                        elif sinif == 'License_Plate':
                            plaka_var = True
                            self.istatistikler.plaka_tespit += 1
                            plaka_koordinatlari.append((x1, y1, x2, y2, conf))

            # --- PLAKA MODELI TESPITLERI (VARSA) ---
            if self.model_plaka:
                results_plaka = self.model_plaka(
                    img, stream=True, verbose=False, show=False,
                    imgsz=self.ayarlar.IMGSZ, conf=self.ayarlar.GUVEN_ESIGI,
                    device=self.device, half=self.gpu_aktif and self.ayarlar.HALF_PRECISION,
                    max_det=self.ayarlar.MAX_DET
                )
                
                for r in results_plaka:
                    for box in r.boxes:
                        x1, y1, x2, y2 = map(int, box.xyxy[0])
                        conf = float(box.conf[0])
                        # Plaka modeli oldugu icin her seyi plaka kabul et
                        # Ancak yine de sinifa bakalim
                        cls = int(box.cls[0])
                        sinif_adi = self.model_plaka.names[cls].lower()
                        
                        if 'plate' in sinif_adi or 'plaka' in sinif_adi or 'number' in sinif_adi:
                            plaka_var = True
                            self.istatistikler.plaka_tespit += 1
                            tespitler.append((x1, y1, x2, y2, conf, 'License_Plate'))
                            plaka_koordinatlari.append((x1, y1, x2, y2, conf))

                            plaka_koordinatlari.append((x1, y1, x2, y2, conf))

            # --- KASKSIZ SURUCU MODELI TESPITLERI (VARSA) ---
            if self.model_kasksiz:
                results_kasksiz = self.model_kasksiz(
                    img, stream=True, verbose=False, show=False,
                    imgsz=self.ayarlar.IMGSZ, conf=self.ayarlar.GUVEN_ESIGI,
                    device=self.device, half=self.gpu_aktif and self.ayarlar.HALF_PRECISION,
                    max_det=self.ayarlar.MAX_DET
                )
                
                for r in results_kasksiz:
                    for box in r.boxes:
                        x1, y1, x2, y2 = map(int, box.xyxy[0])
                        conf = float(box.conf[0])
                        cls = int(box.cls[0])
                        sinif_adi = self.model_kasksiz.names[cls].lower()
                        
                        # No Helmet, Without Helmet gibi siniflari yakala
                        if 'no' in sinif_adi or 'without' in sinif_adi or 'yok' in sinif_adi:
                            kasksiz_var = True
                            self.istatistikler.kasksiz_tespit += 1
                            tespitler.append((x1, y1, x2, y2, conf, 'No_Helmet'))
                        # Helmet sinifini yakala
                        elif 'helmet' in sinif_adi or 'kask' in sinif_adi:
                            self.istatistikler.kaskli_tespit += 1
                            tespitler.append((x1, y1, x2, y2, conf, 'Helmet'))

            self.son_tespitler = tespitler
        
        if self.son_tespitler:
            plaka_mevcut = any(s[5] == 'License_Plate' for s in self.son_tespitler)
            
            for (x1, y1, x2, y2, conf, sinif) in self.son_tespitler:
                if sinif == 'License_Plate':
                    renk = (255, 200, 0)
                    etiket = "Plaka"
                    cv2.rectangle(img, (x1, y1), (x2, y2), renk, 2)
                    cv2.putText(img, f'{etiket} {conf:.0%}', (x1, y1 - 10),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.6, renk, 2)
                
                elif sinif == 'No_Helmet':  # plaka_mevcut sarti kaldirildi
                    renk = (0, 0, 255)
                    etiket = "!!! KASKSIZ !!!"
                    cv2.rectangle(img, (x1, y1), (x2, y2), renk, 4)
                    cv2.putText(img, f'{etiket} {conf:.0%}', (x1, y1 - 10),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.6, renk, 2)
                
                elif sinif == 'Helmet':  # plaka_mevcut sarti kaldirildi
                    renk = (0, 255, 0)
                    etiket = "Kaskli"
                    cv2.rectangle(img, (x1, y1), (x2, y2), renk, 2)
                    cv2.putText(img, f'{etiket} {conf:.0%}', (x1, y1 - 10),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.6, renk, 2)
        
        if kasksiz_var and plaka_var and plaka_koordinatlari:
            self._ceza_isle(img, plaka_koordinatlari)
        
        return img
    
    def _ceza_isle(self, img, plaka_koordinatlari):
        for (x1, y1, x2, y2, conf) in plaka_koordinatlari:
            plaka_goruntu = img[y1:y2, x1:x2]
            if self.ocr_thread:
                self.ocr_thread.plaka_ekle(plaka_goruntu, (x1, y1, x2, y2))
    
    def _ceza_kaydet(self, img, plaka_metni):
        simdi = time.time()
        
        if plaka_metni in self.ceza_kesilen_plakalar:
            if simdi - self.ceza_kesilen_plakalar[plaka_metni] < self.ayarlar.CEZA_BEKLEME_SURESI:
                return
        
        self.ceza_kesilen_plakalar[plaka_metni] = simdi
        self.istatistikler.kesilen_ceza += 1
        self.tespit_edilen_plaka = plaka_metni
        self.son_uyari_zamani = simdi
        
        self.log.kayit_ekle(plaka_metni)
        
        zaman_damgasi = datetime.now().strftime("%Y%m%d_%H%M%S")
        dosya_adi = f"{plaka_metni}_{zaman_damgasi}.jpg"
        dosya_yolu = f"{self.ayarlar.CEZA_KLASORU}/{dosya_adi}"
        
        kanit_img = img.copy()
        h, w = kanit_img.shape[:2]
        
        cv2.rectangle(kanit_img, (0, 0), (w, 60), (0, 0, 100), -1)
        cv2.putText(kanit_img, "TRAFIK IHLALI - KASK KULLANMAMA", (10, 25),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)
        cv2.putText(kanit_img, f"Tarih: {datetime.now().strftime('%d.%m.%Y %H:%M:%S')}", (10, 50),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)
        
        cv2.rectangle(kanit_img, (0, h-50), (w, h), (0, 0, 100), -1)
        cv2.putText(kanit_img, f"PLAKA: {plaka_metni}", (10, h-20),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 255), 2)
        cv2.putText(kanit_img, "CEZA: 1.151 TL", (w-200, h-20),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 165, 255), 2)
        
        cv2.imwrite(dosya_yolu, kanit_img)
        
        if self.db:
            try:
                self.db.ceza_ekle(
                    plaka=plaka_metni,
                    ceza_turu="Kasksiz Surus",
                    tutar=1151.0,
                    kanit_dosyasi=dosya_adi
                )
                print(f"[CEZA] Plaka: {plaka_metni} | Kanit: {dosya_adi} | DB: OK")
            except Exception as e:
                print(f"[CEZA] Plaka: {plaka_metni} | Kanit: {dosya_adi} | DB Hata: {e}")
        else:
            print(f"[CEZA] Plaka: {plaka_metni} | Kanit: {dosya_adi}")
        
        self.ses.uyari_cal()
    
    def _final_rapor(self):
        print("\n" + "="*60)
        print("   OTURUM RAPORU")
        print("="*60)
        print(f"   Toplam Sure: {int(self.istatistikler.gecen_sure//60)} dk")
        print(f"   Kesilen Ceza: {self.istatistikler.kesilen_ceza}")
        print("="*60 + "\n")

if __name__ == "__main__":
    import sys
    sistem = TrafikDenetlemeSistemi()
    if len(sys.argv) > 1:
        sistem.basla(sys.argv[1])
    else:
        sistem.basla()
