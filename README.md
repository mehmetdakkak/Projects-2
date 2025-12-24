<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?style=for-the-badge&logo=android" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?style=for-the-badge&logo=kotlin" alt="Language"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?style=for-the-badge&logo=jetpackcompose" alt="UI"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-orange?style=for-the-badge&logo=firebase" alt="Backend"/>
</p>

<h1 align="center">🚀 JoinUp</h1>

<p align="center">
  <b>Birlikte Oyna, Birlikte Kazan!</b>
</p>

<p align="center">
  <i>Spor etkinlikleri oluşturun, katılın ve yeni insanlarla tanışın!</i>
</p>

---

## 📱 Uygulama Hakkında

**JoinUp**, sporseverleri bir araya getiren modern bir sosyal ağ uygulamasıdır. Kullanıcılar spor etkinlikleri oluşturabilir, mevcut etkinliklere katılabilir, takımlar kurabilir ve spor toplulukları aracılığıyla iletişim kurabilirler.

## ✨ Özellikler

### 🏆 Etkinlik Yönetimi
- **Etkinlik Oluşturma**: Futbol, basketbol, tenis ve daha fazlası için etkinlik oluşturun
- **Katılım Sistemi**: Etkinliklere tek tıkla katılın
- **Konum Seçimi**: Google Maps entegrasyonu ile konum belirleyin
- **Tarih & Saat**: Kolay tarih/saat seçimi
- **Hatırlatıcılar**: Etkinlik öncesi otomatik bildirimler

### 🗺️ Harita Görünümü
- Yakınınızdaki etkinlikleri haritada görün
- Konum bazlı etkinlik keşfi
- Yol tarifi alma özelliği

### 👥 Takım Sistemi
- Takım oluşturma ve yönetimi
- Takım içi sohbet
- Etkinlik önerme ve oylama sistemi
- Üye davet etme

### 🌍 Topluluklar
- Spor kategorilerine göre topluluklar
- Topluluk sohbeti
- Üye listesi görüntüleme
- Bildirimleri sessize alma

### 📊 İstatistikler & Rozetler
- Katılınan etkinlik sayısı
- Oluşturulan etkinlik sayısı
- Spor dağılımı analizi
- Kazanılan rozetler (🌟 Yeni Üye, 🎯 İlk Etkinlik, 🔥 Spor Tutkunu, vb.)

### ⭐ Puanlama Sistemi
- Etkinlik sonrası katılımcıları puanlayın
- 5 yıldızlı değerlendirme sistemi
- Yorum ekleme imkanı
- Kullanıcı güvenilirlik puanı

### 👫 Arkadaşlık Sistemi
- Arkadaş istekleri gönderme/alma
- Arkadaş listesi yönetimi
- E-posta ile arkadaş arama

### 🎨 Tema Desteği
- Açık/Karanlık mod
- Gradient tasarımlar
- Modern Material 3 UI

---

## 🎮 Desteklenen Spor Kategorileri

| Kategori | Sporlar |
|----------|---------|
| ⚽ **Takım** | Futbol, Basketbol, Voleybol, Hentbol, Amerikan Futbolu, Rugby |
| 🎾 **Raket** | Tenis, Badminton, Masa Tenisi, Padel, Squash |
| 🌊 **Su** | Yüzme, Su Topu, Kürek, Sörf, Kano |
| 💪 **Fitness** | Fitness, Yoga, Pilates, Crossfit, Zumba, Jimnastik |
| 🥊 **Mücadele** | Boks, Kick Boks, Güreş, Judo, Karate, Taekwondo |
| 🌲 **Outdoor** | Koşu, Bisiklet, Dağcılık, Kamp, Kaykay, Paten, Okçuluk |
| 🎯 **Eğlence** | Bowling, Bilardo, Satranç, Dart, Langırt, Paintball |
| 🎮 **E-Spor** | Valorant, CS:GO, LoL, FIFA, Dota 2, Rocket League |

---

## 🛠️ Teknolojiler

### Frontend
- **Kotlin** - Modern Android geliştirme dili
- **Jetpack Compose** - Deklaratif UI framework
- **Material 3** - Google'ın en güncel tasarım sistemi
- **Compose Navigation** - Ekran geçişleri

### Backend & Veritabanı
- **Firebase Authentication** - Kullanıcı kimlik doğrulama
- **Cloud Firestore** - NoSQL veritabanı
- **Firebase Cloud Messaging** - Push bildirimleri

### Harita & Konum
- **Google Maps SDK** - Harita görünümü
- **Google Maps Compose** - Compose entegrasyonu
- **Geocoder** - Koordinat-adres dönüşümü
- **Fused Location Provider** - Konum servisleri

### Diğer
- **Coroutines** - Asenkron işlemler
- **DataStore** - Yerel veri depolama

---

## 📁 Proje Yapısı

```
com.joinup.app/
│
├── MainActivity.kt          # Ana aktivite ve tüm composable fonksiyonlar
│
├── 📱 Ekranlar
│   ├── SplashEkrani()       # Açılış animasyonu
│   ├── GirisEkrani()        # Giriş/Kayıt
│   ├── AnaEkran()           # Ana sayfa ve navigasyon
│   ├── KesifEkrani()        # Etkinlik keşfi
│   ├── TakvimEkrani()       # Katılınan etkinlikler
│   ├── TopluluklarEkrani()  # Topluluk listesi
│   ├── TakimimEkrani()      # Takım yönetimi
│   ├── HaritaEkrani()       # Harita görünümü
│   ├── ProfilEkrani()       # Profil düzenleme
│   ├── IstatistikEkrani()   # İstatistikler
│   └── ArkadaslarEkrani()   # Arkadaş yönetimi
│
├── 💬 Dialoglar
│   ├── EtkinlikOlusturDialog()
│   ├── ToplulukOlusturDialog()
│   ├── ToplulukDetayDialog()
│   ├── TakimOlusturDialog()
│   ├── TakimDetayDialog()
│   ├── TakimEtkinlikDialog()
│   ├── UyeEkleDialog()
│   ├── KonumSecici()
│   ├── SporSecici()
│   ├── PuanlamaDialog()
│   ├── ArkadasAraDialog()
│   └── KullaniciProfilDialog()
│
├── 🧩 Bileşenler
│   ├── GradientButton()     # Gradient butonlar
│   ├── GlowCard()           # Işıltılı kartlar
│   ├── AnimatedLogo()       # Animasyonlu logo
│   ├── AyarKarti()          # Ayar kartları
│   ├── AyarSwitch()         # Toggle switchler
│   ├── YildizPuanlama()     # Yıldız puanlama
│   ├── MacKarti()           # Etkinlik kartları
│   └── StatKart()           # İstatistik kartları
│
├── 📦 Veri Modelleri
│   ├── MacModel             # Etkinlik/Maç
│   ├── MesajModel           # Mesaj
│   ├── TakimModel           # Takım
│   ├── TakimEtkinlikModel   # Takım etkinliği
│   ├── ToplulukModel        # Topluluk
│   ├── KullaniciModel       # Kullanıcı
│   ├── PuanlamaModel        # Puanlama
│   ├── ArkadasIstegiModel   # Arkadaş isteği
│   └── SporKategorisi       # Spor kategorisi
│
└── 🎨 Tema & Stiller
    ├── AcikRenkler          # Açık tema
    ├── KoyuRenkler          # Koyu tema
    └── Gradients            # Gradient tanımları
```

---

## 🚀 Kurulum

### Gereksinimler
- Android Studio Hedgehog (2023.1.1) veya üzeri
- Kotlin 1.9+
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

### Adımlar

1. **Projeyi klonlayın**
```bash
git clone https://github.com/kullanici/joinup.git
cd joinup
```

2. **Firebase yapılandırması**
   - [Firebase Console](https://console.firebase.google.com)'da yeni proje oluşturun
   - Android uygulaması ekleyin
   - `google-services.json` dosyasını `app/` dizinine ekleyin
   - Authentication ve Firestore'u etkinleştirin

3. **Google Maps API**
   - [Google Cloud Console](https://console.cloud.google.com)'da Maps SDK'yı etkinleştirin
   - API anahtarını `AndroidManifest.xml`'e ekleyin:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_API_KEY"/>
   ```

4. **Projeyi derleyin**
```bash
./gradlew assembleDebug
```

---

## 📦 Bağımlılıklar

```kotlin
dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    
    // Google Maps
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
}
```

---

## 🔐 İzinler

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

---

## 📸 Ekran Görüntüleri

| Giriş | Keşfet | Harita |
|:---:|:---:|:---:|
| Animasyonlu giriş ekranı | Etkinlik keşfi | Konum bazlı görünüm |

| Topluluklar | Takımlar | Profil |
|:---:|:---:|:---:|
| Spor toplulukları | Takım yönetimi | Kullanıcı profili |

---

## 🎯 Gelecek Özellikler

- [ ] Anlık mesajlaşma (Firebase Realtime Database)
- [ ] Sesli/görüntülü arama
- [ ] Etkinlik fotoğrafları
- [ ] Turnuva organizasyonu
- [ ] Spor tesisi rezervasyonu
- [ ] Fitness takibi entegrasyonu
- [ ] Çoklu dil desteği

---

## 👨‍💻 Geliştirici

**Mehmet Dakkak**

[![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github)](https://github.com/mehmetdakkak)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0A66C2?style=for-the-badge&logo=linkedin)](https://linkedin.com/in/mehmetdakkak)

---

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır. Detaylar için [LICENSE](LICENSE) dosyasına bakınız.

---

<p align="center">
  <b>⭐ Bu projeyi beğendiyseniz yıldız vermeyi unutmayın! ⭐</b>
</p>

</p>
