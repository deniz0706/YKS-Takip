# YKS Takip 2.0 — Mimari Dönüşüm Kaydı

> Durum: İlk envanter / keşif aşaması. Bu belge bir teknoloji geçişini henüz başlatmaz; mevcut sistemi koruyarak kararları kanıtla vermek için başlangıç kaydıdır.

## Değişmez ürün hedefleri

- Android ilk dağıtım hedefidir; iOS ilk günden mimari hedef olarak desteklenmelidir.
- Web sürümü bu çalışmanın kapsamında değildir.
- Uygulama ve kullanıcı arayüzü Türkçe kalır.
- Mevcut kullanıcı verileri, fotoğraflar, sınav kayıtları, çalışma geçmişi, ayarlar ve yedekler kaybolamaz.
- TYT/AYT hesapları, OBP davranışı ve sıralama motoru karakterizasyon testleri olmadan değiştirilemez.
- Eski uygulama, yeni uygulama doğrulanana kadar çalışan referans ve geri dönüş noktası olarak kalır.
- Teknoloji seçimi popülerliğe göre değil; iOS/Android kapsamı, UI kalitesi, performans, erişilebilirlik, bakım maliyeti, test edilebilirlik ve yayınlama gerçeklerine göre yapılır.

## Main dalında ilk incelemede görülenler

İncelenen referans: `main` — `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`.

### Mevcut yapı

- Tek Android Gradle modülü: `:app`.
- Kotlin 2.0.21, Android Gradle Plugin 8.7.3, Java 17 hedefi.
- Jetpack Compose ve klasik XML/ViewBinding/Fragment yapısı birlikte bulunuyor. Bu nedenle arayüz tamamen Compose'a geçmiş değil.
- `compileSdk 35`, `minSdk 26`, `targetSdk 35`.
- Android uygulaması şu an tek gerçek istemci platformu; mevcut Gradle projesinde iOS/Kotlin Multiplatform modülü yok.
- CI, GitHub Actions üzerinden Android APK derlemesi yapıyor.

### Veri ve iş mantığı

- Sınav, çalışma ve öğrenme kayıtlarının önemli bölümü SharedPreferences içinde JSON olarak tutuluyor.
- Veri erişimi `ExamRepository`, `StudyRepository`, `LearningRepository` gibi Android Context/SharedPreferences bağımlı sınıflara bağlı.
- Öğrenme verileriyle ilişkili fotoğraflar uygulamanın özel dosya alanında saklanıyor.
- Tam yedekleme/geri yükleme ve çeşitli eski veri biçimleri için migrasyonlar mevcut.
- Room/SQLite tabanlı bir uygulama veritabanı bu incelemede görülmedi.
- Bu nedenle SQL'e geçiş bir teknoloji etiketi eklemek değil; veri modeli, migrasyon, fotoğraf sahipliği, geri yükleme ve geriye uyumluluk işi olarak ele alınmalı.

### Kritik alanlar

- Sıralama tahmini ve resmî sıralama lookup kodu ayrı Kotlin dosyalarında ve birim testleriyle korunuyor. Yeni uygulamaya geçişte önce mevcut davranışı sabitleyen test vektörleri çıkarılmalı.
- Odak/sınav zamanlayıcısı ortaklaştırılmış durumda; kayıt aritmetiği ve eski zamanlayıcı migrasyonu için testler var.
- AI istemcisi şu anda Android uygulaması içinde NVIDIA uyumlu HTTP API'sine doğrudan istek gönderebiliyor. Kullanıcı API anahtarı Android Keystore destekli depoda saklanıyor. Ürünün kendi ortak API anahtarını APK'ya koymak güvenli değildir; ürün tarafından sağlanan AI erişimi için sunucu tarafı gateway gerekir.
- AI, veri erişimi ve fotoğraf gönderimi yeni mimaride açık izin, veri minimizasyonu, kota ve sağlayıcıdan bağımsız adaptörlerle tasarlanmalı.

## Teknoloji kararı: henüz kilitlenmedi

iOS hedefi nedeniyle çapraz platform seçenekleri gerçek bir teknik denemeyle karşılaştırılacak:

1. **Flutter + Dart:** Tek UI kod tabanı ve Android/iOS tutarlılığı. Özel platform özellikleri için native köprüler gerekebilir.
2. **Kotlin Multiplatform + Compose Multiplatform:** Kotlin/domain bilgisini yeniden kullanma ve Compose UI paylaşımı. Mevcut Android projesinin modülerleştirilmesi ve platform bağımlılıklarının ayrıştırılması gerekir.
3. **Native ayrı istemciler:** Android ve iOS için ayrı UI; ortak backend ve mümkünse ortak sözleşmeler. En fazla platform özgürlüğü, daha yüksek geliştirme/ bakım maliyeti.

Karar, küçük bir dikey dilim üzerinde şu ölçütlerle verilecek: gerçek ekran kalitesi, erişilebilirlik, grafik/animasyon, offline-first veri, bildirim/widget entegrasyonu, fotoğraf seçimi ve dosya yönetimi, performans, test ve CI, iOS derleme/yayınlama yolu. Sırf mevcut Kotlin emeği var diye KMP seçilmeyecek; sırf Flutter popüler diye de Flutter seçilmeyecek.

## Güvenli dönüşüm planı

1. **Envanter:** Main, tüm mevcut branch'ler, PR/commit geçmişi, özellikler, veri anahtarları, fotoğraf dosyaları, migrasyonlar, testler ve CI.
2. **Karakterizasyon:** Mevcut sıralama, net, OBP, süre, istatistik ve yedekleme davranışları için sabit test vektörleri ve örnek veri setleri.
3. **Platform spike:** Flutter ve Kotlin Multiplatform/Compose Multiplatform için aynı küçük ekran + veri akışı prototipi; iOS build yolu doğrulanmadan platform kararı yok.
4. **Hedef mimari:** UI, domain/use-case, repository arayüzleri, yerel veri, senkronizasyon ve platform adaptörlerini ayır.
5. **Veri migrasyonu:** Mevcut JSON/SharedPreferences ve fotoğraf dosyalarından yeni şemaya kontrollü, tekrar çalıştırılabilir, doğrulamalı ve geri alınabilir aktarım.
6. **Dikey dilimlerle taşıma:** Önce bir özellik uçtan uca taşınır; eski ve yeni sonuçlar karşılaştırılır. Sonra diğer özellikler.
7. **Parite ve yayın:** Özellik eşitliği, cihaz testleri, Android dağıtımı ve iOS için macOS/Xcode + Apple Developer gereksinimleri tamamlanmadan eski uygulama emekliye ayrılmaz.

## İlk keşif bulguları ve açık işler

- [x] Main dalı ve mevcut proje ağacı ilk kez incelendi.
- [x] Son main commit ve Android CI durumu kontrol edildi.
- [x] Tek modüllü Android/Compose + XML karma yapı belirlendi.
- [x] JSON/SharedPreferences veri depolaması ve AI istemcisinin mevcut konumu örnek dosyalardan doğrulandı.
- [ ] Tüm dosya/özellik envanterini tamamla.
- [ ] Branch'lerde main'e girmemiş değerli özellikleri ve çakışmaları belirle.
- [ ] Tüm SharedPreferences dosya adları/anahtarları ve JSON şemalarını çıkar.
- [ ] Yedekleme kapsamını fotoğraf dosyaları ve migrasyonlarla birlikte doğrula.
- [ ] Sıralama motoru için sabit giriş/çıkış karakterizasyon seti oluştur.
- [ ] Flutter ve KMP/Compose Multiplatform prototiplerini aynı kabul ölçütleriyle karşılaştır.
- [ ] Teknoloji kararını kanıtlarla ADR olarak kaydet.

## Güvenlik notları

- API anahtarları, imzalama anahtarları, keystore dosyaları ve kişisel kullanıcı yedekleri repoya eklenmez.
- Bu branch yalnızca mimari dokümantasyon ve keşif kayıtları içerir; çalışan uygulama kodu değiştirilmez.
- Main dalı ve mevcut APK, yeni sürüm doğrulanana kadar korunur.
