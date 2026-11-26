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

## Öncelikli geliştirme sırası

### 1. Deneme hata defteri ve konu bağlantısı — tamamlandı (PR #33)
- [x] Denemede etiketlenen zayıf konuları mevcut öğrenme kazanımlarıyla eşleştir.
- [x] Eşleşmeyen etiketleri kullanıcıya göster; doğru ders/kazanım seçilmeden otomatik kayıt yapma.
- [x] Deneme adı, tarihi, ders ve yanlış/boş bağlamını soru/tekrar kartına ekle.
- [x] Aynı deneme etiketi ikinci kez içe aktarılmasın; mevcut fotoğraflı soru kayıtları korunmalı.
- [x] Ders toplamındaki yanlış/boş sayısını konuya aitmiş gibi gösterme.

### 2. Aralıklı tekrar ve bildirimler
- [ ] Mevcut tekrar tarihlerini temel alarak 1–3–7–21 günlük tekrar akışını tamamla.
- [ ] Android bildirim iznini açıklayıp kullanıcıdan iste; izin yoksa uygulama içi tekrar kutusu çalışmaya devam etsin.
- [ ] WorkManager ile yeniden başlatma ve cihaz yeniden açılmasına dayanıklı hatırlatmalar kur.
- [ ] Tamamlanan, ertelenen veya silinen tekrarların eski bildirimlerini iptal et.

### 3. Ana ekran widget'ı
- [ ] Bugünkü soru, çalışma süresi, günlük hedef ve yaklaşan tekrarları gösteren küçük widget.
- [ ] Widget verisini mevcut repository'lerden oku; ikinci bir veri kaynağı oluşturma.
- [ ] Widget güncellemesini çalışma/deneme kaydı değişiklikleriyle eşleştir.

### 4. Arayüz ve kullanılabilirlik
- [ ] Compose ekranlarını ortak tasarım sistemine taşı; eski ekranları yalnızca tüm navigasyon ve derin bağlantılar doğrulandıktan sonra kaldır.
- [ ] Erişilebilirlik, küçük ekran, büyük yazı, koyu/açık tema ve performans kontrolleri.
- [ ] Gereksiz büyük kartları ve tekrarlanan aksiyonları azalt; ana akışları daha hızlı hale getir.

### 5. Veri güvenliği ve yedekleme
- [ ] JSON/SharedPreferences verileri için doğrulanabilir dışa aktarma ve geri yükleme akışı.
- [ ] Geri yükleme öncesi önizleme, sürüm kontrolü ve geri alınabilir snapshot.
- [ ] Room/DataStore geçişini ancak mevcut veriler için testli, çift yönlü olmayan ve geri dönüş planlı bir migrasyon hazırsa değerlendir.
- [ ] Google Drive entegrasyonunu temel yerel yedekleme güvenilir olduktan sonra ele al.

### 6. AI ve ileri yetenekler
- [ ] NVIDIA model/endpoint desteğini doğrulamadan görsel soru analizi vaat etme.
- [ ] Görsel soru analizi mümkünse izinli fotoğraf gönderimi, boyut sınırı, hata yönetimi ve kullanıcı onayı.
- [ ] AI önerileri yalnızca doğrulanmış, dar kapsamlı eylemlere dönüşsün; model doğrudan veri silemesin veya değiştiremesin.

## Değişmez güvenlik kuralları

- Sıralama motoru ve mevcut deneme kayıtları, açıkça kapsamlanmış ve regresyon testli bir iş olmadan değiştirilmez.
- OBP hesabı diploma notu × 0,60 olarak korunur.
- TYT 165, AYT 180 soru/süre modeli ve mevcut sınav türleri korunur; YDT eklenmez.
- Uygulama arayüzü Türkçe kalır.
- Kullanıcı verisi silen veya üzerine yazan migrasyonlar, snapshot ve geri yükleme testi olmadan yapılmaz.
- AI önerileri kullanıcı onayı olmadan çalışma planına veya öğrenme kayıtlarına yazılmaz.
- Her kod değişikliği ayrı PR, test ve yeşil CI ile doğrulanır.
