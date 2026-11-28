# YKS TAKİP 4.0 — MASTER DEVELOPMENT PROMPT

**Complete Product Transformation | Android + iOS | Polyglot | Extensible Architecture**

Bu belge, YKS Takip 4.0 projesinin kalıcı ana geliştirme şartnamesidir. Sonraki oturumlarda önce bu belge, `docs/DEVELOPMENT-STATUS.md` ve gerçek Git/CI durumu okunarak devam edilir. Bu şartname, tek seferlik bir görev değil; bütün geliştirme kararlarını yöneten ürün vizyonu ve mühendislik sözleşmesidir.

## Ana görev ve başarı ölçütü

Principal Software Architect, Senior Mobile Engineer, Backend Engineer, UI/UX Designer ve DevOps Engineer sorumlulukları üstlenilir. Amaç mevcut uygulamaya birkaç özellik eklemek veya tasarım makyajı yapmak değil; çalışan özellikleri, kullanıcı verilerini ve ürün değerini koruyarak uzun yıllar geliştirilebilecek, modern, genişletilebilir ve sürdürülebilir bir platform oluşturmaktır.

**Ana hedef:** Android + iOS + Polyglot + Modüler Mimari + Offline-first + Genişletilebilirlik + Modern UI/UX.

Yapay süre sınırı yoktur. Başarı geçen zamanla değil, gerçek kod, test ve çalışan prototiple doğrulanmış mühendislik çıktısıyla ölçülür.

## 1. Ürün vizyonu

- Android ve iOS birinci sınıf hedeflerdir; özellik, performans ve tasarım paritesi hedeflenir.
- Kullanıcı deneyimi Türkçe kalır.
- Temel özellikler çevrimdışı çalışır; kullanıcı verileri yerel ve güvenli saklanır.
- Hesap, bulut senkronizasyonu, AI servisleri ve çoklu cihaz desteği ileride eklenebilir olmalıdır.
- Web sürümü kapsam dışıdır; mimari gelecekte başka istemcileri engellememelidir.

## 2. Polyglot teknoloji mimarisi

Temel karar **Kotlin Multiplatform + Compose Multiplatform**'dır. Flutter ile ikinci prototip oluşturulmayacaktır.

Teknoloji alanları:
- Android: Kotlin, Compose.
- iOS: Kotlin Multiplatform; gerektiğinde Swift/SwiftUI ve native Apple API'leri.
- Ortak UI: Compose Multiplatform.
- Ortak iş mantığı: Kotlin Multiplatform.
- Yerel ilişkisel veri: SQLDelight.
- Serileştirme: Kotlin Serialization.
- AI/backend: somut ihtiyaçla Python/FastAPI veya Ktor.
- Performans çekirdeği: yalnızca ölçülmüş ihtiyaç varsa Rust veya C/C++.
- Sorgular: SQL.
- API sözleşmeleri: OpenAPI, JSON veya uygun sürümlenebilir alternatif.
- CI/CD: GitHub Actions.

Sırf polyglot görünmek için dil eklenmez. Teknoloji seçimi mevcut kod miktarına göre yapılmaz; resmi dokümantasyondan güncel kararlı sürümler doğrulanır. Önemli kararlar ADR ile kaydedilir. Ana platform kararı `docs/adr/ADR-001-platform-architecture.md` dosyasında gerekçelendirilir.

## 3. Android + iOS mimarisi

- Ortak iş mantığı mümkün olduğunca paylaşılır; Compose Multiplatform ortak UI için tercih edilir.
- Swift/SwiftUI ve native Android API'leri gerektiğinde adaptör olarak kullanılabilir.
- Kamera, galeri, dosya sistemi, bildirim, paylaşım, yaşam döngüsü ve güvenli depolama platform adaptörleriyle sağlanır.
- Telefon, tablet ve farklı ekran boyutları desteklenir.
- iOS imzalama/yayınlama gereksinimleri belgelenir.
- iOS derleme ortamı yoksa açıkça belirtilir; doğrulanmamış iOS desteği iddia edilmez.

## 4. Ortak kod ve veri mimarisi

`commonMain` içinde kesinlikle `android.*`, `java.*`, `org.json` veya başka platform API'leri bulunamaz.

- Ortak domain, veri ve iş mantığı platform bağımsızdır.
- SQLDelight ve Kotlin Serialization kullanılır.
- Android/iOS adaptörleri ayrıdır.
- Repository/use-case yaklaşımı, açık bağımlılıklar, modülerlik ve saf/test edilebilir iş mantığı benimsenir.
- Veri modelleri ve API sözleşmeleri tutarlı tutulur.
- Mevcut saf Kotlin kodu ortak modüllere taşınırken davranışı değiştirilmez.

## 5. Eski veri ve yedek uyumluluğu

SharedPreferences, JSON modelleri, `BackupManager`, `StudyBackupManager`, tüm anahtarlar, fotoğraf/dosya referansları ve dışa aktarılan eski yedek formatları incelenir. Bunları kayıpsız aktaracak `LegacyBackupImporter` oluşturulur.

- Gerçek veri örnekleri varsa kişisel bilgilerden arındırılmış fixture kullanılır; yoksa kaynak koddan çıkarılan gerçek formatlar test edilir. Format uydurulmaz.
- Hata durumunda eski veriye dokunulmaz.
- Tekrarlı import, bozuk yedek, eksik alan ve eski şema sürümü test edilir.
- Sınav kayıtları, çalışma geçmişi, fotoğraflar, ayarlar ve yedekler korunur.
- Geri dönüş/kurtarma stratejisi hazırlanır.
- Migration tamamlanıp doğrulanmadan eski sistem kaldırılmaz.

## 6. UI/UX ve görsel tasarım

Özgün, tutarlı, premium bir tasarım kimliği oluşturulur. İlk mimari prototip geçici grafit temadır:
- Koyu zemin `#0F1115`, mat kart `#181B20`, ince nötr çerçeve `#2A2E37` / 1.dp.
- Neon, glow ve yüksek doygunluklu gradyan yoktur.
- Açık tema mat kâğıt/taş tonlarıdır.
- Things 3 ve Apple Pro çizgisinden ilham alınır; kopyalanmaz.
- Tipografi, boşluk, hiyerarşi, erişilebilirlik, açık/koyu tema ve adaptive layout bütüncül tasarlanır.

Ana ekran modüler Bento Widget Grid olur. Bileşenler: YKS geri sayım halkası, TYT/AYT net ve sparkline kartları, odak sayacı, tek tık AI Koç kartı, günün planı, haftalık aktivite ısı haritası. Bütün bileşenler yeniden kullanılabilir olmalıdır.

Canvas/özel çizim, Bézier, vektör yolları, Lottie/platform animasyonları, gesture, adaptive layout, haptics ve GPU hızlandırma yalnızca somut tasarım/etkileşim ihtiyacında kullanılır.

Yeni UI deklaratiftir; XML tabanlı ekran layout'ları sürdürülmez. Manifest, backup kuralları ve platform yapılandırması gibi gerekli XML korunur. Sadece renk/köşe makyajı yapılmaz; navigasyon, ekran mimarisi, bilgi hiyerarşisi ve etkileşimler yeniden değerlendirilir.

**Grafit tema nihai marka değildir.** Nihai Aurora teması ileride birlikte geliştirilecektir. Renk, efekt, animasyon, yüzey, tipografi ve bileşen tokenları değiştirilebilir olmalı; Aurora'ya geçiş mimariyi yeniden yazmayı gerektirmemelidir.

## 7. Genişletilebilir mimari

Mimari; yeni sınav türleri, eğitim sistemleri, dersler, konular, soru tipleri, puanlama/sıralama algoritmaları, AI sağlayıcıları/modelleri, hesaplar, çoklu cihaz senkronizasyonu, bulut yedekleme, akıllı planlar, bildirimler, widget/canlı aktiviteler, analitik/kişiselleştirme ve yeni mobil/masaüstü istemcilerin eklenmesini kolaylaştırmalıdır. Modüler yapı, açık sözleşmeler ve adapter yaklaşımı kullanılır.

## 8. Backend ve AI mimarisi

Başlangıçta gereksiz mikroservis kurulmaz; ihtiyaç oluşursa modüler monolit tercih edilir. API sürümleme, kimlik doğrulama/yetkilendirme, hesap, senkronizasyon, bulut yedekleme, AI Gateway, rate limiting, loglama/gözlemlenebilirlik, gizlilik, şifreleme ve güvenli iletişim tasarlanır.

AI tek sağlayıcıya bağlanmaz. Birden fazla sağlayıcı/model için adapter, metin/görsel soru çözümü, Türkçe YKS doğruluk değerlendirmesi, model yönlendirme, kota/hata yönetimi, maliyet/gecikme ölçümü ve veri minimizasyonu ele alınır. Ürün API anahtarları APK/iOS uygulamasına gömülmez. Model/fiyat/API desteği doğrulanmadan varsayılmaz.

## 9. Mevcut özellikleri koru

TYT/AYT sınav girişi, OBP, puan ve sıralama tahmini, resmî sıralama verileri, sınav geçmişi, istatistik, çalışma takibi, zamanlayıcı, AI, görselle soru çözümü, fotoğraf/dosya yönetimi, yedekleme/geri yükleme ve ayarlar korunur.

Özellikle `YksRankingEstimator.kt`, `YksOfficialRankingLookup.kt`, `YksRankingCalculator.kt` ve ilişkili kod incelenir. Mevcut davranış karakterizasyon testleriyle sabitlenir. Bu testler geçmeden sıralama mantığı değiştirilmez; motor gereksiz yere yeniden yazılmaz.

## 10. İlk çalışan dikey dilim

Boş modül/klasörlerden ibaret mimari kabul edilmez. İlk gerçek dilim şunları içermelidir:
- Compose Multiplatform ana ekran ve Bento Dashboard.
- Gerçek sınav giriş akışı.
- Ortak domain ve gerçek sıralama hesaplaması.
- Ortak veri katmanı ve SQLDelight entegrasyonu.
- Unit ve integration testleri.
- Android üzerinde çalışan prototip.
- Projede tanımlı iOS hedefi.

Gerçek sıralama motoru ve gerçek veri akışı kullanılmalıdır; sahte/statik ekranlar yeterli değildir.

## 11. Git, branch ve worktree

Önce branch'ler, uzak branch'ler, açık PR'lar, commit geçmişi, main/geliştirme farkları, henüz main'e alınmamış işler, PR #51, `4.0/architecture-audit` ve APK güncelleme/imzalama altyapısı incelenir.

- `main` daima kararlı kalır; `4.0/foundation` ana entegrasyon dalıdır.
- Bağımsız işler için gerektiğinde `4.0/<alan>` veya `codex/<alan>` branch'leri kullanılabilir.
- Feature branch'leri testlerden sonra foundation'a entegre edilir.
- Main'e geçiş yalnızca inceleme ve yeterli testten sonra yapılır.
- Main'e doğrudan yazılmaz; önceki işler ezilmez; force push yapılmaz.
- Başka branch commit'leri incelenmeden yeniden yazılmaz.
- Gereksiz branch açılmaz. Branch silinmeden önce commit'lerin korunduğu doğrulanır.
- PR'lar otomatik merge edilmez; başarısız testle entegrasyon yapılmaz.
- Her branch'in amacı ve durumu belgelenir.

## 12. Android güncelleme ve imzalama

Application ID kesinlikle `com.deniz0706.ykstakip` kalır. Mevcut imza sertifikası korunur. VersionCode monotonik artar. APK mevcut kurulumun üzerine güncellenebilmelidir. İmza uyuşmazlığı gizlenmez. Veri güvenliği doğrulanmadan uygulamayı kaldırma önerilmez. Mevcut/yeni APK sertifikaları karşılaştırılır; yedek/geri dönüş prosedürü oluşturulur. Keystore/parolalar/secret'lar repoya commit edilmez.

## 13. Kalite, test ve performans

Unit, integration, UI, platform, migration ve sıralama karakterizasyon testleri; performans benchmarkları, bellek, açılış süresi, animasyon akıcılığı, erişilebilirlik, bağımlılık güvenlik taraması, CI/CD, Android/iOS build doğrulaması uygulanır. Gereksiz ağır bağımlılıklardan kaçınılır. Build kırmızıysa neden bulunmadan başarılı sayılmaz; çalıştırılmayan test başarılı ilan edilmez.

## 14. Çalışma ve devamlılık

Yapay zaman hedefi yoktur. Büyük işler test edilebilir aşamalara ayrılır. Kod yazmak tek başına tamamlanma sayılmaz; test/build/entegrasyon doğrulanır. Bağlam veya ortam sınırı nedeniyle durulacaksa güvenli commit yapılır. `docs/DEVELOPMENT-STATUS.md` branch, son commit, tamamlanan/devam eden işler, test sonuçları ve sonraki adımlarla güncel tutulur. Her oturumda önce bu belge ve gerçek Git durumu incelenir.

## 15. Tamamlanma kriterleri

Tamamlandı denebilmesi için:
- Hedef mimari belgelenmiş ve kurulmuş olmalı.
- Android/iOS proje altyapısı mevcut olmalı.
- Ortak modüller platform bağımsızlığını korumalı.
- Güvenli legacy migration sistemi olmalı.
- Sıralama karakterizasyon testleri başarılı olmalı.
- Ana ekran ve temel sınav akışı çalışmalı.
- SQLDelight veri katmanı çalışmalı.
- Android build/test başarılı olmalı.
- iOS build mümkün ortamda doğrulanmalı.
- CI/CD çalışmalı.
- Commit'ler düzenli ve entegrasyon doğrulanmış olmalı.
- Eksik işler tamamlanmış gibi gösterilmemeli.

## 16. Son rapor

Son rapor; kurulan mimariyi, kullanılan dilleri/teknolojileri ve gerekçelerini, branch/commit/PR'ları, tamamlanan özellikleri, gerçekten çalıştırılan testleri, Android/iOS build durumunu, migration fixture/verilerini, veri güvenliği yaklaşımını, tamamlanmamış işleri, teknik borç/engelleri ve sıradaki aşamayı açıkça belirtir.

## Son talimat

Eski mimariyi veya eski kodun tamamını körü körüne korumak zorunda değilsin; fakat çalışan özellikleri, kullanıcı verilerini ve ürün değerini korumak zorundasın. Gerektiğinde büyük mimari değişiklik yap, farklı diller kullan veya teknolojiyi değiştir. Yeni mimari yalnızca teoride değil gerçek kod, test ve çalışan prototiple kurulmalıdır.

**Hedef, bugünkü uygulamanın biraz daha iyi sürümü değil; yarın yeni teknolojiler, özellikler, servisler ve platformlarla büyüyebilecek sağlam bir yazılım ürünüdür.**
