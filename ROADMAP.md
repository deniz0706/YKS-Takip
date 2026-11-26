# YKS-Takip — Ürün Yol Haritası

Bu dosya, uygulamayı geliştirirken kapsamın ve teknik kararların kaybolmaması için tutulur. Her özellik ayrı dalda geliştirilir; test ve CI doğrulaması olmadan `main` dalına alınmaz.

## Tamamlanan temel çalışmalar

- [x] Ana ekranın yeniden düzenlenmesi ve ortak Compose tasarım bileşenleri.
- [x] Haftalık çalışma raporu ve geçmiş deneme analizlerinin doğruluk iyileştirmeleri.
- [x] Sıralama tahmin motorunun ayrı tutulması ve regresyonlardan korunması.
- [x] TYT 165 / AYT 180 dakika sınav zamanlama hesaplama çekirdeği ve testleri.
- [x] Kalıcı TYT/AYT sınav modu: başlat, duraklat, devam et, sıfırla ve ders geçişi işaretleri. (PR #30)

## Aktif geliştirme

- [x] **AI Koç → çalışma planı eylemi** (PR #31): Yapılandırılmış öneriyi doğrula, çakışmaları kontrol et ve yalnızca kullanıcı onayından sonra haftalık plana kaydet.
- [x] **Deneme → konu → hata/tekrar defteri** (PR #33): Etiketleri benzersiz kazanımlara bağla, belirsiz eşleşmeleri engelle, deneme bağlamını tekrar kartına ekle ve aynı etiketi ikinci kez içe aktarma.
- [x] **Aralıklı tekrar bildirimleri** (PR #35): 1–3–7–21 günlük tekrar aralığı, WorkManager ve kullanıcı kontrollü bildirim izni.
- [x] **Ana ekran çalışma widget’ı** (PR #36): Bugünkü soru, süre ve hedefi mevcut kayıt depolarından gösterir.
- [x] **Deneme detay ekranı Compose geçişi** (PR #38): Ortak tasarım sistemi ve zayıf konudan öğrenme merkezine doğrudan bağlantı.
- [x] **Tek dosyalık tam yedek** (PR #42): Denemeler, çalışma geçmişi, öğrenme kazanımları, soru kayıtları ve fotoğraflar; doğrulamalı ZIP ve birleştirerek geri yükleme.

## Öncelikli geliştirme sırası

### 0. Kronometre ve sınav modu düzeltmeleri — tamamlandı (PR #44)
- [x] Branş süresini 60 dakikaya sabitleme; 1–600 dakika arasında kullanıcı tanımlı süre.
- [x] TYT'nin dört test grubu ve AYT Sayısal / Eşit Ağırlık / Sözel alanları.
- [x] Ayrı sınav kartı yerine mevcut odak kronometresinde mod seçimi.
- [x] Duraklatma, kaldığı yerden devam etme ve sıfırlama; mod değiştirirken diğer sayacı duraklatma.


### 0b. Tek kronometre ve günlük çalışma kaydı — PR #50
- [x] Odak, TYT, AYT ve branş modlarını aynı elapsed/running sayacına bağla; tek saat ve tek kontrol seti kullan.
- [x] Her moddan aynı günlük çalışma günlüğüne kaydet; saniye artığını kaybetme ve günlük 24 saat sınırını koru.
- [x] Eski sınav sayacı verisini açık odak oturumunu ezmeden taşı; odak oturumu bitince bekleyen migrasyonu yeniden dene.
- [x] Kayıt sonrası sınav ders geçişlerini temizle; eski sayaç migrasyonu ve kayıt aritmetiği için birim testleri ekle.
- [ ] CI ve gerçek cihazda mod değiştirme, eski oturum migrasyonu ve günlük toplam kontrolü.

### 1. Deneme hata defteri ve konu bağlantısı — tamamlandı (PR #33)
- [x] Denemede etiketlenen zayıf konuları mevcut öğrenme kazanımlarıyla eşleştir.
- [x] Eşleşmeyen etiketleri kullanıcıya göster; doğru ders/kazanım seçilmeden otomatik kayıt yapma.
- [x] Deneme adı, tarihi, ders ve yanlış/boş bağlamını soru/tekrar kartına ekle.
- [x] Aynı deneme etiketi ikinci kez içe aktarılmasın; mevcut fotoğraflı soru kayıtları korunmalı.
- [x] Ders toplamındaki yanlış/boş sayısını konuya aitmiş gibi gösterme.

### 2. Aralıklı tekrar ve bildirimler — tamamlandı (PR #35)
- [x] Mevcut tekrar tarihlerini temel alarak 1–3–7–21 günlük tekrar akışını tamamla.
- [x] Android bildirim iznini açıklayıp kullanıcıdan iste; izin yoksa uygulama içi tekrar kutusu çalışmaya devam etsin.
- [x] WorkManager ile yeniden başlatma ve cihaz yeniden açılmasına dayanıklı hatırlatmalar kur.
- [x] Tamamlanan, ertelenen veya silinen tekrarların eski bildirimlerini iptal et.

### 3. Ana ekran widget’ı — tamamlandı (PR #36)
- [x] Bugünkü soru, çalışma süresi ve günlük hedefi gösteren küçük widget.
- [x] Widget verisini mevcut repository’lerden oku; ikinci bir veri kaynağı oluşturma.
- [x] Widget güncellemesini çalışma kayıtları ve günlük hedef değişiklikleriyle eşleştir.

### 4. Performans ve akıcılık — geliştirme dalında
- [x] İstatistik ve geçmiş ekranlarında deneme JSON yüklemesini ana iş parçacığından çıkar.
- [x] Yeni deneme formunda öğrenme müfredatı migrasyonunu ve kayıt yüklemesini arka planda yap.
- [x] Öğrenme deposunu süreç içinde paylaş; aynı ağır ilk migrasyonları ekran geçişlerinde tekrar çalıştırma.
- [x] Ana ekran ve çalışma ekranında öğrenme deposunu arka planda başlat.
- [x] Tekrar önerileri ve deneme-konu eşleştirmelerini arka planda hesapla; deneme kayıtlarını önbelleğe al.
- [ ] CI ve gerçek cihaz akıcılık kontrolü.

### 5. Arayüz ve kullanılabilirlik
- [ ] Compose ekranlarını ortak tasarım sistemine taşı; eski ekranları yalnızca tüm navigasyon ve derin bağlantılar doğrulandıktan sonra kaldır.
- [ ] Erişilebilirlik, küçük ekran, büyük yazı, koyu/açık tema ve performans kontrolleri.
- [ ] Gereksiz büyük kartları ve tekrarlanan aksiyonları azalt; ana akışları daha hızlı hale getir.

### 6. Veri güvenliği ve yedekleme
- [x] Tek dosyalık ZIP dışa aktarma ve geri yükleme; dosya/sürüm/içerik doğrulaması ve geri yükleme önizlemesi (PR #42).
- [x] Birleştirme tabanlı geri yükleme; yerel kayıtlar korunur, fotoğraflar doğrulanır, hata halinde geri alma uygulanır (PR #42).
- [ ] Room/DataStore geçişini ancak mevcut veriler için testli, çift yönlü olmayan ve geri dönüş planlı bir migrasyon hazırsa değerlendir.
- [ ] Google Drive entegrasyonunu temel yerel yedekleme güvenilir olduktan sonra ele al.

### 7. AI ve ileri yetenekler
- [x] NVIDIA model/endpoint desteğini doğrula: Mistral Large 4 görsel girdisini destekliyor (PR #40).
- [x] Görsel soru analizi: kullanıcı seçimi, boyut sınırı, hata yönetimi, açık veri aktarım bildirimi ve yerel geçmişte dosya saklamama (PR #40).
- [x] AI önerileri yalnızca doğrulanmış, dar kapsamlı eylemlere dönüşsün; model doğrudan veri silemesin veya değiştiremesin (PR #31).

## Değişmez güvenlik kuralları

- Sıralama motoru ve mevcut deneme kayıtları, açıkça kapsamlanmış ve regresyon testli bir iş olmadan değiştirilmez.
- OBP hesabı diploma notu × 0,60 olarak korunur.
- TYT 165, AYT 180 soru/süre modeli ve mevcut sınav türleri korunur; YDT eklenmez.
- Uygulama arayüzü Türkçe kalır.
- Kullanıcı verisi silen veya üzerine yazan migrasyonlar, snapshot ve geri yükleme testi olmadan yapılmaz.
- AI önerileri kullanıcı onayı olmadan çalışma planına veya öğrenme kayıtlarına yazılmaz.
- Her kod değişikliği ayrı PR, test ve yeşil CI ile doğrulanır.
