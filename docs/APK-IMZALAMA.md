# YKS Takip 2.0 — APK güncelleme ve imza

## Neden 'tutarsız imza' hatası oldu?
Android güncellemeleri aynı uygulama kimliğine **ve aynı imza sertifikasına**
sahip olmalıdır. GitHub Actions her seferinde temiz bir Ubuntu makinesinde
derleme yaptığı için Android Gradle debug keystore'u kalıcı tutulmuyordu.

## 2.0 geliştirme dalındaki geçici çözüm
- Debug anahtarı GitHub Actions cache'inden geri yüklenir.
- İlk derlemede anahtar yoksa GitHub Actions `keytool` ile oluşturur,
  `app/build.gradle.kts` içindeki debug yapılandırmasına **tam yolunu**
  bildirir ve başarılı job'un sonunda cache'e kaydeder.
- Böylece Gradle'ın hangi anahtarla APK'yı imzaladığı ile cache'e
  kaydedilen dosya kesin olarak aynı olur. Yerel Android Studio derlemeleri
  ise varsayılan debug yapılandırmasını kullanmaya devam eder.
- Sonraki derlemeler cache varsa **aynı imzayı** kullanır.
- İmzayı karşılaştırmak için Actions'ta sertifikanın SHA-256 parmak izi
  kaydedilir. Private key asla loga yazdırılmaz.

**Önemli:** GitHub cache **kalıcı depolama değildir**. GitHub varsayılan
olarak 7 gün erişilmeyen cache'i silebilir. Cache silinirse yeni debug imzası
oluşur; aynı hata tekrarlanabilir. Bu yalnızca geçici geliştirme çözümüdür.
Kullanıcıya "artık her zaman güncellenecek" sözü verilmemelidir.

## Kalıcı çözüm — GitHub Actions secret (önerilen)
GitHub reposunda `Settings → Secrets and variables → Actions → New repository secret`
bölümüne `YKS_PREVIEW_DEBUG_KEYSTORE_B64` adlı bir secret eklenir.
Değer, standart Android **debug** keystore dosyasının tek satırlık Base64'üdür.

Anahtarın `androiddebugkey` alias'ı ve `android` parola düzeniyle oluşturulması
gereklidir; böylece Gradle'ın varsayılan debug imza yapılandırması ve
`keytool` doğrulaması çalışır. Workflow secret varsa **her zaman** onu
kullanır, cache'i ikincil seçenek olarak bırakır. Secret mevcut değilse
geçici cache stratejisine döner.

Bu anahtarı **herkese açık repoya, commitlere veya issue'lara yüklemeyin.**
İmza anahtarı GitHub Secrets içinde saklanmalıdır. Gerçek dağıtım/Play Store
için ayrı bir güvenli **release** anahtarı, imzalı yayın yapılandırması ve
saklama/rotasyon planı gerekir; debug anahtarı gerçek sürüm imzası değildir.

## Eski APK'dan geçiş
Bu değişiklik, **önceden kurulmuş** ve farklı anahtarla imzalanmış bir APK'yı
sihrî şekilde güncelleyemez. İlk kez yeni anahtara geçerken bir defaya
mahsus farklı imza sorunu olabilir. Kayıtları kaybetmemek için:

1. Önce tüm denemeleri JSON'la yedekleyin.
2. Soru/fotoğraf ve öğrenme kayıtları varsa ayrıca öğrenme ZIP'i oluşturun.
3. Güncel dosyaların gerçekten kaydedildiğini kontrol edin.
4. Eski APK'yı ancak yedekler doğrulanınca kaldırıp yeni APK'yı yükleyin.
5. Deneme JSON'unu, varsa öğrenme ZIP'ini ilgili içe aktarım yollarıyla
   geri yükleyin. Çalışma günlüğü ve plan kayıtları ayrı olduğundan,
   bu iki yedeğin hepsini kapsadığı **varsayılmamalıdır**.

Bu imzalama değişikliğinin yalnızca **2.0** dalında uygulanması gerekir.
