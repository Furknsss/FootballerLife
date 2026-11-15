# Sonraki geliştirme işleri

Bu değişiklik yaşayan dünya altyapısının ve temel oynanabilir sahnelerin ilk sürümüdür.
Ana yönergenin bütün AAA sunum ve futbol dünyası kapsamının tamamlandığı anlamına gelmez.

1. NPC görselleri: mevcut altı gerçekçi karakter katmanını daha geniş, kimliğe bağlı bir
   oyuncu/NPC varyant setine genişlet. Şimdiki iki oyuncu görünümü bütün saç, yüz ve kıyafet
   seçeneklerini temsil etmez. Yaş, duygu ve poz varyantları için görsel QA ekle.
   Mevcut oyuncu görünümünü ve `CharacterIdentity.id` değerlerini koru.
2. Sahne yönetimi: çok düğümlü diyalog grafiği, zaman çizelgesi editörü, koridor/kapı girişi,
   omuz üstü çekimler, odak geçişleri ve her sahneye özel koreografi ekle. Kamera ve animasyon
   tanımlarını doğrulayan içerik testleri yaz.
3. Ses: mekâna özgü lisanslı/özgün ambiyans, kapı ve imza efektleri, ses yaşam döngüsü ve
   erişilebilirlik ayarlarını tamamla. Bu sürümde mevcut kısa efektler vardır; mekân ambiyansı
   ve seslendirme tamamlanmış değildir.
4. Gemini: HTTPS sunucusunu gerçek bir hesaba dağıt, doğrulanmış model ID'si ve sunucu
   anahtarını yapılandır, gerçek oturum yönetimine bağla. Canlı API ile kota, gecikme ve
   kesinti testleri yap. API testlerinin sağlayıcı yanıtları şu an kontrollü taklittir.
5. Transfer: oyuncu/klüp serbestlik statüsü, ön sözleşme, kiralık dönüş tarihi, kulüp satış
   onayı, federasyon tescil limitleri, yaş/uyruk istisnaları ve teklifli forma numarası
   çakışmalarını ekle. `LeagueRegistrationRules` için kaynak tarihli gerçek DataPack adaptörü
   isteğe bağlı kalmalı. Şimdiki tarihler resmî olarak sunulmamalı.
6. Sözleşme: pazarlıkta kaydedilen forma tercihi ve serbest kalma maddesini sonraki kariyer
   kararlarında yürürlüğe sok; bonus/prim ve kadro rolü taahhütlerinin sezon içi takip motorunu
   ekle. Desteklenmeyen kiralık/bedelsiz işlemleri arayüzde açma.
7. Sağlık: kişiselleştirilmiş sakatlık türleri, ikinci muayene raporu, kademeli kontrollü
   dönüş ve riskli erken dönüşü ayrı kurallarla geliştir. Mevcut günlük iyileşme ve tam yük
   engelini koru. Sağlık sisteminin kurgu olduğunu belirt.
8. NPC sözleri: tarihli beklentilerin otomatik değerlendirmesi, takım arkadaşı/aile gündemi,
   farklı karakterlerin birbirinden haberdarlığı ve yeni teknik direktör seçimini ekle.
   Önceki hocanın özel hafızasını yeni hocaya aktarma.
9. Dünya: sezon sonu/sezon başlangıcı, terfi/küme düşme, milli takım ve ödül törenleri için
   mevcut maç ve kariyer verisinden üretilen olayları geliştir. Sabit günlere bağlı eski
   senaryo olaylarını koşul ve tekrar aralığı olan olay tanımlarına taşı.
10. Kayıt: kullanıcı cihazındaki gerçek v1/v2/v3 kariyerlerin anonimleştirilmiş test
    örnekleriyle migrasyon matrisi oluştur. Büyük kayıtlar için normalize Room tabloları,
    olay arşivleme ve kayıt boyutu sınırlarını değerlendir. Yuva kayıtlarını silme.
11. Performans: fiziksel 60/90/120/144 Hz cihazlarda kare süreleri, sıcaklık, pil ve bellek
    ölç. Özellikle sahne ilk görsel yüklemesini, kalabalık telefon içeriklerini ve uzun
    oturumları profille. Emülatör sonucunu fiziksel tablet 90 FPS garantisi diye sunma.
12. DataPack dünya adaptörü: verified source packs from providers/federations must supply
    complete gameplay fields before imported leagues/clubs/players can replace the current
    `WorldDataCatalog` in career setup. Implement pack-version selection in the picker,
    record completeness/license/source audits, and map full squads/contracts without
    manufacturing missing ratings or media. The 2026/27 fixture currently proves import
    mechanics only (one TFF competition and two clubs); it is not world coverage.

Her madde için önce mevcut testleri çalıştır; kod, kabul ölçütü ve gerçek test sonucunu
birlikte kaydet. Çalışan modülü değiştirmeden yeni kapsamın durumunu açıkça belirt.

