# Football Life — Living World

Mevcut Kotlin / Jetpack Compose Android futbolcu kariyer oyunu üzerine geliştirilmiştir.
Mevcut hibrit 2D maç motoru, telefon uygulamaları, kariyer arşivi ve kayıt yuvaları korunur.

## Yerel Android kurulumu

- JDK 21 (Android 36 Robolectric testleri için gereklidir).
- Android SDK: projedeki `compileSdk` ile uyumlu platform ve build tools.
- `ANDROID_HOME` veya git dışında tutulan `local.properties` içinde `sdk.dir`.
- Gradle Wrapper depoya eklenmiştir: Windows'ta `gradlew.bat`, diğer sistemlerde `./gradlew`.

AI Studio'nun imza ayarları korunmuştur. Yerel debug derlemesi için, yalnızca bu çalışma
kopyasında bir test anahtarı oluştur:

```powershell
keytool -genkeypair -keystore debug.keystore -storepass android -keypass android -alias androiddebugkey -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug --no-configuration-cache
```

`debug.keystore` ve `.env` Git'e eklenmez. Yerel test anahtarı AI Studio anahtarıyla imzalanmış
bir uygulamanın üzerine güncelleme yapmak için kullanılamaz. Mevcut cihazdaki kariyerini
korumak için güncellemede aynı imza anahtarı kullanılmalıdır.

Bağlı bir Android test cihazında:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest --no-configuration-cache
```

## Oyun içi akış

Hayatım bölümündeki günlük program gerçek görev durumunu gösterir. Tamamlanan görevler
aktif listeden çıkar ve geçmişte saklanır. Teknik direktör görüşmesi, sağlık kontrolü,
transfer, maç öncesi/sonrası, basın ve aile buluşmaları ortak sahne motorunu kullanır.
Görüşmeyi ertelemek kontrol noktasını saklar; özet geçişi oyuncu adına karar vermez.

Transfer teklifindeki kabul işlemi önce menajer görüşmesini açar. Anlaşma → sağlık kontrolü
→ sözleşme → hedef ligin tescil kontrolü tamamlanmadan kulüp değişmez. Transfer pencereleri
resmî takvim iddiası taşımayan, lig ve sezon bazında değiştirilebilen oyun ayarlarıdır.

## İsteğe bağlı Gemini diyaloğu

Yerel bağlamsal diyalog varsayılan olarak çalışır. Canlı Gemini hizmeti ayrıca kurulmalıdır;
bu depoda çalışan bir üretim sunucusu veya API anahtarı bulunmaz.

`server/` Node 22+ ile ek paket gerektirmeyen bir diyalog geçididir:

```powershell
cd server
node --test dialogue-server.test.mjs
# GEMINI_API_KEY, GEMINI_MODEL ve en az 32 karakterlik DIALOGUE_SESSION_SECRET
# yalnızca sunucu ortamında ayarlanır.
node dialogue-server.mjs
node issue-session.mjs oyuncu-kimligi
```

Sunucu loopback üzerinde başlar; Android erişimi için HTTPS reverse proxy gerekir.
Oluşturulan bir saatlik oturum kodu ve HTTPS `/dialogue` adresi oyun ayarlarındaki isteğe
bağlı bağlantı bölümüne girilir. Oturum kodu bellekte tutulur ve uygulama kapanınca silinir.
Gemini anahtarı APK'ya konmaz. Hata, kota, çevrimdışı durum veya geçersiz yanıtta yerel
cevap korunur. Model çıktısı hiçbir ekonomik veya kariyer değişikliğini doğrudan uygulayamaz.

## Teknik belgeler

- `docs/IMPLEMENTATION_STATUS.md`: doğrulanan kapsam ve gerçek sınırlamalar.
- `docs/CONTINUATION.md`: sonraki geliştirme turu için somut işler.
- `docs/SCENE_ASSETS.md`: özgün görseller ve üretim istemleri.
- `docs/DATA_PACKS.md`: JSON/CSV/ZIP şeması, doğrulama ve 2026/27 örnek paket kapsamı.

