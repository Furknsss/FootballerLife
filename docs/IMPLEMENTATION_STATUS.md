# Football Life — Living World teknik teslim raporu

Tarih: 15 Kasım 2026. Başlangıç: `main`, `91f7323c73e9b91948590a437c2b11b352e1f8e2`.

Bu teslim mevcut Android oyunu üzerinde çalışan ilk Living World sürümüdür. Ana yönergedeki
bütün AAA sunum, futbol dünyası ve federasyon kurallarının tamamlandığı iddia edilmez.
Tamamlanan kapsam ve kalan işler aşağıda ayrı belirtilmiştir.

## 1. Uygulanan oyun sistemleri

`LivingWorldEngine` görevleri, bildirimleri, sahne kontrol noktalarını, NPC kimliklerini ve
olayların tekrar uygulanmasını aynı kariyer durumu üzerinden yönetir. `GameClock` gerçek
takvim hesapları kullanır; 30 günlük ay varsayımı kaldırılmıştır. Antrenman, maç, sağlık,
transfer ve basın sonuçları mevcut kariyer motoruna bağlanmıştır. Mevcut 2D maç motoru ve
telefon uygulamaları korunmuştur.

## 2. Kullanılabilir sahneler

Ortak Compose sahne ekranı teknik direktör, sağlık kontrolü, menajer/transfer, maç öncesi,
maç sonrası, basın, aile buluşması ve imza akışlarını sunar. Kamera yakınlaşması/kayması,
karakter katmanları, nefes/bekleme hareketleri, giriş animasyonu, serbest metin ve karar
ekranı vardır. Özet geçişi oyuncu adına karar vermez. Ertelenen görüşmeler ayrı kontrol
noktalarıyla saklanır; başka bir görüşmeden sonra devam edilebilir.

Altı temel sahnenin Android ekran ve dokunma testleri vardır. İmza ve maç sonrası
sunumları aynı motoru kullanır; bunların ayrı görsel kabul testleri henüz kapsamda değildir.

## 3. Görev yaşam döngüsü

Görev durumları ayrı veri modeliyle saklanır. Tamamlanan antrenman/görüşme/sağlık/transfer
aşamaları aktif listeden çıkar, geçmiş ve olay kaydında kalır. Geçersiz veya süresi dolan
görüşmeler açılmaz. Rehabilitasyon günlük programa bağlanır. Görev tamamlamak bütün
mesajları okunmuş yapmaz; bildirim durumu ile sohbetin okunmamış sayısı ayrı yönetilir.

## 4. NPC hafızası ve serbest konuşma

Karakter kimliği, rol, kulüp, görsel kimlik, önemli anılar ve sahne konuşmaları kalıcıdır.
Hafıza getirme ilgili karakterin kimliğine, konuya ve oyun tarihine göre sınırlı sayıda
kayıt seçer. Yeni hocaya önceki hocanın kişisel hafızası aktarılmaz; eski NPC kayıtları
arşivlenir. Yerel diyalog gerçek kadro, maç, enerji, sakatlık ve transfer verisini kullanır.
Serbest yazılan kelimeler kendi başına para harcatmaz veya ilk 11/maaş garantisi vermez.

Yerel yanıtlar sınırlı kurallardan üretilir; sınırsız doğal dil anlayışı veya bütün NPC'lerin
bağımsız günlük yaşam simülasyonu tamamlanmış değildir.

## 5. Gemini bağlantısının gerçek durumu

Android HTTPS istemcisi ve Node sunucu geçidi uygulanmıştır. Gemini anahtarı yalnızca
sunucuda kalır. Geçici imzalı oturumlar, kota, zaman aşımı, yanıt doğrulama ve yerel yedek
yanıt vardır. AI çıktısı ekonomi veya kariyer durumunu değiştiremez. Geç gelen yanıt,
başka kariyere/güne/görüşmeye uygulanmaz.

Canlı Gemini hesabı, model ve HTTPS dağıtımı yapılandırılmadı. Gerçek sağlayıcı çağrısı
yapılmadı. Sunucu testlerindeki sağlayıcı yanıtları kontrollü test yanıtlarıdır; canlı
entegrasyon testi olarak raporlanmaz.

## 6. Transfer ve pazarlık

Lig/sezon bazında yapılandırılabilen tescil pencereleri hedef lig için kontrol edilir.
Varsayılan tarihler açıkça oyun yapılandırmasıdır; resmî federasyon takvimi değildir.
Teklif, pazarlık, sağlık kontrolü, imza ve tescil ayrı aşamalardır. Finansal sınırlar,
teklifin varlığı, geçerliliği ve tekrar işleme kontrol edilir. Pazarlık en fazla üç turdur;
kulüp karşı teklif verebilir veya çekilebilir. İlk teklif ücret sınırının referansı olarak
saklanır; güncel şartlar bütün teklif ekranlarına yansıtılır.

Kulüp, sözleşme, imza parası, kadro, haberler ve kariyer geçmişi birlikte güncellenir.
Önceki kulüp/NPC arşivi korunur. Kiralık dönüşü, serbest oyuncu istisnaları, ön sözleşme,
federasyon kadro limitleri ve bütün satış onayı kuralları henüz tamamlanmamıştır.
Forma tercihi ve serbest kalma maddesi pazarlık kaydında tutulur; sonraki kariyer
eylemlerindeki bütün hükümleri henüz uygulanmaz.

## 7. Sağlık

Enerji, yorgunluk, antrenman/maç yükü, önceki sakatlıklar ve maç verisi risk hesabını
etkiler. Sakatlık, sağlık görevi ve rapor kaydı oluşturur. Fizyoterapi ve daha güvenli
dönüş planı seçilebilir; iyileşme kariyer günleriyle ilerler. Sakatken tam yükte antrenman
engellenir. Aynı gün tekrar fizyoterapiyle sınırsız iyileşme sağlanmaz.
Bu kurgusal oyun modelidir; ayrıntılı tıbbi teşhis ve kademeli erken dönüş sistemi değildir.

## 8. Telefon ve takvim

Görüşme görevleri takvim ve mesajlara bağlanır. Transfer/aile davetleri gelen arama
üretebilir; kabul sahneyi açar, erteleme görevi korur, ret daveti kapatır. Süresi dolmuş
arama bağlantıları sonuç uygulamaz. Sohbet açıldığında yalnızca ilgili sohbet okunur.

## 9. Kayıt güvenliği

Kariyer JSON şeması v4'e varsayılan alanlarla geçer; Room tablosu silinmez. Yıkıcı migrasyon
geri dönüşü kaldırılmıştır. Bir kariyerin ilişkili değişiklikleri tek Room kaydı ve
transaction içinde yazılır. Son geçerli yedek korunur; bozuk ana kayıt yedekten kurtarılır.
ViewModel kayıt/silme kuyruğu işlem sırasını korur; yükleme bekleyen kayıtları bekler.
Mevcut oyuncu, sözleşme, para ve geçmiş migrasyon testlerinde korunur.

Gerçek kullanıcı cihazındaki bütün eski kayıt çeşitleri elde olmadığı için kapsamlı
saha migrasyon matrisi henüz oluşturulmamıştır.

## 10. Gerçekten çalıştırılan testler

- Son DataPack değişikliğiyle 38 JVM/Robolectric testi geçti, hata yok. Buna JSON/CSV/ZIP
  parse/bozulma ve ID doğrulaması, zorunlu alan/missing-rate raporu, örnek TFF paketinin
  Room'a kurulması, iki sürümün yan yana saklanması ve kariyer Room kaydının korunması dahildir.
- Önceki Living World tesliminde altı sahne ve gerçek Activity/ViewModel/Room kariyer
  akışı için 8 Android emülatör testi ile 6 Node HTTP testi de geçmişti. Bu son DataPack
  eklemesinden sonra bunlar yeniden çalıştırılmadı.
- 8 Android emülatör testi: altı temel sahne, gerçek MainActivity/ViewModel/Room akışı
  ve uygulama bağlamı. Metin girişi, kaydırma, karar, görev kapatma ve ödeme kontrolleri.
- 6 Node sunucu testi: imzalı oturum/sona erme, yetkilendirme, kota, yapılandırma yokluğu,
  yapılandırılmış yanıt ve bağlantı hatası. Gerçek Gemini çağrısı içermez.

Test adları ve kaynakları depodadır. Android ekran görüntüleri ayrıca teslim edilir.

## 11. Derleme ve lint

JDK 21 ve yerel Android SDK ile debug APK üretildi. Standart Gradle Wrapper eklendi.
`testDebugUnitTest` ve `lintDebug` çalıştırıldı; lint sonucu 0 hata, 72 uyarıdır. Uyarıların
tam dökümü teslim paketindedir. AI Studio imza yapılandırması değiştirilmedi; yerel test
anahtarı Git'e eklenmez. Mevcut kurulumun kayıtlarını korumak için aynı imzayla güncelleme
gerekliliği README'de açıklanır.

## 12. Görsel kalite

Dört özgün mekân ve altı şeffaf gerçekçi karakter katmanı gerçek PNG varlıkları olarak
eklendi. Yerel Barlow Condensed ve Manrope kullanılır. Bunlar gerçek kişilerin fotoğrafı
değildir. Karakter seti sınırlıdır; bütün yüz/saç/poz/duygu seçenekleri ve dudak eşleme
tamamlanmış değildir. Koridor/kapı koreografisi, bütün kamera türleri ve mekâna özgü
ambiyans/seslendirme sonraki aşamadadır. Mevcut kısa oyun efektleri korunmuştur.

## 13. Yenileme hızı ve performans

Cihazın aynı çözünürlükte desteklediği ekran modları okunur. Otomatik veya desteklenen
sabit hedef seçilebilir; grafik kalitesi ayrı seçilir. Ana pencere ve sinematik pencere
Android yenileme hızı tercihlerini uygular. Maç çizimi sabit simülasyon adımından ayrı
interpolasyon kullanır; fazla çizim karesi oyun zamanını hızlandırmaz.

Doğrulama yazılımsal GPU kullanan Android emülatöründe yapıldı. Fiziksel 90/120/144 Hz
cihazda sürdürülebilir FPS, kare bütçesi, sıcaklık ve pil ölçümü yapılmadı. 90 FPS garantisi
verilmez. Test sonunda süreç kapandığından alınan gfxinfo/meminfo denemesi kullanılabilir
bir performans ölçümü üretmedi; sayısal performans sonucu uydurulmamıştır.

## 14. Tamamlanmayan ana kapsam

AAA karakter animasyonu ve bütün sahne koreografileri; canlı Gemini dağıtımı; kapsamlı
federasyon/kiralık/ön sözleşme kuralları; tüm NPC hedef/sözlerinin otomatik takibi; gelişmiş
sağlık aşamaları; sezon/milli takım/ödül yönetimi; fiziksel cihaz performans profili.
Tam dünya/oyuncu/medya verisi ve sağlayıcı entegrasyonu tamamlanmadı. Doğrulanmış TFF
örnek paketinde yalnızca 1 lig ve fikstürde adı geçen 2 kulüp vardır. Yeni kariyer seçilen
`packId@version` değerini saklar; playable kariyer lig/kulüp üreticisi henüz imported
records yerine legacy `WorldDataCatalog` kullanır. Bu veri adaptörü sonraki kapsamdır.

## 15. Devam dosyası ve teknik referanslar

Küçük ve somut sonraki işler `docs/CONTINUATION.md` içinde kayıtlıdır. Görsel üretim
istemleri `docs/SCENE_ASSETS.md` ve `docs/CHARACTER_ASSETS.json` içindedir.

Uygulanan platform yaklaşımının referansları:
[Android yenileme hızı tercihleri](https://developer.android.com/media/optimize/performance/frame-rate),
[Android adaptif yenileme](https://developer.android.com/develop/ui/views/animations/adaptive-refresh-rate),
[Gemini generateContent](https://ai.google.dev/api/generate-content),
[Gemini yapılandırılmış çıktı](https://ai.google.dev/gemini-api/docs/structured-output).

## 16. World DataPack altyapısı (15 Kasım 2026)

Kariyer DB'den ayrı `football_life_world_data.db` Room veritabanı, 12 modül şeması, stable
internal IDs, provider external-ID eşlemeleri, checksum/provenance alanları, JSON/CSV/ZIP
import doğrulaması, güvenli ZIP açma, ID tabanlı medya dosyası saklama ve rapor ekranı
eklendi. Android belge seçici JSON/CSV/ZIP alır. Android yeni paketleri `packId@version`
anahtarıyla ayrı saklar ve bundan sonra başlayacak kariyere seçebilir. Mevcut kariyerlerin
otomatik güncellenmesi yoktur.

Gerçek fixture: [TFF 2026-2027 Süper Lig fikstür listesi](https://www.tff.org/Default.aspx?macId=283594&pageId=198).
Oyuncu, değerleme, kadro, logo veya portre fixture'a eklenmedi; veri kapsamı bilerek küçük
tutuldu. JSON ve ZIP örnekleri `data-packs/examples/` altındadır. Şema ve checksum
sözleşmesi `docs/DATA_PACKS.md` içindedir.

