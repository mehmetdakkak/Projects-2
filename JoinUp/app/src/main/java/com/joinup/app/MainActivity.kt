package com.joinup.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale


val PrimaryGradient = Brush.linearGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))
val SecondaryGradient = Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D)))
val AccentGradient = Brush.linearGradient(listOf(Color(0xFFFC466B), Color(0xFF3F5EFB)))
val SunsetGradient = Brush.linearGradient(listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D)))
val OceanGradient = Brush.linearGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED)))
val PurpleGradient = Brush.linearGradient(listOf(Color(0xFFB721FF), Color(0xFF21D4FD)))
val GoldGradient = Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500)))

val AcikRenkler = lightColorScheme(
    primary = Color(0xFF667EEA),
    onPrimary = Color.White,
    secondary = Color(0xFF11998E),
    tertiary = Color(0xFFFC466B),
    background = Color(0xFFF0F4FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EEFF),
    onBackground = Color(0xFF1A1F36),
    onSurface = Color(0xFF1A1F36)
)

val KoyuRenkler = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color.White,
    secondary = Color(0xFF34D399),
    tertiary = Color(0xFFF472B6),
    background = Color(0xFF0F0F23),
    surface = Color(0xFF1A1A2E),
    surfaceVariant = Color(0xFF252542),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC)
)


val DefaultShape = RoundedCornerShape(16.dp)
val CardShape = RoundedCornerShape(28.dp)
val VARSAYILAN_KONUM = LatLng(41.0082, 28.9784)


data class MacModel(val id: String = "", val macAdi: String = "", val sporTuru: String = "", val konum: String = "", val tarihSaat: String = "", val oyuncuSayisi: Int = 0, val sahipId: String = "", val latitude: Double = 0.0, val longitude: Double = 0.0, val katilimcilar: List<String> = emptyList())
data class MesajModel(val id: String = "", val gonderenId: String = "", val gonderenIsim: String = "", val mesaj: String = "", val tarih: Long = 0)
data class TakimModel(val id: String = "", val takimAdi: String = "", val olusturanId: String = "", val uyeler: List<String> = emptyList())
data class TakimEtkinlikModel(val id: String = "", val takimId: String = "", val baslik: String = "", val tarihSaat: String = "", val onaylayanlar: List<String> = emptyList(), val reddedenler: List<String> = emptyList())
data class ToplulukModel(val id: String = "", val ad: String = "", val kategori: String = "", val olusturanId: String = "", val uyeler: List<String> = emptyList())
data class SporKategorisi(val ad: String, val emoji: String, val renk: Brush, val sporlar: List<String>)


data class KullaniciModel(val id: String = "", val isim: String = "", val email: String = "", val profilFotoUrl: String = "", val puan: Double = 0.0, val puanSayisi: Int = 0, val arkadaslar: List<String> = emptyList(), val katilinanEtkinlikler: Int = 0, val rozetler: List<String> = emptyList())
data class PuanlamaModel(val id: String = "", val puanlayanId: String = "", val puanlananId: String = "", val etkinlikId: String = "", val puan: Int = 0, val yorum: String = "", val tarih: Long = 0)
data class ArkadasIstegiModel(val id: String = "", val gonderen: String = "", val alan: String = "", val durum: String = "bekliyor", val tarih: Long = 0)


val sporKategorileri = listOf(
    SporKategorisi("Takım", "⚽", SecondaryGradient, listOf("Futbol", "Basketbol", "Voleybol", "Hentbol", "Amerikan Futbolu", "Rugby")),
    SporKategorisi("Raket", "🎾", SunsetGradient, listOf("Tenis", "Badminton", "Masa Tenisi", "Padel", "Squash")),
    SporKategorisi("Su", "🌊", OceanGradient, listOf("Yüzme", "Su Topu", "Kürek", "Sörf", "Kano")),
    SporKategorisi("Fitness", "💪", PurpleGradient, listOf("Fitness", "Yoga", "Pilates", "Crossfit", "Zumba", "Jimnastik")),
    SporKategorisi("Mücadele", "🥊", AccentGradient, listOf("Boks", "Kick Boks", "Güreş", "Judo", "Karate", "Taekwondo")),
    SporKategorisi("Outdoor", "🌲", SecondaryGradient, listOf("Koşu", "Bisiklet", "Dağcılık", "Kamp", "Kaykay", "Paten", "Okçuluk")),
    SporKategorisi("Eğlence", "🎯", GoldGradient, listOf("Bowling", "Bilardo", "Satranç", "Dart", "Langırt", "Paintball")),
    SporKategorisi("E-Spor", "🎮", PrimaryGradient, listOf("Valorant", "CS:GO", "LoL", "FIFA", "Dota 2", "Rocket League"))
)

val rozetListesi = mapOf(
    "yeni_uye" to Pair("🌟", "Yeni Üye"),
    "ilk_etkinlik" to Pair("🎯", "İlk Etkinlik"),
    "spor_tutkunu" to Pair("🔥", "Spor Tutkunu"),
    "sosyal_kelebek" to Pair("🦋", "Sosyal Kelebek"),
    "takim_kaptani" to Pair("👑", "Takım Kaptanı"),
    "super_star" to Pair("⭐", "Süper Star")
)


fun sporEmoji(s: String) = when(s.lowercase(Locale("tr"))) {

    "futbol" -> "⚽"; "basketbol" -> "🏀"; "voleybol" -> "🏐"; "hentbol" -> "🤾"; "amerikan futbolu" -> "🏈"; "rugby" -> "🏉"

    "tenis" -> "🎾"; "badminton" -> "🏸"; "masa tenisi" -> "🏓"; "padel" -> "🎾"; "squash" -> "⚫"

    "yüzme" -> "🏊"; "su topu" -> "🤽"; "kürek" -> "🚣"; "sörf" -> "🏄"; "kano" -> "🛶"

    "fitness" -> "🏋️"; "yoga" -> "🧘"; "pilates" -> "🧘‍♀️"; "crossfit" -> "💪"; "zumba" -> "💃"; "jimnastik" -> "🤸"

    "boks" -> "🥊"; "kick boks" -> "🦵"; "güreş" -> "🤼"; "judo" -> "🥋"; "karate" -> "🥋"; "taekwondo" -> "🥋"

    "koşu" -> "🏃"; "bisiklet" -> "🚴"; "dağcılık" -> "🧗"; "kamp" -> "⛺"; "kaykay" -> "🛹"; "paten" -> "🛼"; "okçuluk" -> "🏹"

    "bowling" -> "🎳"; "bilardo" -> "🎱"; "satranç" -> "♟️"; "dart" -> "🎯"; "langırt" -> "⚽"; "paintball" -> "🔫"

    "valorant" -> "🎯"; "cs:go" -> "🔫"; "lol" -> "⚔️"; "fifa" -> "⚽"; "dota 2" -> "🛡️"; "rocket league" -> "🚗"
    else -> "🎮"
}

fun sporRengi(s: String): Brush {
    val spor = s.lowercase(Locale("tr"))
    return when {

        listOf("yüzme", "su topu", "kürek", "sörf", "kano").contains(spor) -> OceanGradient

        listOf("boks", "kick boks", "güreş", "judo", "karate", "taekwondo", "crossfit").contains(spor) -> AccentGradient

        listOf("tenis", "badminton", "masa tenisi", "padel", "bowling", "bilardo", "dart").contains(spor) -> SunsetGradient

        listOf("basketbol", "voleybol", "hentbol", "amerikan futbolu").contains(spor) -> PrimaryGradient

        listOf("fitness", "yoga", "pilates", "zumba", "jimnastik", "satranç").contains(spor) -> PurpleGradient

        listOf("futbol", "koşu", "bisiklet", "dağcılık", "kamp", "okçuluk").contains(spor) -> SecondaryGradient

        else -> GoldGradient
    }
}

fun konumIzniVarMi(ctx: Context) = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
fun koordinattanAdres(ctx: Context, l: LatLng): String = try { Geocoder(ctx, Locale("tr")).getFromLocation(l.latitude, l.longitude, 1)?.firstOrNull()?.let { listOfNotNull(it.thoroughfare, it.subLocality).joinToString(", ") } ?: "Konum" } catch (e: Exception) { "Konum" }

object TemaAyari { var karanlikMod = mutableStateOf(false) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)

        try { BildirimHelper.kanalOlustur(this) } catch (e: Exception) { e.printStackTrace() }

        setContent {
            val dark by TemaAyari.karanlikMod
            MaterialTheme(colorScheme = if (dark) KoyuRenkler else AcikRenkler) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppWithSplash()
                }
            }
        }
    }
}


@Composable
fun AppWithSplash() {
    var splashGosteriliyor by remember { mutableStateOf(true) }

    if (splashGosteriliyor) {
        SplashEkrani { splashGosteriliyor = false }
    } else {
        App()
    }
}

@Composable
fun SplashEkrani(onBitti: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(1f, 1.2f, infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale")
    val rotation by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "rotation")

    LaunchedEffect(Unit) {
        delay(2500)
        onBitti()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF667EEA), Color(0xFF764BA2)))),
        contentAlignment = Alignment.Center
    ) {

        Box(Modifier.size(300.dp).graphicsLayer { rotationZ = rotation }.background(Color.White.copy(0.1f), CircleShape))
        Box(Modifier.size(200.dp).graphicsLayer { rotationZ = -rotation }.background(Color.White.copy(0.1f), CircleShape))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(
                modifier = Modifier.size((100 * scale).dp).shadow(24.dp, CircleShape).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = (48 * scale).sp)
            }

            Spacer(Modifier.height(32.dp))

            Text("JoinUp", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(8.dp))
            Text("Birlikte Oyna, Birlikte Kazan!", fontSize = 16.sp, color = Color.White.copy(0.9f))

            Spacer(Modifier.height(48.dp))

            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun App() {
    var girisYapildi by remember { mutableStateOf(Firebase.auth.currentUser != null) }
    var profilAcik by remember { mutableStateOf(false) }
    var istatistikAcik by remember { mutableStateOf(false) }
    var arkadaslarAcik by remember { mutableStateOf(false) }

    if (!girisYapildi) GirisEkrani { girisYapildi = true }
    else if (profilAcik) ProfilEkrani { profilAcik = false }
    else if (istatistikAcik) IstatistikEkrani { istatistikAcik = false }
    else if (arkadaslarAcik) ArkadaslarEkrani { arkadaslarAcik = false }
    else AnaEkran(
        onCikis = { Firebase.auth.signOut(); girisYapildi = false },
        onProfil = { profilAcik = true },
        onIstatistik = { istatistikAcik = true },
        onArkadaslar = { arkadaslarAcik = true }
    )
}

@Composable
fun GradientButton(text: String, gradient: Brush, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = DefaultShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.LightGray)), DefaultShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        }
    }
}

@Composable
fun GlowCard(modifier: Modifier = Modifier, glowColor: Color = MaterialTheme.colorScheme.primary, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.3f))
    ) {
        content()
    }
}

@Composable
fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val scale by infiniteTransition.animateFloat(1f, 1.1f, infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse), label = "scale")
    val rotation by infiniteTransition.animateFloat(-5f, 5f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "rotation")

    Box(
        modifier = Modifier.size(120.dp).graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = rotation }.shadow(24.dp, CircleShape, spotColor = Color(0xFF667EEA)).background(PrimaryGradient, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("🚀", fontSize = 56.sp)
    }
}

@Composable
fun YildizPuanlama(puan: Int, onPuanDegisti: (Int) -> Unit, enabled: Boolean = true) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { i ->
            IconButton(
                onClick = { if (enabled) onPuanDegisti(i) },
                modifier = Modifier.size(40.dp),
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (i <= puan) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    tint = if (i <= puan) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun GirisEkrani(onBasarili: () -> Unit) {
    val auth = Firebase.auth
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var yukleniyor by remember { mutableStateOf(false) }
    var sifreGorunur by remember { mutableStateOf(false) }

    val animatedOffset by animateFloatAsState(if (yukleniyor) 10f else 0f, animationSpec = tween(500), label = "offset")

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF667EEA).copy(0.1f), Color(0xFFF0F4FF), Color(0xFF764BA2).copy(0.1f))))
    ) {
        Box(Modifier.size(200.dp).offset((-50).dp, (-50).dp).background(Color(0xFF667EEA).copy(0.1f), CircleShape))
        Box(Modifier.size(150.dp).offset(300.dp, 100.dp).background(Color(0xFF764BA2).copy(0.1f), CircleShape))
        Box(Modifier.size(100.dp).offset(50.dp, 600.dp).background(Color(0xFF11998E).copy(0.1f), CircleShape))

        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp).offset(y = animatedOffset.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedLogo()
            Spacer(Modifier.height(24.dp))
            Text("JoinUp", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1F36))
            Text("Birlikte Oyna, Birlikte Kazan!", fontSize = 16.sp, color = Color(0xFF667EEA), fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(48.dp))

            GlowCard {
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, label = { Text("E-posta") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF667EEA)) },
                    modifier = Modifier.fillMaxWidth().padding(4.dp), shape = DefaultShape, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
            }
            Spacer(Modifier.height(16.dp))
            GlowCard {
                OutlinedTextField(
                    value = sifre, onValueChange = { sifre = it }, label = { Text("Şifre") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF667EEA)) },
                    trailingIcon = { IconButton(onClick = { sifreGorunur = !sifreGorunur }) { Icon(if (sifreGorunur) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray) } },
                    visualTransformation = if (sifreGorunur) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().padding(4.dp), shape = DefaultShape, singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                )
            }
            Spacer(Modifier.height(32.dp))

            if (yukleniyor) {
                CircularProgressIndicator(color = Color(0xFF667EEA), strokeWidth = 3.dp)
            } else {
                GradientButton(text = "🚀 Giriş Yap", gradient = PrimaryGradient, onClick = {
                    if (email.isNotEmpty() && sifre.isNotEmpty()) {
                        yukleniyor = true
                        auth.signInWithEmailAndPassword(email, sifre)
                            .addOnSuccessListener { onBasarili() }
                            .addOnFailureListener { yukleniyor = false; Toast.makeText(ctx, "Hata: ${it.message}", Toast.LENGTH_SHORT).show() }
                    }
                }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.weight(1f), color = Color.Gray.copy(0.3f))
                    Text("  veya  ", color = Color.Gray)
                    HorizontalDivider(Modifier.weight(1f), color = Color.Gray.copy(0.3f))
                }
                Spacer(Modifier.height(16.dp))
                GradientButton(text = "✨ Yeni Hesap Oluştur", gradient = SecondaryGradient, onClick = {
                    if (email.isNotEmpty() && sifre.length >= 6) {
                        yukleniyor = true
                        auth.createUserWithEmailAndPassword(email, sifre)
                            .addOnSuccessListener { onBasarili() }
                            .addOnFailureListener { yukleniyor = false; Toast.makeText(ctx, "Hata: ${it.message}", Toast.LENGTH_SHORT).show() }
                    }
                }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(onCikis: () -> Unit, onProfil: () -> Unit, onIstatistik: () -> Unit, onArkadaslar: () -> Unit) {
    var sekme by remember { mutableStateOf(0) }
    var etkinlikDialogAcik by remember { mutableStateOf(false) }
    var ayarlarAcik by remember { mutableStateOf(false) }

    if (etkinlikDialogAcik) { EtkinlikOlusturDialog { etkinlikDialogAcik = false } }
    if (ayarlarAcik) { AyarlarEkrani(onKapat = { ayarlarAcik = false }, onProfil = onProfil, onCikis = onCikis, onIstatistik = onIstatistik, onArkadaslar = onArkadaslar) }

    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.Transparent) {
                Box(modifier = Modifier.fillMaxWidth().background(PrimaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🚀", fontSize = 28.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("JoinUp", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                                Text("Hoş Geldin!", fontSize = 12.sp, color = Color.White.copy(0.8f))
                            }
                        }
                        IconButton(onClick = { ayarlarAcik = true }) { Icon(Icons.Default.Settings, null, tint = Color.White, modifier = Modifier.size(28.dp)) }
                    }
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 20.dp, color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)) {
                NavigationBar(containerColor = Color.Transparent, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    listOf(Triple(Icons.Default.Explore, "Keşfet", 0), Triple(Icons.Default.CalendarMonth, "Takvim", 1), Triple(Icons.Default.Public, "Topluluklar", 2), Triple(Icons.Default.Groups, "Takım", 3), Triple(Icons.Default.Map, "Harita", 4)).forEach { (icon, label, index) ->
                        NavigationBarItem(
                            selected = sekme == index, onClick = { sekme = index },
                            icon = { Box(modifier = if (sekme == index) Modifier.background(PrimaryGradient, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = 8.dp) else Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { Icon(icon, null, tint = if (sekme == index) Color.White else Color.Gray) } },
                            label = { Text(label, fontSize = 10.sp, fontWeight = if (sekme == index) FontWeight.Bold else FontWeight.Normal) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                        )
                    }
                }
            }
        },
        floatingActionButton = {

            Surface(
                onClick = { etkinlikDialogAcik = true },
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent,
                shadowElevation = 0.dp
            ) {
                Box(
                    modifier = Modifier
                        .background(PrimaryGradient)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Etkinlik",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Oluştur",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    ) { pv ->
        Box(Modifier.fillMaxSize().padding(pv)) {
            when (sekme) {
                0 -> KesifEkrani()
                1 -> TakvimEkrani()
                2 -> TopluluklarEkrani()
                3 -> TakimimEkrani()
                4 -> HaritaEkrani()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyarlarEkrani(onKapat: () -> Unit, onProfil: () -> Unit, onCikis: () -> Unit, onIstatistik: () -> Unit, onArkadaslar: () -> Unit) {
    val ctx = LocalContext.current
    var karanlikMod by TemaAyari.karanlikMod
    var bildirimEtkinlik by remember { mutableStateOf(true) }
    var bildirimMesaj by remember { mutableStateOf(true) }
    var bildirimTopluluk by remember { mutableStateOf(true) }
    var profilGizle by remember { mutableStateOf(false) }
    var konumPaylas by remember { mutableStateOf(true) }
    var cevrimiciGoster by remember { mutableStateOf(true) }
    var cikisOnayi by remember { mutableStateOf(false) }

    if (cikisOnayi) {
        AlertDialog(onDismissRequest = { cikisOnayi = false }, title = { Text("🚪 Çıkış Yap") }, text = { Text("Hesabınızdan çıkış yapmak istediğinizden emin misiniz?") },
            confirmButton = { Button(onClick = { cikisOnayi = false; onKapat(); onCikis() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Çıkış Yap") } },
            dismissButton = { TextButton(onClick = { cikisOnayi = false }) { Text("İptal") } }
        )
    }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(topBar = { Box(Modifier.fillMaxWidth().background(PrimaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onKapat) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Text("⚙️ Ayarlar", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) } } }) { pv ->
                LazyColumn(Modifier.fillMaxSize().padding(pv).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    item { Text("👤 Hesap", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { AyarKarti(icon = Icons.Default.Person, baslik = "Profil Düzenle", aciklama = "Ad, fotoğraf ve iletişim bilgileri", onClick = { onKapat(); onProfil() }) }
                    item { AyarKarti(icon = Icons.Default.BarChart, baslik = "İstatistiklerim", aciklama = "Etkinlik geçmişi ve başarılar", onClick = { onKapat(); onIstatistik() }) }
                    item { AyarKarti(icon = Icons.Default.People, baslik = "Arkadaşlarım", aciklama = "Arkadaş listesi ve istekler", onClick = { onKapat(); onArkadaslar() }) }
                    item { AyarKarti(icon = Icons.Default.Lock, baslik = "Şifre Değiştir", aciklama = "Hesap güvenliğinizi güncelleyin", onClick = { Toast.makeText(ctx, "Yakında!", Toast.LENGTH_SHORT).show() }) }

                    item { Spacer(Modifier.height(8.dp)); Text("🔔 Bildirimler", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { AyarSwitch(icon = Icons.Default.Event, baslik = "Etkinlik Hatırlatıcıları", aciklama = "Yaklaşan etkinlikler için bildirim", checked = bildirimEtkinlik, onCheckedChange = { bildirimEtkinlik = it }) }
                    item { AyarSwitch(icon = Icons.Default.Chat, baslik = "Yeni Mesajlar", aciklama = "Sohbet bildirimleri", checked = bildirimMesaj, onCheckedChange = { bildirimMesaj = it }) }
                    item { AyarSwitch(icon = Icons.Default.Campaign, baslik = "Topluluk Duyuruları", aciklama = "Topluluk etkinlikleri ve haberler", checked = bildirimTopluluk, onCheckedChange = { bildirimTopluluk = it }) }

                    item { Spacer(Modifier.height(8.dp)); Text("🎨 Görünüm", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { AyarSwitch(icon = if (karanlikMod) Icons.Default.DarkMode else Icons.Default.LightMode, baslik = "Karanlık Mod", aciklama = if (karanlikMod) "Açık" else "Kapalı", checked = karanlikMod, onCheckedChange = { karanlikMod = it }) }

                    item { Spacer(Modifier.height(8.dp)); Text("🔒 Gizlilik", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { AyarSwitch(icon = Icons.Default.VisibilityOff, baslik = "Profilimi Gizle", aciklama = "Diğer kullanıcılar profilinizi göremez", checked = profilGizle, onCheckedChange = { profilGizle = it }) }
                    item { AyarSwitch(icon = Icons.Default.LocationOn, baslik = "Konum Paylaşımı", aciklama = "Etkinliklerde konumunuzu paylaşın", checked = konumPaylas, onCheckedChange = { konumPaylas = it }) }
                    item { AyarSwitch(icon = Icons.Default.Circle, baslik = "Çevrimiçi Durumu", aciklama = "Aktif olduğunuzda görünsün", checked = cevrimiciGoster, onCheckedChange = { cevrimiciGoster = it }) }

                    item { Spacer(Modifier.height(8.dp)); Text("💬 Destek", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { AyarKarti(icon = Icons.Default.Help, baslik = "Yardım Merkezi", aciklama = "Sık sorulan sorular", onClick = { Toast.makeText(ctx, "Yakında!", Toast.LENGTH_SHORT).show() }) }
                    item { AyarKarti(icon = Icons.Default.Star, baslik = "Uygulamayı Puanla", aciklama = "Play Store'da değerlendirin", onClick = { Toast.makeText(ctx, "Yakında!", Toast.LENGTH_SHORT).show() }) }

                    item { Spacer(Modifier.height(8.dp)); Text("ℹ️ Hakkında", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 8.dp)) }
                    item { GlowCard { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text("Uygulama Sürümü"); Text("1.0.0", color = Color.Gray) } } }

                    item { Spacer(Modifier.height(24.dp)) }
                    item { GradientButton(text = "🚪 Çıkış Yap", gradient = Brush.linearGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))), onClick = { cikisOnayi = true }, modifier = Modifier.fillMaxWidth()) }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

@Composable
fun AyarKarti(icon: androidx.compose.ui.graphics.vector.ImageVector, baslik: String, aciklama: String, onClick: () -> Unit) {
    GlowCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(PrimaryGradient, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(baslik, fontWeight = FontWeight.SemiBold); Text(aciklama, color = Color.Gray, fontSize = 12.sp) }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}

@Composable
fun AyarSwitch(icon: androidx.compose.ui.graphics.vector.ImageVector, baslik: String, aciklama: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    GlowCard {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(if (checked) SecondaryGradient else Brush.linearGradient(listOf(Color.Gray, Color.LightGray)), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) { Text(baslik, fontWeight = FontWeight.SemiBold); Text(aciklama, color = Color.Gray, fontSize = 12.sp) }
            Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF11998E)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KesifEkrani() {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current

    var arama by remember { mutableStateOf("") }
    var secilenKategori by remember { mutableStateOf("Hepsi") }
    var secilenSpor by remember { mutableStateOf<String?>(null) }

    val maclar = remember { mutableStateListOf<MacModel>() }

    DisposableEffect(Unit) {
        val l = db.collection("maclar").addSnapshotListener { s, _ ->
            maclar.clear()
            s?.documents?.forEach { d ->
                maclar.add(MacModel(
                    d.id,
                    d.getString("macAdi")?:"",
                    d.getString("sporTuru")?:"",
                    d.getString("konum")?:"",
                    d.getString("tarihSaat")?:"",
                    d.getLong("oyuncuSayisi")?.toInt()?:0,
                    d.getString("sahipId")?:"",
                    d.getDouble("latitude")?:0.0,
                    d.getDouble("longitude")?:0.0,
                    (d.get("katilimcilar") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                ))
            }
        }
        onDispose { l.remove() }
    }

    Column(Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)))
                .padding(16.dp)
        ) {
            Text("🔥 Keşfet", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Yakınındaki Etkinlikleri Bul", color = Color.Gray)
            Spacer(Modifier.height(16.dp))


            GlowCard(glowColor = Color(0xFF667EEA)) {
                OutlinedTextField(
                    value = arama,
                    onValueChange = { arama = it },
                    placeholder = { Text("🔍 Etkinlik ara...", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    shape = DefaultShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                )
            }

            Spacer(Modifier.height(16.dp))


            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = secilenKategori == "Hepsi",
                        onClick = {
                            secilenKategori = "Hepsi"
                            secilenSpor = null
                        },
                        label = { Text("🌟 Hepsi") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF667EEA),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(sporKategorileri) { k ->
                    FilterChip(
                        selected = secilenKategori == k.ad,
                        onClick = {
                            if (secilenKategori != k.ad) {
                                secilenKategori = k.ad
                                secilenSpor = null
                            }
                        },
                        label = { Text("${k.emoji} ${k.ad}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF667EEA),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }


            if (secilenKategori != "Hepsi") {
                val aktifKategori = sporKategorileri.find { it.ad == secilenKategori }

                if (aktifKategori != null) {
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(aktifKategori.sporlar) { spor ->
                            val secili = secilenSpor == spor


                            val renk = sporRengi(spor)
                            val seffafBrush = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))

                            Surface(
                                onClick = { secilenSpor = if (secilenSpor == spor) null else spor },
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Transparent,
                                modifier = Modifier.height(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = if (secili) renk else seffafBrush,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (secili) Color.Transparent else Color.Gray,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = spor,
                                        color = if (secili) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = if (secili) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }


        val liste = maclar.filter { mac ->
            val kategoriUygun = when {
                secilenKategori == "Hepsi" -> true
                secilenSpor != null -> mac.sporTuru.equals(secilenSpor, ignoreCase = true)
                else -> sporKategorileri.find { it.ad == secilenKategori }?.sporlar?.any { it.equals(mac.sporTuru, true) } ?: false
            }
            kategoriUygun && (arama.isEmpty() || mac.macAdi.contains(arama, true))
        }

        if (liste.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("🏟️", fontSize = 80.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (secilenSpor != null) "$secilenSpor etkinliği yok" else "Henüz etkinlik yok",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text("İlk etkinliği sen oluştur!", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                items(items = liste, key = { it.id }) { mac ->
                    MacKarti(mac = mac, uid = uid, ctx = ctx)
                }
            }
        }
    }
}

@Composable
fun TakvimEkrani() {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var etkinlikler by remember { mutableStateOf<List<MacModel>>(emptyList()) }

    DisposableEffect(uid) {
        val l = uid?.let { db.collection("maclar").whereArrayContains("katilimcilar", it).addSnapshotListener { s, _ ->
            etkinlikler = s?.documents?.map { d -> MacModel(d.id, d.getString("macAdi")?:"", d.getString("sporTuru")?:"", d.getString("konum")?:"", d.getString("tarihSaat")?:"", 0, "", d.getDouble("latitude")?:0.0, d.getDouble("longitude")?:0.0) } ?: emptyList()
        } }
        onDispose { l?.remove() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("📅 Takvimim", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Katıldığınız Etkinlikler", color = Color.Gray)
        Spacer(Modifier.height(24.dp))

        if (etkinlikler.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(120.dp).background(OceanGradient, CircleShape), contentAlignment = Alignment.Center) {
                    Text("📅", fontSize = 56.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text("Katıldığınız etkinlik yok", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Keşfet sekmesinden etkinliklere katılın!", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items = etkinlikler, key = { it.id }) { mac ->
                    GlowCard(glowColor = Color(0xFF11998E)) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp).background(sporRengi(mac.sporTuru), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Text(sporEmoji(mac.sporTuru), fontSize = 28.sp)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(mac.macAdi, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), tint = Color(0xFF11998E))
                                        Spacer(Modifier.width(4.dp))
                                        Text(mac.tarihSaat, color = Color(0xFF11998E), fontWeight = FontWeight.Medium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(Modifier.width(4.dp))
                                        Text(mac.konum, color = Color.Gray, fontSize = 14.sp)
                                    }
                                }
                            }
                            if (mac.latitude != 0.0) {
                                Spacer(Modifier.height(16.dp))
                                GradientButton(
                                    text = "🗺️ Yol Tarifi Al",
                                    gradient = OceanGradient,
                                    onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${mac.latitude},${mac.longitude}"))) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopluluklarEkrani() {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var topluluklar by remember { mutableStateOf<List<ToplulukModel>>(emptyList()) }
    var arama by remember { mutableStateOf("") }
    var dialogAcik by remember { mutableStateOf(false) }
    var secilenTopluluk by remember { mutableStateOf<ToplulukModel?>(null) }

    DisposableEffect(Unit) {
        val l = db.collection("topluluklar").addSnapshotListener { s, _ ->
            topluluklar = s?.documents?.map { d -> ToplulukModel(d.id, d.getString("ad")?:"", d.getString("kategori")?:"", d.getString("olusturanId")?:"", (d.get("uyeler") as? List<*>)?.filterIsInstance<String>() ?: emptyList()) } ?: emptyList()
        }
        onDispose { l.remove() }
    }

    if (dialogAcik) { ToplulukOlusturDialog { dialogAcik = false } }
    secilenTopluluk?.let { t -> ToplulukDetayDialog(topluluk = t, onKapat = { secilenTopluluk = null }); return }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("🌍 Topluluklar", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Spor Topluluklarına Katıl", color = Color.Gray)
            }
            FloatingActionButton(
                onClick = { dialogAcik = true },
                containerColor = Color.Transparent,
                modifier = Modifier.size(48.dp)
            ) {
                Box(Modifier.fillMaxSize().background(SecondaryGradient, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GlowCard(glowColor = Color(0xFF11998E)) {
            OutlinedTextField(
                value = arama,
                onValueChange = { arama = it },
                placeholder = { Text("🔍 Topluluk ara...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                shape = DefaultShape,
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
            )
        }

        Spacer(Modifier.height(16.dp))

        val liste = topluluklar.filter { arama.isEmpty() || it.ad.contains(arama, true) }

        if (liste.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(120.dp).background(SecondaryGradient, CircleShape), contentAlignment = Alignment.Center) {
                    Text("🌍", fontSize = 56.sp)
                }
                Spacer(Modifier.height(24.dp))
                Text(if (arama.isNotEmpty()) "Sonuç bulunamadı" else "Henüz topluluk yok", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items = liste, key = { it.id }) { t ->
                    GlowCard(glowColor = Color(0xFF667EEA), modifier = Modifier.clickable { secilenTopluluk = t }) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(56.dp).background(sporRengi(t.kategori), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                                    Text(sporEmoji(t.kategori), fontSize = 28.sp)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(t.ad, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.People, null, Modifier.size(16.dp), tint = Color.Gray)
                                        Spacer(Modifier.width(4.dp))
                                        Text("${t.uyeler.size} üye", color = Color.Gray)
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            if (t.uyeler.contains(uid)) {
                                Surface(color = Color(0xFF11998E).copy(0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF11998E))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Üyesiniz", color = Color(0xFF11998E), fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                GradientButton(
                                    text = "✨ Katıl",
                                    gradient = SecondaryGradient,
                                    onClick = { uid?.let { db.collection("topluluklar").document(t.id).update("uyeler", FieldValue.arrayUnion(it)); Toast.makeText(ctx, "Hoş geldin! 🎉", Toast.LENGTH_SHORT).show() } },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToplulukOlusturDialog(onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var ad by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Text("🌍 Yeni Topluluk", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))

                GlowCard {
                    OutlinedTextField(value = ad, onValueChange = { ad = it }, label = { Text("Topluluk Adı") }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent))
                }
                Spacer(Modifier.height(16.dp))
                SporSecici(secilen = kategori, onSecildi = { kategori = it })
                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onKapat, Modifier.weight(1f).height(56.dp), shape = DefaultShape) { Text("İptal") }
                    Box(Modifier.weight(1f)) {
                        GradientButton(
                            text = "✨ Oluştur",
                            gradient = SecondaryGradient,
                            onClick = { if (ad.isNotEmpty()) { uid?.let { db.collection("topluluklar").add(hashMapOf("ad" to ad, "kategori" to kategori, "olusturanId" to it, "uyeler" to listOf(it))); Toast.makeText(ctx, "Oluşturuldu! 🎉", Toast.LENGTH_SHORT).show(); onKapat() } } },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ad.isNotEmpty()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToplulukDetayDialog(topluluk: ToplulukModel, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var mesaj by remember { mutableStateOf("") }
    val mesajlar = remember { mutableStateListOf<MesajModel>() }
    var uyeAdlari by remember { mutableStateOf<List<String>>(emptyList()) }
    var menuAcik by remember { mutableStateOf(false) }
    var sessizMi by remember { mutableStateOf(false) }
    var cikisOnayi by remember { mutableStateOf(false) }
    var silmeOnayi by remember { mutableStateOf(false) }

    DisposableEffect(topluluk.id) {
        val l = db.collection("topluluklar").document(topluluk.id).collection("mesajlar").orderBy("tarih").addSnapshotListener { s, _ ->
            mesajlar.clear()
            s?.documents?.forEach { d -> mesajlar.add(MesajModel(d.id, d.getString("gonderenId")?:"", d.getString("gonderenIsim")?:"", d.getString("mesaj")?:"", d.getLong("tarih")?:0)) }
        }
        onDispose { l.remove() }
    }

    LaunchedEffect(topluluk.uyeler) {
        val adlar = mutableListOf<String>()
        topluluk.uyeler.forEach { id -> db.collection("kullanicilar").document(id).get().addOnSuccessListener { adlar.add(it.getString("isim") ?: "Kullanıcı"); uyeAdlari = adlar.toList() } }
    }

    if (cikisOnayi) {
        AlertDialog(onDismissRequest = { cikisOnayi = false }, title = { Text("🚪 Topluluktan Ayrıl") }, text = { Text("${topluluk.ad} topluluğundan ayrılmak istediğinizden emin misiniz?") },
            confirmButton = { Button(onClick = { uid?.let { db.collection("topluluklar").document(topluluk.id).update("uyeler", FieldValue.arrayRemove(it)) }; Toast.makeText(ctx, "Topluluktan ayrıldınız", Toast.LENGTH_SHORT).show(); cikisOnayi = false; onKapat() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Ayrıl") } },
            dismissButton = { TextButton(onClick = { cikisOnayi = false }) { Text("İptal") } }
        )
    }

    if (silmeOnayi) {
        AlertDialog(onDismissRequest = { silmeOnayi = false }, title = { Text("🗑️ Topluluğu Sil") }, text = { Text("${topluluk.ad} topluluğunu silmek istediğinizden emin misiniz?") },
            confirmButton = { Button(onClick = { db.collection("topluluklar").document(topluluk.id).delete(); Toast.makeText(ctx, "Topluluk silindi", Toast.LENGTH_SHORT).show(); silmeOnayi = false; onKapat() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Sil") } },
            dismissButton = { TextButton(onClick = { silmeOnayi = false }) { Text("İptal") } }
        )
    }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    Box(Modifier.fillMaxWidth().background(PrimaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onKapat) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                                Text(topluluk.ad, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                            Box {
                                IconButton(onClick = { menuAcik = true }) { Icon(Icons.Default.MoreVert, null, tint = Color.White) }
                                DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                                    DropdownMenuItem(text = { Row { Icon(if (sessizMi) Icons.Default.VolumeUp else Icons.Default.VolumeOff, null, Modifier.size(20.dp)); Spacer(Modifier.width(12.dp)); Text(if (sessizMi) "Sesi Aç" else "Sessize Al") }}, onClick = { sessizMi = !sessizMi; menuAcik = false; Toast.makeText(ctx, if (sessizMi) "Bildirimler kapatıldı" else "Bildirimler açıldı", Toast.LENGTH_SHORT).show() })
                                    HorizontalDivider()
                                    DropdownMenuItem(text = { Row { Icon(Icons.Default.ExitToApp, null, Modifier.size(20.dp), tint = Color(0xFFF59E0B)); Spacer(Modifier.width(12.dp)); Text("Topluluktan Ayrıl", color = Color(0xFFF59E0B)) }}, onClick = { menuAcik = false; cikisOnayi = true })
                                    if (topluluk.olusturanId == uid) {
                                        HorizontalDivider()
                                        DropdownMenuItem(text = { Row { Icon(Icons.Default.Delete, null, Modifier.size(20.dp), tint = Color(0xFFEF4444)); Spacer(Modifier.width(12.dp)); Text("Topluluğu Sil", color = Color(0xFFEF4444)) }}, onClick = { menuAcik = false; silmeOnayi = true })
                                    }
                                }
                            }
                        }
                    }
                }
            ) { pv ->
                Column(Modifier.fillMaxSize().padding(pv)) {
                    if (sessizMi) {
                        Surface(color = Color(0xFFF59E0B).copy(0.1f), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.VolumeOff, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Bu topluluk sessize alındı", color = Color(0xFFF59E0B), fontSize = 13.sp) }
                        }
                    }
                    TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("💬 Sohbet", fontWeight = if (tab == 0) FontWeight.Bold else FontWeight.Normal) })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("👥 Üyeler (${topluluk.uyeler.size})", fontWeight = if (tab == 1) FontWeight.Bold else FontWeight.Normal) })
                    }
                    when (tab) {
                        0 -> {
                            Column(Modifier.fillMaxSize()) {
                                LazyColumn(Modifier.weight(1f).padding(16.dp)) {
                                    items(items = mesajlar, key = { it.id }) { m ->
                                        val benim = m.gonderenId == uid
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = if (benim) Arrangement.End else Arrangement.Start) {
                                            Surface(color = if (benim) Color(0xFF667EEA) else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                                                Column(Modifier.padding(12.dp)) {
                                                    if (!benim) Text(m.gonderenIsim, fontSize = 12.sp, color = Color(0xFF667EEA), fontWeight = FontWeight.Bold)
                                                    Text(m.mesaj, color = if (benim) Color.White else MaterialTheme.colorScheme.onSurface)
                                                }
                                            }
                                        }
                                    }
                                }
                                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(value = mesaj, onValueChange = { mesaj = it }, placeholder = { Text("Mesaj yaz...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        FloatingActionButton(onClick = { if (mesaj.isNotBlank()) { db.collection("topluluklar").document(topluluk.id).collection("mesajlar").add(hashMapOf("gonderenId" to uid, "gonderenIsim" to "Kullanıcı", "mesaj" to mesaj, "tarih" to System.currentTimeMillis())); mesaj = "" } }, modifier = Modifier.size(48.dp), containerColor = Color.Transparent) {
                                            Box(Modifier.fillMaxSize().background(PrimaryGradient, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, null, tint = Color.White) }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(items = uyeAdlari) { ad ->
                                    GlowCard {
                                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(48.dp).background(PrimaryGradient, CircleShape), contentAlignment = Alignment.Center) { Text(ad.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) }
                                            Spacer(Modifier.width(16.dp))
                                            Text(ad, fontWeight = FontWeight.Medium, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TakimimEkrani() {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var takimlar by remember { mutableStateOf<List<TakimModel>>(emptyList()) }
    var dialogAcik by remember { mutableStateOf(false) }
    var secilenTakim by remember { mutableStateOf<TakimModel?>(null) }
    var silmeOnayi by remember { mutableStateOf<TakimModel?>(null) }

    DisposableEffect(uid) {
        val l = uid?.let { db.collection("takimlar").whereArrayContains("uyeler", it).addSnapshotListener { s, _ ->
            takimlar = s?.documents?.map { d -> TakimModel(d.id, d.getString("takimAdi")?:"", d.getString("olusturanId")?:"", (d.get("uyeler") as? List<*>)?.filterIsInstance<String>() ?: emptyList()) } ?: emptyList()
        } }
        onDispose { l?.remove() }
    }

    silmeOnayi?.let { t ->
        AlertDialog(onDismissRequest = { silmeOnayi = null }, title = { Text("⚠️ Takımı Sil") }, text = { Text("${t.takimAdi} silinsin mi?") },
            confirmButton = { Button(onClick = { db.collection("takimlar").document(t.id).delete(); silmeOnayi = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Sil") } },
            dismissButton = { TextButton(onClick = { silmeOnayi = null }) { Text("İptal") } }
        )
    }

    if (dialogAcik) { TakimOlusturDialog { dialogAcik = false } }
    secilenTakim?.let { t -> TakimDetayDialog(takim = t, onKapat = { secilenTakim = null }); return }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("👥 Takımlarım", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Takımlarını Yönet", color = Color.Gray)
            }
            FloatingActionButton(onClick = { dialogAcik = true }, containerColor = Color.Transparent, modifier = Modifier.size(48.dp)) {
                Box(Modifier.fillMaxSize().background(AccentGradient, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White) }
            }
        }
        Spacer(Modifier.height(24.dp))

        if (takimlar.isEmpty()) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(120.dp).background(AccentGradient, CircleShape), contentAlignment = Alignment.Center) { Text("🏆", fontSize = 56.sp) }
                Spacer(Modifier.height(24.dp))
                Text("Henüz takımınız yok", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("İlk takımını oluştur!", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(items = takimlar, key = { it.id }) { t ->
                    GlowCard(glowColor = Color(0xFFFC466B), modifier = Modifier.clickable { secilenTakim = t }) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(56.dp).background(AccentGradient, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text("👥", fontSize = 28.sp) }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(t.takimAdi, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.People, null, Modifier.size(16.dp), tint = Color.Gray); Spacer(Modifier.width(4.dp)); Text("${t.uyeler.size} üye", color = Color.Gray) }
                            }
                            if (t.olusturanId == uid) { IconButton(onClick = { silmeOnayi = t }) { Icon(Icons.Outlined.Delete, null, tint = Color(0xFFEF4444)) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TakimOlusturDialog(onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var ad by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Text("🏆 Yeni Takım", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                GlowCard { OutlinedTextField(value = ad, onValueChange = { ad = it }, label = { Text("Takım Adı") }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onKapat, Modifier.weight(1f).height(56.dp), shape = DefaultShape) { Text("İptal") }
                    Box(Modifier.weight(1f)) { GradientButton(text = "✨ Oluştur", gradient = AccentGradient, onClick = { if (ad.isNotEmpty()) { uid?.let { db.collection("takimlar").add(hashMapOf("takimAdi" to ad, "olusturanId" to it, "uyeler" to listOf(it))); Toast.makeText(ctx, "Oluşturuldu! 🎉", Toast.LENGTH_SHORT).show(); onKapat() } } }, modifier = Modifier.fillMaxWidth(), enabled = ad.isNotEmpty()) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakimDetayDialog(takim: TakimModel, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var mesaj by remember { mutableStateOf("") }
    val mesajlar = remember { mutableStateListOf<MesajModel>() }
    var etkinlikler by remember { mutableStateOf<List<TakimEtkinlikModel>>(emptyList()) }
    var etkinlikDialogAcik by remember { mutableStateOf(false) }
    var uyeEkleAcik by remember { mutableStateOf(false) }

    DisposableEffect(takim.id) {
        val l1 = db.collection("takimlar").document(takim.id).collection("mesajlar").orderBy("tarih").addSnapshotListener { s, _ ->
            mesajlar.clear()
            s?.documents?.forEach { d -> mesajlar.add(MesajModel(d.id, d.getString("gonderenId")?:"", d.getString("gonderenIsim")?:"", d.getString("mesaj")?:"", d.getLong("tarih")?:0)) }
        }
        val l2 = db.collection("takimlar").document(takim.id).collection("etkinlikler").addSnapshotListener { s, _ ->
            etkinlikler = s?.documents?.map { d -> TakimEtkinlikModel(d.id, takim.id, d.getString("baslik")?:"", d.getString("tarihSaat")?:"", (d.get("onaylayanlar") as? List<*>)?.filterIsInstance<String>() ?: emptyList(), (d.get("reddedenler") as? List<*>)?.filterIsInstance<String>() ?: emptyList()) } ?: emptyList()
        }
        onDispose { l1.remove(); l2.remove() }
    }

    if (etkinlikDialogAcik) { TakimEtkinlikDialog(takimId = takim.id, onKapat = { etkinlikDialogAcik = false }) }
    if (uyeEkleAcik) { UyeEkleDialog(takimId = takim.id, onKapat = { uyeEkleAcik = false }) }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    Box(Modifier.fillMaxWidth().background(AccentGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onKapat) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }
                                Text(takim.takimAdi, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            }
                            IconButton(onClick = { uyeEkleAcik = true }) { Icon(Icons.Default.PersonAdd, null, tint = Color.White) }
                        }
                    }
                }
            ) { pv ->
                Column(Modifier.fillMaxSize().padding(pv)) {
                    TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("📅 Etkinlikler", fontWeight = if (tab == 0) FontWeight.Bold else FontWeight.Normal) })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("💬 Sohbet", fontWeight = if (tab == 1) FontWeight.Bold else FontWeight.Normal) })
                    }

                    when (tab) {
                        0 -> {
                            Column(Modifier.fillMaxSize().padding(16.dp)) {
                                GradientButton(text = "✨ Etkinlik Öner", gradient = AccentGradient, onClick = { etkinlikDialogAcik = true }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(16.dp))

                                if (etkinlikler.isEmpty()) {
                                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Text("📅", fontSize = 64.sp)
                                        Spacer(Modifier.height(16.dp))
                                        Text("Henüz etkinlik yok", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        items(items = etkinlikler, key = { it.id }) { e ->
                                            val onayliMi = e.onaylayanlar.contains(uid)
                                            val reddettMi = e.reddedenler.contains(uid)
                                            GlowCard(glowColor = Color(0xFFFC466B)) {
                                                Column(Modifier.padding(20.dp)) {
                                                    Text(e.baslik, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp), tint = Color(0xFFFC466B))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(e.tarihSaat, color = Color(0xFFFC466B))
                                                    }
                                                    Spacer(Modifier.height(16.dp))
                                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                        Surface(
                                                            onClick = { uid?.let { db.collection("takimlar").document(takim.id).collection("etkinlikler").document(e.id).update("onaylayanlar", FieldValue.arrayUnion(it)); db.collection("takimlar").document(takim.id).collection("etkinlikler").document(e.id).update("reddedenler", FieldValue.arrayRemove(it)) } },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(14.dp),
                                                            color = if (onayliMi) Color(0xFF11998E) else Color(0xFF11998E).copy(0.1f)
                                                        ) {
                                                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = if (onayliMi) Color.White else Color(0xFF11998E))
                                                                Spacer(Modifier.width(8.dp))
                                                                Text("${e.onaylayanlar.size}", fontWeight = FontWeight.Bold, color = if (onayliMi) Color.White else Color(0xFF11998E))
                                                            }
                                                        }
                                                        Surface(
                                                            onClick = { uid?.let { db.collection("takimlar").document(takim.id).collection("etkinlikler").document(e.id).update("reddedenler", FieldValue.arrayUnion(it)); db.collection("takimlar").document(takim.id).collection("etkinlikler").document(e.id).update("onaylayanlar", FieldValue.arrayRemove(it)) } },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(14.dp),
                                                            color = if (reddettMi) Color(0xFFEF4444) else Color(0xFFEF4444).copy(0.1f)
                                                        ) {
                                                            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.Cancel, null, Modifier.size(20.dp), tint = if (reddettMi) Color.White else Color(0xFFEF4444))
                                                                Spacer(Modifier.width(8.dp))
                                                                Text("${e.reddedenler.size}", fontWeight = FontWeight.Bold, color = if (reddettMi) Color.White else Color(0xFFEF4444))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(Modifier.fillMaxSize()) {
                                LazyColumn(Modifier.weight(1f).padding(16.dp)) {
                                    items(items = mesajlar, key = { it.id }) { m ->
                                        val benim = m.gonderenId == uid
                                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = if (benim) Arrangement.End else Arrangement.Start) {
                                            Surface(color = if (benim) Color(0xFFFC466B) else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                                                Text(m.mesaj, Modifier.padding(12.dp), color = if (benim) Color.White else MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        OutlinedTextField(value = mesaj, onValueChange = { mesaj = it }, placeholder = { Text("Mesaj yaz...") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), singleLine = true)
                                        Spacer(Modifier.width(8.dp))
                                        FloatingActionButton(onClick = { if (mesaj.isNotBlank()) { db.collection("takimlar").document(takim.id).collection("mesajlar").add(hashMapOf("gonderenId" to uid, "mesaj" to mesaj, "tarih" to System.currentTimeMillis())); mesaj = "" } }, modifier = Modifier.size(48.dp), containerColor = Color.Transparent) {
                                            Box(Modifier.fillMaxSize().background(AccentGradient, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Send, null, tint = Color.White) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TakimEtkinlikDialog(takimId: String, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var baslik by remember { mutableStateOf("") }
    var tarihSaat by remember { mutableStateOf("") }
    val takvim = Calendar.getInstance()
    val zaman = TimePickerDialog(ctx, { _, s, d -> tarihSaat = "$tarihSaat - ${String.format("%02d:%02d", s, d)}" }, takvim.get(Calendar.HOUR_OF_DAY), takvim.get(Calendar.MINUTE), true)
    val tarih = DatePickerDialog(ctx, { _, y, a, g -> tarihSaat = "$g/${a+1}/$y"; zaman.show() }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH))

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Text("📅 Etkinlik Öner", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                GlowCard { OutlinedTextField(value = baslik, onValueChange = { baslik = it }, label = { Text("Etkinlik Başlığı") }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
                Spacer(Modifier.height(16.dp))
                GlowCard(modifier = Modifier.clickable { tarih.show() }) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF667EEA)); Spacer(Modifier.width(12.dp)); Text(if (tarihSaat.isNotEmpty()) tarihSaat else "📅 Tarih ve Saat Seç", color = if (tarihSaat.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface) } }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onKapat, Modifier.weight(1f).height(56.dp), shape = DefaultShape) { Text("İptal") }
                    Box(Modifier.weight(1f)) { GradientButton(text = "✨ Öner", gradient = AccentGradient, onClick = { if (baslik.isNotEmpty()) { uid?.let { db.collection("takimlar").document(takimId).collection("etkinlikler").add(hashMapOf("baslik" to baslik, "tarihSaat" to tarihSaat, "onaylayanlar" to listOf(it), "reddedenler" to emptyList<String>())); Toast.makeText(ctx, "Önerildi! 🎉", Toast.LENGTH_SHORT).show(); onKapat() } } }, modifier = Modifier.fillMaxWidth(), enabled = baslik.isNotEmpty()) }
                }
            }
        }
    }
}

@Composable
fun UyeEkleDialog(takimId: String, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(24.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Text("👥 Üye Ekle", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                GlowCard { OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("E-posta Adresi") }, leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF667EEA)) }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onKapat, Modifier.weight(1f).height(56.dp), shape = DefaultShape) { Text("İptal") }
                    Box(Modifier.weight(1f)) { GradientButton(text = "✨ Ekle", gradient = SecondaryGradient, onClick = { db.collection("kullanicilar").whereEqualTo("email", email).get().addOnSuccessListener { r -> if (r.documents.isNotEmpty()) { db.collection("takimlar").document(takimId).update("uyeler", FieldValue.arrayUnion(r.documents[0].id)); Toast.makeText(ctx, "Eklendi! 🎉", Toast.LENGTH_SHORT).show(); onKapat() } else Toast.makeText(ctx, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show() } }, modifier = Modifier.fillMaxWidth(), enabled = email.isNotEmpty()) }
                }
            }
        }
    }
}

@Composable
fun HaritaEkrani() {
    val db = Firebase.firestore
    val ctx = LocalContext.current
    var maclar by remember { mutableStateOf<List<MacModel>>(emptyList()) }
    var secilen by remember { mutableStateOf<MacModel?>(null) }
    val cameraState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(VARSAYILAN_KONUM, 11f) }

    DisposableEffect(Unit) { val l = db.collection("maclar").addSnapshotListener { s, _ -> maclar = s?.documents?.map { d -> MacModel(d.id, d.getString("macAdi")?:"", d.getString("sporTuru")?:"", d.getString("konum")?:"", "", 0, "", d.getDouble("latitude")?:0.0, d.getDouble("longitude")?:0.0) } ?: emptyList() }; onDispose { l.remove() } }
    LaunchedEffect(Unit) { if (konumIzniVarMi(ctx)) { try { LocationServices.getFusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener { l -> l?.let { cameraState.position = CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 13f) } } } catch (e: SecurityException) { } } }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraState, properties = MapProperties(isMyLocationEnabled = konumIzniVarMi(ctx))) {
            maclar.filter { it.latitude != 0.0 }.forEach { m -> Marker(state = MarkerState(LatLng(m.latitude, m.longitude)), title = m.macAdi, onClick = { secilen = m; true }) }
        }

        Surface(Modifier.align(Alignment.TopCenter).padding(top = 16.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface.copy(0.9f), shadowElevation = 8.dp) {
            Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🗺️", fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text("Yakınındaki Etkinlikler", fontWeight = FontWeight.Bold)
            }
        }

        secilen?.let { m ->
            GlowCard(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(), glowColor = Color(0xFF667EEA)) {
                Column(Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(56.dp).background(sporRengi(m.sporTuru), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text(sporEmoji(m.sporTuru), fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.macAdi, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = Color.Gray)
                                Spacer(Modifier.width(4.dp))
                                Text(m.konum, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { secilen = null }) { Icon(Icons.Default.Close, null) }
                    }
                    Spacer(Modifier.height(16.dp))
                    GradientButton(text = "🗺️ Yol Tarifi Al", gradient = OceanGradient, onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${m.latitude},${m.longitude}"))) }, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun MacKarti(mac: MacModel, uid: String?, ctx: Context) {
    val db = Firebase.firestore
    var acik by remember { mutableStateOf(false) }
    val katildiMi = mac.katilimcilar.contains(uid)

    GlowCard(
        glowColor = Color(0xFF667EEA),
        modifier = Modifier
            .clip(CardShape)
            .animateContentSize()
            .clickable { acik = !acik }
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(sporRengi(mac.sporTuru), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                    Text(sporEmoji(mac.sporTuru), fontSize = 28.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(mac.macAdi, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(mac.konum.ifEmpty { "Konum yok" }, color = Color.Gray, fontSize = 14.sp)
                    }
                }
                Surface(color = if (mac.oyuncuSayisi > 0) Color(0xFF11998E).copy(0.1f) else Color(0xFFEF4444).copy(0.1f), shape = RoundedCornerShape(14.dp)) {
                    Text(if (mac.oyuncuSayisi > 0) "${mac.oyuncuSayisi} kişi" else "Dolu", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (mac.oyuncuSayisi > 0) Color(0xFF11998E) else Color(0xFFEF4444))
                }
            }

            AnimatedVisibility(visible = acik, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(20.dp), tint = Color(0xFF667EEA))
                        Spacer(Modifier.width(8.dp))
                        Text(mac.tarihSaat.ifEmpty { "Tarih belirlenmedi" }, color = Color(0xFF667EEA), fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (mac.sahipId == uid) {
                            OutlinedButton(onClick = { db.collection("maclar").document(mac.id).delete() }, Modifier.weight(1f).height(48.dp), shape = DefaultShape, border = BorderStroke(1.dp, Color(0xFFEF4444)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))) {
                                Icon(Icons.Outlined.Delete, null, Modifier.size(20.dp))
                            }
                        }
                        Box(Modifier.weight(2f)) {
                            GradientButton(
                                text = if (katildiMi) "✓ Katıldın" else "🚀 Katıl",
                                gradient = if (katildiMi) Brush.linearGradient(listOf(Color.Gray, Color.LightGray)) else SecondaryGradient,
                                onClick = {
                                    if (!katildiMi && mac.oyuncuSayisi > 0) {
                                        db.collection("maclar").document(mac.id).update("oyuncuSayisi", mac.oyuncuSayisi - 1)
                                        db.collection("maclar").document(mac.id).update("katilimcilar", FieldValue.arrayUnion(uid))
                                        BildirimHelper.hatirlaticiPlanla(ctx, mac.id, mac.macAdi, mac.tarihSaat)
                                        Toast.makeText(ctx, "Katıldın! Hatırlatıcı kuruldu 🔔", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !katildiMi && mac.oyuncuSayisi > 0
                            )
                        }
                    }

                    if (mac.latitude != 0.0) {
                        Spacer(Modifier.height(12.dp))
                        GradientButton(text = "🗺️ Yol Tarifi", gradient = OceanGradient, onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${mac.latitude},${mac.longitude}"))) }, modifier = Modifier.fillMaxWidth())
                    }


                    Spacer(Modifier.height(12.dp))
                    PuanlamaButonu(etkinlikId = mac.id, katilimcilar = mac.katilimcilar, sahipId = mac.sahipId, uid = uid)
                }
            }
        }
    }
}

@Composable
fun EtkinlikOlusturDialog(onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var baslik by remember { mutableStateOf("") }
    var sporTuru by remember { mutableStateOf("") }
    var oyuncuSayisi by remember { mutableStateOf("") }
    var tarihSaat by remember { mutableStateOf("") }
    var konum by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }
    var konumAcik by remember { mutableStateOf(false) }
    val takvim = Calendar.getInstance()
    val zaman = TimePickerDialog(ctx, { _, s, d -> tarihSaat = "$tarihSaat - ${String.format("%02d:%02d", s, d)}" }, takvim.get(Calendar.HOUR_OF_DAY), takvim.get(Calendar.MINUTE), true)
    val tarih = DatePickerDialog(ctx, { _, y, a, g -> tarihSaat = "$g/${a+1}/$y"; zaman.show() }, takvim.get(Calendar.YEAR), takvim.get(Calendar.MONTH), takvim.get(Calendar.DAY_OF_MONTH))

    if (konumAcik) { KonumSecici(onSecildi = { l, a -> konum = a; lat = l.latitude; lng = l.longitude; konumAcik = false }, onKapat = { konumAcik = false }) }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ Etkinlik Oluştur", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onKapat, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)) { Icon(Icons.Default.Close, null) }
                }
                Spacer(Modifier.height(24.dp))

                GlowCard { OutlinedTextField(value = baslik, onValueChange = { baslik = it }, label = { Text("Etkinlik Adı") }, leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color(0xFF667EEA)) }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
                Spacer(Modifier.height(16.dp))

                SporSecici(secilen = sporTuru, onSecildi = { sporTuru = it })
                Spacer(Modifier.height(16.dp))

                GlowCard { OutlinedTextField(value = oyuncuSayisi, onValueChange = { oyuncuSayisi = it }, label = { Text("Katılımcı Sayısı") }, leadingIcon = { Icon(Icons.Default.People, null, tint = Color(0xFF667EEA)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
                Spacer(Modifier.height(16.dp))

                GlowCard(modifier = Modifier.clickable { tarih.show() }) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF667EEA)); Spacer(Modifier.width(12.dp)); Text(if (tarihSaat.isNotEmpty()) tarihSaat else "📅 Tarih ve Saat Seç", color = if (tarihSaat.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface) } }
                Spacer(Modifier.height(16.dp))

                GlowCard(modifier = Modifier.clickable { konumAcik = true }) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF667EEA)); Spacer(Modifier.width(12.dp)); Text(if (konum.isNotEmpty()) "📍 $konum" else "📍 Konum Seç", color = if (konum.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface) } }
                Spacer(Modifier.height(24.dp))

                GradientButton(text = "🚀 OLUŞTUR", gradient = PrimaryGradient, onClick = { if (baslik.isNotEmpty() && sporTuru.isNotEmpty()) { db.collection("maclar").add(hashMapOf("macAdi" to baslik, "sporTuru" to sporTuru, "oyuncuSayisi" to (oyuncuSayisi.toIntOrNull()?:0), "konum" to konum, "tarihSaat" to tarihSaat, "sahipId" to uid, "latitude" to lat, "longitude" to lng, "katilimcilar" to listOf(uid))); Toast.makeText(ctx, "Oluşturuldu! 🎉", Toast.LENGTH_SHORT).show(); onKapat() } }, modifier = Modifier.fillMaxWidth(), enabled = baslik.isNotEmpty() && sporTuru.isNotEmpty())
            }
        }
    }
}

@Composable
fun SporSecici(secilen: String, onSecildi: (String) -> Unit) {
    var acik by remember { mutableStateOf(false) }

    GlowCard(modifier = Modifier.clickable { acik = true }) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (secilen.isNotEmpty()) { Text(sporEmoji(secilen), fontSize = 24.sp); Spacer(Modifier.width(12.dp)) }
            else { Icon(Icons.Default.SportsBaseball, null, tint = Color(0xFF667EEA)); Spacer(Modifier.width(12.dp)) }
            Text(if (secilen.isNotEmpty()) secilen else "Spor Türü Seç", color = if (secilen.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray)
        }
    }

    if (acik) {
        Dialog(onDismissRequest = { acik = false }) {
            Surface(Modifier.fillMaxWidth().padding(24.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(24.dp)) {
                    Text("🏆 Spor Seç", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(Modifier.heightIn(max = 400.dp)) {
                        sporKategorileri.forEach { k ->
                            item { Text("${k.emoji} ${k.ad}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 12.dp)) }
                            items(items = k.sporlar) { s ->
                                Surface(onClick = { onSecildi(s); acik = false }, Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(sporEmoji(s), fontSize = 24.sp); Spacer(Modifier.width(12.dp)); Text(s, fontWeight = FontWeight.Medium) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KonumSecici(onSecildi: (LatLng, String) -> Unit, onKapat: () -> Unit) {
    val ctx = LocalContext.current
    var secilen by remember { mutableStateOf(VARSAYILAN_KONUM) }
    var adres by remember { mutableStateOf("") }
    val cameraState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(VARSAYILAN_KONUM, 15f) }

    LaunchedEffect(Unit) { if (konumIzniVarMi(ctx)) { try { LocationServices.getFusedLocationProviderClient(ctx).lastLocation.addOnSuccessListener { l -> l?.let { secilen = LatLng(it.latitude, it.longitude); adres = koordinattanAdres(ctx, secilen); cameraState.position = CameraPosition.fromLatLngZoom(secilen, 15f) } } } catch (e: SecurityException) { } } }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize()) {
            Column {
                Box(Modifier.fillMaxWidth().background(PrimaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onKapat) { Icon(Icons.Default.Close, null, tint = Color.White) }; Text("📍 Konum Seç", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) }
                        Button(onClick = { onSecildi(secilen, adres) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF667EEA)), shape = RoundedCornerShape(14.dp)) { Text("Seç", fontWeight = FontWeight.Bold) }
                    }
                }
                Box(Modifier.weight(1f)) { GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraState, onMapClick = { secilen = it; adres = koordinattanAdres(ctx, it) }, properties = MapProperties(isMyLocationEnabled = konumIzniVarMi(ctx))) { Marker(state = MarkerState(secilen)) } }
                if (adres.isNotEmpty()) { Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.LocationOn, null, tint = Color(0xFF667EEA)); Spacer(Modifier.width(12.dp)); Text(adres, fontWeight = FontWeight.Medium) } } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilEkrani(onGeri: () -> Unit) {
    val db = Firebase.firestore
    val auth = Firebase.auth
    val ctx = LocalContext.current
    val uid = auth.currentUser?.uid
    var isim by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }
    var foto by remember { mutableStateOf("") }
    var yukleniyor by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val inp = ctx.contentResolver.openInputStream(it)
            val bytes = inp?.readBytes()
            inp?.close()
            if (bytes != null && bytes.size < 500000) {
                foto = "data:image/jpeg;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                uid?.let { i -> db.collection("kullanicilar").document(i).update("profilFotoUrl", foto) }
            }
        }
    }

    LaunchedEffect(Unit) { uid?.let { db.collection("kullanicilar").document(it).get().addOnSuccessListener { d -> isim = d.getString("isim") ?: ""; telefon = d.getString("telefon") ?: ""; foto = d.getString("profilFotoUrl") ?: ""; yukleniyor = false } } ?: run { yukleniyor = false } }
    LaunchedEffect(isim, telefon) { if (!yukleniyor && uid != null) { db.collection("kullanicilar").document(uid).set(hashMapOf("isim" to isim, "telefon" to telefon, "email" to auth.currentUser?.email), SetOptions.merge()) } }

    val bitmap = remember(foto) { if (foto.isNotEmpty() && foto.startsWith("data:")) { try { val bytes = android.util.Base64.decode(foto.substringAfter("base64,"), android.util.Base64.DEFAULT); android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) } catch (e: Exception) { null } } else null }

    Scaffold(topBar = { Box(Modifier.fillMaxWidth().background(PrimaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onGeri) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Text("👤 Profilim", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) } } }) { pv ->
        Column(Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (bitmap != null) { Image(bitmap = bitmap.asImageBitmap(), contentDescription = "Profil", modifier = Modifier.size(140.dp).shadow(16.dp, CircleShape).clip(CircleShape).border(4.dp, Color.White, CircleShape), contentScale = ContentScale.Crop) }
                else { Box(Modifier.size(140.dp).shadow(16.dp, CircleShape).background(PrimaryGradient, CircleShape).border(4.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) { Text(isim.take(1).uppercase().ifEmpty { "?" }, fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White) } }
                FloatingActionButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(44.dp).offset(x = (-4).dp, y = (-4).dp), containerColor = Color.Transparent) { Box(Modifier.fillMaxSize().background(SecondaryGradient, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(22.dp)) } }
            }
            Spacer(Modifier.height(20.dp))
            Text(isim.ifEmpty { "İsimsiz" }, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text(auth.currentUser?.email ?: "", color = Color.Gray, fontSize = 16.sp)
            Spacer(Modifier.height(32.dp))
            GlowCard { OutlinedTextField(value = isim, onValueChange = { isim = it }, label = { Text("Ad Soyad") }, leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0xFF667EEA)) }, modifier = Modifier.fillMaxWidth().padding(4.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
            Spacer(Modifier.height(16.dp))
            GlowCard { OutlinedTextField(value = telefon, onValueChange = { telefon = it }, label = { Text("Telefon") }, leadingIcon = { Icon(Icons.Default.Phone, null, tint = Color(0xFF667EEA)) }, modifier = Modifier.fillMaxWidth().padding(4.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)) }
            Spacer(Modifier.height(24.dp))
            GlowCard(glowColor = Color(0xFF11998E)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(44.dp).background(SecondaryGradient, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Default.CheckCircle, null, tint = Color.White) }; Spacer(Modifier.width(16.dp)); Column { Text("Otomatik Kayıt", fontWeight = FontWeight.SemiBold); Text("Bilgileriniz otomatik kaydedilir", color = Color.Gray, fontSize = 12.sp) } } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IstatistikEkrani(onGeri: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    var katilinanEtkinlik by remember { mutableStateOf(0) }
    var olusturulanEtkinlik by remember { mutableStateOf(0) }
    var toplamMesaj by remember { mutableStateOf(0) }
    var ortalamaPuan by remember { mutableStateOf(0.0) }
    var puanSayisi by remember { mutableStateOf(0) }
    var sporDagilimi by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var rozetler by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(uid) {
        uid?.let { id ->

            db.collection("maclar").whereArrayContains("katilimcilar", id).get().addOnSuccessListener { katilinanEtkinlik = it.size(); val sporlar = mutableMapOf<String, Int>(); it.documents.forEach { d -> val spor = d.getString("sporTuru") ?: ""; sporlar[spor] = (sporlar[spor] ?: 0) + 1 }; sporDagilimi = sporlar }

            db.collection("maclar").whereEqualTo("sahipId", id).get().addOnSuccessListener { olusturulanEtkinlik = it.size() }

            db.collection("kullanicilar").document(id).get().addOnSuccessListener { d -> ortalamaPuan = d.getDouble("puan") ?: 0.0; puanSayisi = d.getLong("puanSayisi")?.toInt() ?: 0; rozetler = (d.get("rozetler") as? List<*>)?.filterIsInstance<String>() ?: listOf("yeni_uye") }
        }
    }

    Scaffold(
        topBar = { Box(Modifier.fillMaxWidth().background(GoldGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onGeri) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Text("📊 İstatistiklerim", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) } } }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatKart(Modifier.weight(1f), "🎯", "Katıldığın", katilinanEtkinlik.toString(), SecondaryGradient)
                    StatKart(Modifier.weight(1f), "✨", "Oluşturduğun", olusturulanEtkinlik.toString(), PrimaryGradient)
                }
            }

            item {
                GlowCard(glowColor = Color(0xFFFFD700)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⭐ Kullanıcı Puanın", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(String.format("%.1f", ortalamaPuan), fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                            Text(" / 5", fontSize = 20.sp, color = Color.Gray)
                        }
                        Text("$puanSayisi değerlendirme", color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            (1..5).forEach { i ->
                                Icon(
                                    imageVector = if (i <= ortalamaPuan.toInt()) Icons.Filled.Star else if (i <= ortalamaPuan + 0.5) Icons.Filled.StarHalf else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text("🏆 Rozetlerin", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(12.dp))
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rozetler.size) { i ->
                        val rozet = rozetler[i]
                        val (emoji, ad) = rozetListesi[rozet] ?: Pair("🏅", "Rozet")
                        GlowCard(glowColor = Color(0xFFFFD700)) {
                            Column(Modifier.padding(16.dp).width(100.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(emoji, fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(ad, fontWeight = FontWeight.Medium, fontSize = 12.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            if (sporDagilimi.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("🏃 Spor Dağılımın", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                }
                items(sporDagilimi.entries.toList()) { (spor, sayi) ->
                    GlowCard {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(48.dp).background(sporRengi(spor), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                                Text(sporEmoji(spor), fontSize = 24.sp)
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(spor, fontWeight = FontWeight.SemiBold)
                                LinearProgressIndicator(
                                    progress = { sayi.toFloat() / katilinanEtkinlik.coerceAtLeast(1) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                    color = Color(0xFF11998E)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("$sayi", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF11998E))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun StatKart(modifier: Modifier, emoji: String, baslik: String, deger: String, gradient: Brush) {
    GlowCard(modifier = modifier) {
        Column(Modifier.fillMaxWidth().background(gradient, CardShape).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 32.sp)
            Text(deger, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(baslik, color = Color.White.copy(0.9f), fontSize = 14.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArkadaslarEkrani(onGeri: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var arkadaslar by remember { mutableStateOf<List<KullaniciModel>>(emptyList()) }
    var gelenIstekler by remember { mutableStateOf<List<ArkadasIstegiModel>>(emptyList()) }
    var aramaMetni by remember { mutableStateOf("") }
    var aramaDialogAcik by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        uid?.let { id ->

            db.collection("kullanicilar").document(id).addSnapshotListener { d, _ ->
                val arkadasIdler = (d?.get("arkadaslar") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (arkadasIdler.isNotEmpty()) {
                    arkadasIdler.forEach { aid ->
                        db.collection("kullanicilar").document(aid).get().addOnSuccessListener { ad ->
                            val yeniArkadas = KullaniciModel(ad.id, ad.getString("isim") ?: "Kullanıcı", ad.getString("email") ?: "", ad.getString("profilFotoUrl") ?: "", ad.getDouble("puan") ?: 0.0)
                            arkadaslar = (arkadaslar + yeniArkadas).distinctBy { it.id }
                        }
                    }
                }
            }

            db.collection("arkadasIstekleri").whereEqualTo("alan", id).whereEqualTo("durum", "bekliyor").addSnapshotListener { s, _ ->
                gelenIstekler = s?.documents?.map { d -> ArkadasIstegiModel(d.id, d.getString("gonderen") ?: "", d.getString("alan") ?: "", d.getString("durum") ?: "", d.getLong("tarih") ?: 0) } ?: emptyList()
            }
        }
    }

    if (aramaDialogAcik) {
        ArkadasAraDialog(onKapat = { aramaDialogAcik = false })
    }

    Scaffold(
        topBar = { Box(Modifier.fillMaxWidth().background(SecondaryGradient).padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onGeri) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) }; Text("👥 Arkadaşlarım", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) }; IconButton(onClick = { aramaDialogAcik = true }) { Icon(Icons.Default.PersonAdd, null, tint = Color.White) } } } }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            TabRow(selectedTabIndex = tab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("👥 Arkadaşlar (${arkadaslar.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📩 İstekler")
                        if (gelenIstekler.isNotEmpty()) {
                            Spacer(Modifier.width(4.dp))
                            Surface(color = Color(0xFFEF4444), shape = CircleShape) { Text("${gelenIstekler.size}", Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontSize = 12.sp) }
                        }
                    }
                })
            }

            when (tab) {
                0 -> {
                    if (arkadaslar.isEmpty()) {
                        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(Modifier.size(120.dp).background(SecondaryGradient, CircleShape), contentAlignment = Alignment.Center) { Text("👥", fontSize = 56.sp) }
                            Spacer(Modifier.height(24.dp))
                            Text("Henüz arkadaşın yok", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Yeni arkadaşlar ekle!", color = Color.Gray)
                            Spacer(Modifier.height(24.dp))
                            GradientButton(text = "➕ Arkadaş Ekle", gradient = SecondaryGradient, onClick = { aramaDialogAcik = true }, modifier = Modifier.fillMaxWidth(0.6f))
                        }
                    } else {
                        LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(arkadaslar) { arkadas ->
                                GlowCard {
                                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(56.dp).background(PrimaryGradient, CircleShape), contentAlignment = Alignment.Center) {
                                            Text(arkadas.isim.take(1).uppercase(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(arkadas.isim, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Star, null, Modifier.size(16.dp), tint = Color(0xFFFFD700))
                                                Spacer(Modifier.width(4.dp))
                                                Text(String.format("%.1f", arkadas.puan), color = Color.Gray)
                                            }
                                        }
                                        IconButton(onClick = {
                                            uid?.let { db.collection("kullanicilar").document(it).update("arkadaslar", FieldValue.arrayRemove(arkadas.id)) }
                                            arkadaslar = arkadaslar.filter { it.id != arkadas.id }
                                            Toast.makeText(ctx, "Arkadaş silindi", Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Outlined.PersonRemove, null, tint = Color(0xFFEF4444)) }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (gelenIstekler.isEmpty()) {
                        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text("📩", fontSize = 64.sp)
                            Spacer(Modifier.height(16.dp))
                            Text("Bekleyen istek yok", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(gelenIstekler) { istek ->
                                var gonderenIsim by remember { mutableStateOf("Kullanıcı") }
                                LaunchedEffect(istek.gonderen) {
                                    db.collection("kullanicilar").document(istek.gonderen).get().addOnSuccessListener { gonderenIsim = it.getString("isim") ?: "Kullanıcı" }
                                }
                                GlowCard {
                                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(48.dp).background(AccentGradient, CircleShape), contentAlignment = Alignment.Center) {
                                            Text(gonderenIsim.take(1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Text(gonderenIsim, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                        IconButton(onClick = {
                                            uid?.let { db.collection("kullanicilar").document(it).update("arkadaslar", FieldValue.arrayUnion(istek.gonderen)) }
                                            db.collection("kullanicilar").document(istek.gonderen).update("arkadaslar", FieldValue.arrayUnion(uid))
                                            db.collection("arkadasIstekleri").document(istek.id).update("durum", "kabul")
                                            Toast.makeText(ctx, "Arkadaş eklendi! 🎉", Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF11998E), modifier = Modifier.size(32.dp)) }
                                        IconButton(onClick = {
                                            db.collection("arkadasIstekleri").document(istek.id).update("durum", "red")
                                            Toast.makeText(ctx, "İstek reddedildi", Toast.LENGTH_SHORT).show()
                                        }) { Icon(Icons.Default.Cancel, null, tint = Color(0xFFEF4444), modifier = Modifier.size(32.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArkadasAraDialog(onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var yukleniyor by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onKapat) {
        Surface(Modifier.fillMaxWidth(), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Text("👥 Arkadaş Ekle", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("E-posta adresi ile arkadaş isteği gönder", color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                GlowCard {
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, label = { Text("E-posta Adresi") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF667EEA)) },
                        modifier = Modifier.fillMaxWidth().padding(4.dp), singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onKapat, Modifier.weight(1f).height(56.dp), shape = DefaultShape) { Text("İptal") }
                    Box(Modifier.weight(1f)) {
                        GradientButton(
                            text = if (yukleniyor) "..." else "📩 Gönder",
                            gradient = SecondaryGradient,
                            onClick = {
                                if (email.isNotEmpty()) {
                                    yukleniyor = true
                                    db.collection("kullanicilar").whereEqualTo("email", email).get().addOnSuccessListener { r ->
                                        if (r.documents.isNotEmpty()) {
                                            val hedefId = r.documents[0].id
                                            if (hedefId != uid) {
                                                db.collection("arkadasIstekleri").add(hashMapOf("gonderen" to uid, "alan" to hedefId, "durum" to "bekliyor", "tarih" to System.currentTimeMillis()))
                                                Toast.makeText(ctx, "İstek gönderildi! 📩", Toast.LENGTH_SHORT).show()
                                                onKapat()
                                            } else {
                                                Toast.makeText(ctx, "Kendinize istek gönderemezsiniz", Toast.LENGTH_SHORT).show()
                                                yukleniyor = false
                                            }
                                        } else {
                                            Toast.makeText(ctx, "Kullanıcı bulunamadı", Toast.LENGTH_SHORT).show()
                                            yukleniyor = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = email.isNotEmpty() && !yukleniyor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PuanlamaDialog(etkinlikId: String, katilimcilar: List<String>, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var katilimciAdlari by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var puanlar by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var yorumlar by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var yukleniyor by remember { mutableStateOf(false) }

    LaunchedEffect(katilimcilar) {
        katilimcilar.filter { it != uid }.forEach { kid ->
            db.collection("kullanicilar").document(kid).get().addOnSuccessListener { d ->
                katilimciAdlari = katilimciAdlari + (kid to (d.getString("isim") ?: "Kullanıcı"))
            }
        }
    }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ Katılımcıları Puanla", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onKapat) { Icon(Icons.Default.Close, null) }
                }

                Text("Birlikte oynadığın kişileri değerlendir", color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                LazyColumn(Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(katilimcilar.filter { it != uid }) { kid ->
                        val isim = katilimciAdlari[kid] ?: "Kullanıcı"
                        GlowCard {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(48.dp).background(GoldGradient, CircleShape), contentAlignment = Alignment.Center) {
                                        Text(isim.take(1).uppercase(), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(isim, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                }
                                Spacer(Modifier.height(12.dp))

                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    YildizPuanlama(
                                        puan = puanlar[kid] ?: 0,
                                        onPuanDegisti = { puanlar = puanlar + (kid to it) }
                                    )
                                }

                                Spacer(Modifier.height(12.dp))


                                OutlinedTextField(
                                    value = yorumlar[kid] ?: "",
                                    onValueChange = { yorumlar = yorumlar + (kid to it) },
                                    placeholder = { Text("Yorum (opsiyonel)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = DefaultShape,
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                GradientButton(
                    text = if (yukleniyor) "Kaydediliyor..." else "⭐ Puanları Kaydet",
                    gradient = GoldGradient,
                    onClick = {
                        yukleniyor = true
                        puanlar.forEach { (kid, puan) ->

                            db.collection("puanlamalar").add(hashMapOf(
                                "puanlayanId" to uid,
                                "puanlananId" to kid,
                                "etkinlikId" to etkinlikId,
                                "puan" to puan,
                                "yorum" to (yorumlar[kid] ?: ""),
                                "tarih" to System.currentTimeMillis()
                            ))


                            db.collection("kullanicilar").document(kid).get().addOnSuccessListener { d ->
                                val eskiPuan = d.getDouble("puan") ?: 0.0
                                val eskiSayi = d.getLong("puanSayisi")?.toInt() ?: 0
                                val yeniSayi = eskiSayi + 1
                                val yeniPuan = ((eskiPuan * eskiSayi) + puan) / yeniSayi
                                db.collection("kullanicilar").document(kid).update(
                                    mapOf("puan" to yeniPuan, "puanSayisi" to yeniSayi)
                                )
                            }
                        }
                        Toast.makeText(ctx, "Puanlar kaydedildi! ⭐", Toast.LENGTH_SHORT).show()
                        onKapat()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = puanlar.isNotEmpty() && !yukleniyor
                )
            }
        }
    }
}

@Composable
fun PuanlamaButonu(etkinlikId: String, katilimcilar: List<String>, sahipId: String, uid: String?) {
    var puanlamaAcik by remember { mutableStateOf(false) }
    val db = Firebase.firestore
    var zatenPuanladi by remember { mutableStateOf(false) }

    LaunchedEffect(etkinlikId, uid) {
        uid?.let { db.collection("puanlamalar").whereEqualTo("puanlayanId", it).whereEqualTo("etkinlikId", etkinlikId).get().addOnSuccessListener { zatenPuanladi = !it.isEmpty } }
    }

    if (puanlamaAcik) {
        PuanlamaDialog(etkinlikId = etkinlikId, katilimcilar = katilimcilar, onKapat = { puanlamaAcik = false })
    }

    if (katilimcilar.contains(uid) && katilimcilar.size > 1 && !zatenPuanladi) {
        OutlinedButton(
            onClick = { puanlamaAcik = true },
            modifier = Modifier.fillMaxWidth(),
            shape = DefaultShape,
            border = BorderStroke(2.dp, Color(0xFFFFD700))
        ) {
            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700))
            Spacer(Modifier.width(8.dp))
            Text("Katılımcıları Puanla", color = Color(0xFFFFD700))
        }
    } else if (zatenPuanladi) {
        Surface(color = Color(0xFFFFD700).copy(0.1f), shape = DefaultShape, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFFFFD700))
                Spacer(Modifier.width(8.dp))
                Text("Puanlandı ✓", color = Color(0xFFFFD700))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KullaniciProfilDialog(kullaniciId: String, onKapat: () -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    val ctx = LocalContext.current
    var kullanici by remember { mutableStateOf<KullaniciModel?>(null) }
    var puanlamalar by remember { mutableStateOf<List<PuanlamaModel>>(emptyList()) }
    var arkadasMi by remember { mutableStateOf(false) }

    LaunchedEffect(kullaniciId) {
        db.collection("kullanicilar").document(kullaniciId).get().addOnSuccessListener { d ->
            kullanici = KullaniciModel(
                d.id,
                d.getString("isim") ?: "Kullanıcı",
                d.getString("email") ?: "",
                d.getString("profilFotoUrl") ?: "",
                d.getDouble("puan") ?: 0.0,
                d.getLong("puanSayisi")?.toInt() ?: 0,
                (d.get("arkadaslar") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                d.getLong("katilinanEtkinlikler")?.toInt() ?: 0,
                (d.get("rozetler") as? List<*>)?.filterIsInstance<String>() ?: listOf("yeni_uye")
            )
        }
        db.collection("puanlamalar").whereEqualTo("puanlananId", kullaniciId).get().addOnSuccessListener { s ->
            puanlamalar = s.documents.map { d -> PuanlamaModel(d.id, "", kullaniciId, "", d.getLong("puan")?.toInt() ?: 0, d.getString("yorum") ?: "") }
        }
        uid?.let { db.collection("kullanicilar").document(it).get().addOnSuccessListener { d -> arkadasMi = (d.get("arkadaslar") as? List<*>)?.contains(kullaniciId) ?: false } }
    }

    Dialog(onDismissRequest = onKapat, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp), shape = CardShape, color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                kullanici?.let { k ->

                    Box(Modifier.size(100.dp).background(PrimaryGradient, CircleShape), contentAlignment = Alignment.Center) {
                        Text(k.isim.take(1).uppercase(), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(k.isim, fontSize = 24.sp, fontWeight = FontWeight.Bold)


                    Row(verticalAlignment = Alignment.CenterVertically) {
                        (1..5).forEach { i ->
                            Icon(
                                imageVector = if (i <= k.puan.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(String.format("%.1f", k.puan), fontWeight = FontWeight.Bold)
                        Text(" (${k.puanSayisi})", color = Color.Gray)
                    }

                    Spacer(Modifier.height(24.dp))


                    Text("🏆 Rozetler", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(k.rozetler.size) { i ->
                            val (emoji, _) = rozetListesi[k.rozetler[i]] ?: Pair("🏅", "")
                            Surface(color = Color(0xFFFFD700).copy(0.1f), shape = CircleShape) {
                                Text(emoji, Modifier.padding(12.dp), fontSize = 24.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))


                    if (kullaniciId != uid) {
                        if (arkadasMi) {
                            OutlinedButton(onClick = {
                                uid?.let { db.collection("kullanicilar").document(it).update("arkadaslar", FieldValue.arrayRemove(kullaniciId)) }
                                arkadasMi = false
                                Toast.makeText(ctx, "Arkadaşlıktan çıkarıldı", Toast.LENGTH_SHORT).show()
                            }, Modifier.fillMaxWidth(), shape = DefaultShape) {
                                Icon(Icons.Default.PersonRemove, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Arkadaşlıktan Çıkar")
                            }
                        } else {
                            GradientButton(
                                text = "👥 Arkadaş Ekle",
                                gradient = SecondaryGradient,
                                onClick = {
                                    db.collection("arkadasIstekleri").add(hashMapOf("gonderen" to uid, "alan" to kullaniciId, "durum" to "bekliyor", "tarih" to System.currentTimeMillis()))
                                    Toast.makeText(ctx, "İstek gönderildi! 📩", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onKapat) { Text("Kapat") }

                } ?: CircularProgressIndicator()
            }
        }
    }
}