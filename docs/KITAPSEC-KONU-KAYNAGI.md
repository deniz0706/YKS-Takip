# Kitapseç kaynaklı 2.0 Öğrenme Merkezi katalogu

Kaynak: https://www.kitapsec.com/blog/yks-2023-tyt-ayt-konulari-112.html

Erişim: 17 Ekim 2026. URL'de '2023' geçmesine rağmen sayfanın görünen
başlığı **YKS 2027 TYT AYT Konuları**.

## Katalog ilkeleri
- TYT: 10 ayrı ders grubu; Tarih, Coğrafya, Felsefe, Din ayrı ayrı.
- AYT: Sayısal / Eşit Ağırlık / Sözel alan filtresi aynen korunur.
- Sayfanın **konu listeleri** esas alınır. Paragraf gibi bir ana başlık
  yalnızca bir satır gösterilir; kaynakta alt başlık verildiyse açılır.
- Bazı derslerde lise sınıf düzeyleri de listelenmiştir. Öğrenme ekranı
  konuları ders altında birleştirir, aynı başlığı iki kez tekrarlamaz.
- **AYT Tarih-2** için bu sayfada ayrı konu listesi olmadığından sayfadaki
  AYT Tarih soru dağılımında adı geçen tarih konu başlıkları alınmıştır.
- **AYT Edebiyat** 9–12. sınıf metin türleri ile aynı sayfanın
  tarihsel soru dağılımı bölümünde açıkça adlandırdığı edebî dönemleri
  birlikte içerir. Bunlar iki farklı kaynak bölümünden alınmıştır.
- **AYT Kimyasal Tepkimelerde Hız**, sitedeki ana konu listesinde
  atlanmıştır ancak soru dağılımında açıkça bulunur; bu başlık da
  *tablodan tamamlayıcı* olarak eklenmiştir. Bu durum sitenin kendi
  içinde eksikleri olduğunu gösterir; resmî sınav kapsamı iddia edilmez.
- Bu site MEB veya ÖSYM'nin resmi sınav kapsamı belgesi değildir.

## Veri güvenliği
- İlk açılışta eski yerel Öğrenme Merkezi kayıtlarının ve soruların ham
  JSON kopyası uygulama içinde ayrı anahtarlarda saklanır.
- Açık ve kesin isim eşleşmeleri yeni ünite kimliğine taşınır.
- Belirsiz/yanlış birleştirme tehlikesi taşıyan kayıtlar **Eski Kayıtlar**
  adlı ayrı derste tutulur; bağlı soru fotoğrafları silinmez.
- Soruların kendi kimlikleri, fotoğraf dosyası adları ve mevcut durumları
  korunur; yalnızca güvenli eşleşmelerin konu kimliği değiştirilir.
- Uygulama verisini silmek yine bu yerel korumayı siler. Android'i
  kaldırmadan önce güncel JSON ve Öğrenme ZIP yedeği saklanmalıdır.
- Eski ZIP içe aktarımları aynı dönüşümden geçirilir.

## Otomatik kontroller
- TYT/AYT ders grupları ve seçilmiş derslerin kaynak konu sayısı.
- Paragraf ve Problemler'in doğru alt konu başlıkları.
- Bütün açılır grupların gerçek üniteye bağlı olması.
- Belirsiz konu ve fotoğraf referanslarının arşive alınması.
- CI aynı APK imza anahtarıyla derleme ve sertifika SHA-256 doğrulaması.

**Sınır:** Bu doğrulama, kullanıcı tarafından gönderilen Kitapseç
sayfasındaki görünür konu başlıklarını esas alır; kaynak içindeki
çelişkili veya eksik bölümler bağımsız resmî doğrulama değildir.
