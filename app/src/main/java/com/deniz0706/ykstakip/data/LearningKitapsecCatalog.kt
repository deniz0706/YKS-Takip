package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/** Kitapsec "YKS 2027 TYT AYT Konuları" source: https://www.kitapsec.com/blog/yks-2023-tyt-ayt-konulari-112.html
 * Uses subject outline headings, not historical question distributions as subskills.
 * Tarih-2 is the exception: its topics are taken from the site's history question
 * distribution labels, since the site has no independent Tarih-2 curriculum list.
 * This commercial site is not an official MEB/ÖSYM scope declaration.
 */
object LearningKitapsecCatalog {
    private data class Group(val exam: String, val subject: String, val title: String)
    private val groups: List<Group> by lazy {
        subjectRows.trimIndent().lineSequence().filter { it.isNotBlank() }.flatMap { row ->
            val p = row.split('|', limit = 3)
            require(p.size == 3)
            p[2].split(';').filter { it.isNotBlank() }.asSequence().map {
                Group(p[0].trim(), p[1].trim(), it.trim())
            }
        }.toList().also { list ->
            require(list.map { "unit|${it.exam}|${it.subject}|${it.title}" }.distinct().size == list.size)
        }
    }

    val starter: List<LearningOutcome> by lazy {
        groups.map { (exam, subject, title) ->
            LearningOutcome("unit|$exam|$subject|$title", exam, subject, subject, title)
        }
    }

    private val nested: Map<String, List<String>> by lazy {
        childrenRows.trimIndent().lineSequence().filter { it.isNotBlank() }.associate { row ->
            val p = row.split('|', limit = 4)
            require(p.size == 4)
            val key = "unit|${p[0]}|${p[1]}|${p[2]}"
            key to p[3].split(';').map { it.trim() }.filter { it.isNotBlank() }.distinct()
        }.also { all ->
            require((all.keys - starter.mapTo(mutableSetOf()) { it.id }).isEmpty()) {
                "Nested topics without a matching unit"
            }
        }
    }

    fun forOutcome(outcome: LearningOutcome): List<String> = nested[outcome.id].orEmpty()
    fun expandableIds(): Set<String> = nested.keys
    fun groupedSubjects(): Map<String, Int> =
        groups.groupingBy { it.exam + "/" + it.subject }.eachCount()

    private val subjectRows = """
TYT|Türkçe|Sözcükte Anlam;Söz Yorumu;Deyim ve Atasözü;Cümlede Anlam;Paragraf;Ses Bilgisi;Yazım Kuralları;Noktalama İşaretleri;Sözcükte Yapı/Ekler;Sözcük Türleri;Fiiller;Sözcük Grupları;Cümlenin Ögeleri;Cümle Türleri;Anlatım Bozukluğu
TYT|Matematik|Temel Kavramlar;Sayı Basamakları;Bölme ve Bölünebilme;EBOB – EKOK;Rasyonel Sayılar;Basit Eşitsizlikler;Mutlak Değer;Üslü Sayılar;Köklü Sayılar;Çarpanlara Ayırma;Oran Orantı;Denklem Çözme;Problemler;Kümeler – Kartezyen Çarpım;Mantık;Fonksiyonlar;Polinomlar;2. Dereceden Denklemler;Permütasyon ve Kombinasyon;Olasılık;Veri – İstatistik
TYT|Geometri|Temel Kavramlar;Doğruda Açılar;Üçgende Açılar;Eşkenar Üçgen;Açıortay;Kenarortay;Eşlik ve Benzerlik;Üçgende Alan;Üçgende Benzerlik;Açı Kenar Bağıntıları;Çokgenler;Özel Dörtgenler;Çember ve Daire;Analitik Geometri;Katı Cisimler;Çemberin Analitiği
TYT|Fizik|Fizik Bilimine Giriş;Madde ve Özellikleri;Sıvıların Kaldırma Kuvveti;Basınç;Isı, Sıcaklık ve Genleşme;Hareket ve Kuvvet;Dinamik;İş, Güç ve Enerji;Elektrik;Manyetizma;Dalgalar;Optik
TYT|Kimya|Kimya Bilimi;Atom ve Periyodik Sistem;Kimyasal Türler Arası Etkileşimler;Maddenin Halleri;Doğa ve Kimya;Kimyanın Temel Kanunları;Kimyasal Hesaplamalar;Karışımlar;Asit, Baz ve Tuz;Kimya Her Yerde
TYT|Biyoloji|Canlıların Ortak Özellikleri;Canlıların Temel Bileşenleri;Hücre ve Organelleri;Hücre Zarından Madde Geçişi;Canlıların Sınıflandırılması;Mitoz ve Eşeysiz Üreme;Mayoz ve Eşeyli Üreme;Kalıtım;Ekosistem Ekolojisi;Güncel Çevre Sorunları
TYT|Tarih|Tarih ve Zaman;İnsanlığın İlk Dönemleri;Orta Çağ’da Dünya;İlk ve Orta Çağlarda Türk Dünyası;İslam Medeniyetinin Doğuşu;Türklerin İslamiyet’i Kabulü ve İlk Türk İslam Devletleri;Yerleşme ve Devletleşme Sürecinde Selçuklu Türkiyesi;Beylikten Devlete Osmanlı Siyaseti;Devletleşme Sürecinde Savaşçılar ve Askerler;Beylikten Devlete Osmanlı Medeniyeti;Dünya Gücü Osmanlı;Sultan ve Osmanlı Merkez Teşkilatı;Klasik Çağda Osmanlı Toplum Düzeni;Değişen Dünya Dengeleri Karşısında Osmanlı Siyaseti;Değişim Çağında Avrupa ve Osmanlı;Uluslararası İlişkilerde Denge Stratejisi (1774-1914);Devrimler Çağında Değişen Devlet-Toplum İlişkileri;Sermaye ve Emek;XIX. ve XX. Yüzyılda Değişen Gündelik Hayat;XX. Yüzyıl Başlarında Osmanlı Devleti ve Dünya;Milli Mücadele;Atatürkçülük ve Türk İnkılabı
TYT|Coğrafya|Doğa ve İnsan;Dünya’nın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi;Atmosfer ve Sıcaklık;İklimler;Basınç ve Rüzgarlar;Nem, Yağış ve Buharlaşma;İç Kuvvetler / Dış Kuvvetler;Su – Toprak ve Bitkiler;Nüfus;Göç;Yerleşme;Türkiye’nin Yer Şekilleri;Ekonomik Faaliyetler;Bölgeler;Uluslararası Ulaşım Hatları;Çevre ve Toplum;Doğal Afetler
TYT|Felsefe|Felsefe’nin Konusu;Bilgi Felsefesi;Varlık Felsefesi;Ahlak Felsefesi;Sanat Felsefesi;Din Felsefesi;Siyaset Felsefesi;Bilim Felsefesi;İlk Çağ Felsefesi;2. Yüzyıl ve 15. Yüzyıl Felsefeleri;15. Yüzyıl ve 17. Yüzyıl Felsefeleri;18. Yüzyıl ve 19. Yüzyıl Felsefeleri;20. Yüzyıl Felsefesi
TYT|Din Kültürü|Bilgi ve İnanç;İslam ve İbadet;Ahlak ve Değerler;Allah İnsan İlişkisi;Hz. Muhammed (S.A.V.);Vahiy ve Akıl;İslam Düşüncesinde Yorumlar, Mezhepler;Din, Kültür ve Medeniyet;İslam ve Bilim, Estetik, Barış;Yaşayan Dinler
AYT|Matematik|Kümeler;Denklem ve Eşitsizlikler;Fonksiyonlar;Veri;Olasılık;Sayma;Fonksiyonlarla İşlemler ve Uygulamalar;İkinci Dereceden Denklem ve Fonksiyonlar;Polinomlar;Trigonometri;Analitik Geometri;Fonksiyonlarda Uygulamalar;Denklem ve Eşitsizlik Sistemleri;Üstel ve Logaritmik Fonksiyonlar;Diziler;Türev;İntegral;Dönüşümler
AYT|Geometri|Üçgenler;Dörtgenler ve Çokgenler;Geometrik Cisimler;Trigonometri;Analitik Geometri;Çember ve Daire;Uzay Geometri;Dönüşümler
AYT|Fizik|Fizik Bilimine Giriş;Madde ve Özellikleri;Kuvvet ve Hareket;Enerji;Isı ve Sıcaklık;Basınç ve Kaldırma Kuvveti;Elektrik ve Manyetizma;Dalgalar;Optik;Çembersel Hareket;Basit Harmonik Hareket;Dalga Mekaniği;Atom Fiziğine Giriş ve Radyoaktivite;Modern Fizik;Modern Fiziğin Teknolojideki Uygulamaları
AYT|Kimya|Kimya Bilimi;Atom ve Periyodik Sistem;Kimyasal Türler Arası Etkileşimler;Maddenin Halleri;Karışımlar;Asitler, Bazlar ve Tuzlar;Kimya Her Yerde;Modern Atom Teorisi;Gazlar;Sıvı Çözeltiler ve Çözünürlük;Kimyasal Tepkimelerde Enerji;Kimyasal Tepkimelerde Hız;Kimyasal Tepkimelerde Denge;Kimya ve Elektrik;Karbon Kimyasına Giriş;Organik Bileşikler;Enerji Kaynakları ve Bilimsel Gelişmeler
AYT|Biyoloji|Yaşam Bilimi ve Biyoloji;Canlılar Dünyası;Güncel Çevre Sorunları;Üreme;Kalıtımın Genel İlkeleri;Dünyamız;İnsan Fizyolojisi;Komünite ve Popülasyon Ekolojisi;Genden Proteine;Canlılarda Enerji Dönüşümleri;Bitki Biyolojisi;Canlılar ve Çevre
AYT|Edebiyat|9. Sınıf Metinleri;10. Sınıf Metinleri;11. Sınıf Metinleri;12. Sınıf Metinleri;Anlam Bilgisi;Dil Bilgisi;Metinlerin Sınıflandırılması;Şiir Bilgisi;Edebi Sanatlar;İslamiyet Öncesi Türk Edebiyatı ve Geçiş Dönemi;Halk Edebiyatı;Divan Edebiyatı;Tanzimat Edebiyatı;Servet-i Fünun ve Fecr-i Âti Edebiyatı;Milli Edebiyat;Cumhuriyet Dönemi Edebiyatı;Batı Edebiyat Akımları
AYT|Tarih-1|Tarih Bilimi;Uygarlığın Doğuşu ve İlk Uygarlıklar;İlk Türk Devletleri;İslam Tarihi ve Uygarlığı (13. Yüzyıla Kadar);Türk – İslam Devletleri (10-13. Yüzyıllar);Türkiye Tarihi (11-13. Yüzyıllar);Beylikten Devlete;Dünya Gücü: Osmanlı Devleti (1453-1600);Arayış Yılları (17. Yüzyıl);Avrupa ve Osmanlı Devleti (18. Yüzyıl);En Uzun Yüzyıl (1800-1922);Değişen Dünya Dengeleri Karşısında Osmanlı Siyaseti (1595-1774);Değişim Çağında Avrupa ve Osmanlı;Uluslararası İlişkilerde Denge Stratejisi (1774-1914);Devrimler Çağında Değişen Devlet – Toplum İlişkileri;Sermaye ve Emek;19. ve 20. Yüzyılda Değişen Gündelik Hayat;T.C. İnkılap Tarihi ve Atatürkçülük;20. Yüzyıl Başlarında Osmanlı Devleti ve Dünya;Milli Mücadele;Atatürkçülük ve Türk İnkılabı;İki Savaş Arasındaki Dönemde Türkiye ve Dünya;II. Dünya Savaşı Sürecinde Türkiye ve Dünya;II. Dünya Savaşı Sonrasında Türkiye ve Dünya;Toplumsal Devrim Çağında Dünya ve Türkiye;Yüzyılın Eşiğinde Türkiye ve Dünya
AYT|Tarih-2|Tarih ve Zaman;İnsanlığın İlk Dönemleri;Orta Çağ’da Dünya;İlk ve Orta Çağlarda Türk Dünyası;İslam Medeniyetinin Doğuşu;Türklerin İslamiyet’i Kabulü ve İlk Türk İslam Devletleri;Yerleşme ve Devletleşme Sürecinde Selçuklu Türkiyesi;Beylikten Devlete Osmanlı;Dünya Gücü Osmanlı;Değişen Dünya Dengeleri Karşısında Osmanlı Siyaseti;Değişim Çağında Avrupa ve Osmanlı;Uluslararası İlişkilerde Denge Stratejisi (1774-1914);XX. Yüzyıl Başlarında Osmanlı Devleti ve Dünya;Milli Mücadele;Atatürkçülük ve Türk İnkılabı;İki Savaş Arasındaki Dönemde Türkiye ve Dünya;II. Dünya Savaşı Sürecinde Türkiye ve Dünya;II. Dünya Savaşı Sonrasında Türkiye ve Dünya;Soğuk Savaş Dönemi;Yumuşama Dönemi;Küreselleşen Dünya;XXI. Yüzyılın Eşiğinde Türkiye ve Dünya
AYT|Coğrafya-1|Doğal Sistemler;Beşeri Sistemler;Mekânsal Bir Sentez: Türkiye;Küresel Ortam: Bölgeler ve Ülkeler;Çevre ve Toplum;Ekonomik Faaliyetler
AYT|Coğrafya-2|Doğal Sistemler;Beşeri Sistemler;Mekânsal Bir Sentez: Türkiye;Küresel Ortam: Bölgeler ve Ülkeler;Çevre ve Toplum;Ekonomik Faaliyetler
AYT|Felsefe Grubu|Felsefe;Mantık;Psikoloji;Sosyoloji
AYT|Din Kültürü / Ek Felsefe|İnanç;İbadet;Ahlak ve Değerler;Din, Kültür ve Medeniyet;Hz. Muhammed (SAV);Vahiy ve Akıl;Dünya ve Ahiret;Kur’an’a Göre Hz. Muhammed;İnançla İlgili Meseleler;Yahudilik ve Hristiyanlık;İslam ve Bilim;Anadolu’da İslam;İslam Düşüncesinde Tasavvufi Yorumlar;Güncel Dinî Meseleler;Hint ve Çin Dinleri
    """.trimIndent()

    private val childrenRows = """
TYT|Türkçe|Paragraf|Paragrafta Anlatım Teknikleri;Paragrafta Düşünceyi Geliştirme Yolları;Paragrafta Yapı;Paragrafta Konu-Ana Düşünce;Paragrafta Yardımcı Düşünce
TYT|Türkçe|Sözcük Türleri|İsimler;Zamirler;Sıfatlar;Zarflar;Edat – Bağlaç – Ünlem
TYT|Türkçe|Fiiller|Fiilde Anlam (Kip-Kişi-Yapı);Ek Fiil;Fiilimsi;Fiilde Çatı
TYT|Matematik|Problemler|Sayı Problemleri;Kesir Problemleri;Yaş Problemleri;Hareket Hız Problemleri;İşçi Emek Problemleri;Yüzde Problemleri;Kar Zarar Problemleri;Karışım Problemleri;Grafik Problemleri;Rutin Olmayan Problemler
TYT|Geometri|Üçgende Açılar|Özel Üçgenler;Dik Üçgen;İkizkenar Üçgen
TYT|Geometri|Özel Dörtgenler|Dörtgenler;Deltoid;Paralelkenar;Eşkenar Dörtgen;Dikdörtgen;Kare;Yamuk
TYT|Geometri|Çember ve Daire|Çemberde Açı;Çemberde Uzunluk;Dairede Çevre ve Alan
TYT|Geometri|Analitik Geometri|Noktanın Analitiği;Doğrunun Analitiği;Dönüşüm Geometrisi
TYT|Geometri|Katı Cisimler|Prizmalar;Küp;Silindir;Piramit;Koni;Küre
TYT|Fizik|İş, Güç ve Enerji|İş, Enerji ve Güç;Mekanik Enerji;Enerjinin Korunumu ve Dönüşümleri;Verim
TYT|Fizik|Elektrik|Elektrik Yükleri;Akım, Potansiyel Fark, Direnç;Elektrik Devreleri
TYT|Fizik|Manyetizma|Mıknatıslar;Akım ve Manyetik Alan İlişkisi
TYT|Fizik|Dalgalar|Dalga ve Dalga Hareketinin Temel Değişkenleri;Su Dalgası;Deprem Dalgaları ve Depremlerin Özellikleri
TYT|Fizik|Optik|Aydınlanma;Gölge;Yansıma;Düz Aynalar;Küresel Aynalar;Kırılma;Renk;Prizmalar;Mercekler
AYT|Matematik|Kümeler|Kümelerde Temel Kavramlar;Kümelerde İşlemler
AYT|Matematik|Denklem ve Eşitsizlikler|Gerçek Sayılar;Birinci Dereceden Denklem ve Eşitsizlikler;Üslü İfadeler ve Denklemler;Denklem ve Eşitsizliklerle İlgili Uygulamalar
AYT|Matematik|Fonksiyonlar|Fonksiyon Kavramı ve Gösterimi
AYT|Matematik|Veri|Merkezi Eğilim ve Yayılım Ölçüleri;Verilerin Grafikle Gösterilmesi
AYT|Matematik|Olasılık|Basit Olayların Olasılıkları;Koşullu Olasılık;Deneysel ve Teorik Olasılık
AYT|Matematik|Sayma|Sıralama ve Seçme
AYT|Matematik|Fonksiyonlarla İşlemler ve Uygulamalar|Fonksiyonların Simetrileri ve Cebirsel Özellikleri;İki Fonksiyonun Bileşkesi ve Bir Fonksiyonun Tersi
AYT|Matematik|İkinci Dereceden Denklem ve Fonksiyonlar|İkinci Dereceden Bir Bilinmeyenli Denklemler
AYT|Matematik|Polinomlar|Polinom Kavramı ve Polinomlarla İşlemler;Polinomlarda Çarpanlara Ayırma;Polinom ve Rasyonel Denklemlerin Çözüm Kümeleri
AYT|Matematik|Trigonometri|Yönlü Açılar;Trigonometrik Fonksiyonlar;Toplam – Fark ve İki Kat Açı Formülleri;Trigonometrik Denklemler
AYT|Matematik|Analitik Geometri|Doğrunun Analitik İncelenmesi
AYT|Matematik|Fonksiyonlarda Uygulamalar|Fonksiyonlarla İlgili Uygulamalar;İkinci Dereceden Fonksiyonlar ve Grafikleri;Fonksiyonların Dönüşümleri
AYT|Matematik|Denklem ve Eşitsizlik Sistemleri|İkinci Dereceden İki Bilinmeyenli Denklem Sistemleri;İkinci Dereceden Bir Bilinmeyenli Eşitsizlikler ve Eşitsizlik Sistemleri
AYT|Matematik|Üstel ve Logaritmik Fonksiyonlar|Üstel Fonksiyonlar;Logaritma Fonksiyonu;Üstel ve Logaritmik Denklem ve Eşitsizlikler
AYT|Matematik|Diziler|Gerçek Sayı Dizileri
AYT|Matematik|Türev|Limit ve Süreklilik;Anlık Değişim Oranı ve Türev;Türevin Uygulamaları
AYT|Matematik|İntegral|Belirsiz İntegral;Belirli İntegral ve Uygulamaları
AYT|Matematik|Dönüşümler|Analitik Düzlemde Temel Dönüşümler;Çemberin Analitik İncelenmesi
AYT|Geometri|Üçgenler|Üçgenlerin Eşliği;Üçgenlerin Benzerliği;Üçgenin Yardımcı Elemanları;Dik Üçgen ve Trigonometri;Üçgenin Alanı
AYT|Geometri|Dörtgenler ve Çokgenler|Dörtgenler ve Özellikleri;Özel Dörtgenler
AYT|Geometri|Geometrik Cisimler|Katı Cisimlerin Yüzey Alanları ve Hacimleri
AYT|Geometri|Trigonometri|Yönlü Açılar;Trigonometrik Fonksiyonlar;Toplam – Fark ve İki Kat Açı Formülleri;Trigonometrik Denklemler
AYT|Geometri|Analitik Geometri|Doğrunun Analitik İncelenmesi
AYT|Geometri|Çember ve Daire|Çemberin Temel Elemanları;Çemberde Açılar;Çemberde Teğet;Dairenin Çevresi ve Alanı
AYT|Geometri|Uzay Geometri|Katı Cisimler
AYT|Geometri|Dönüşümler|Analitik Düzlemde Temel Dönüşümler;Çemberin Analitik İncelenmesi
AYT|Fizik|Madde ve Özellikleri|Katılar;Akışkanlar
AYT|Fizik|Kuvvet ve Hareket|Bir Boyutta Hareket;Kuvvet;Newton’un Hareket Yasaları;Vektörler;Bağıl Hareket;Bir Boyutta Sabit İvmeli Hareket;İki Boyutta Hareket;Enerji ve Hareket;İtme ve Çizgisel Momentum;Tork;Denge ve Denge Şartları;Basit Makineler
AYT|Fizik|Enerji|İş, Enerji ve Güç;Mekanik Enerji;Enerjinin Korunumu ve Dönüşümleri;Verim
AYT|Fizik|Isı ve Sıcaklık|Isı, Sıcaklık ve İç Enerji;Hal Değişimi;Isıl Denge;Enerji İletim Yolları ve Enerji İletim Hızı;Genleşme
AYT|Fizik|Elektrik ve Manyetizma|Elektrik Yükleri;Akım, Potansiyel Fark, Direnç;Elektrik Devreleri;Mıknatıslar;Akım ve Manyetik Alan İlişkisi;Elektriksel Kuvvet ve Elektrik Alan;Elektriksel Potansiyel;Düzgün Elektrik ve Sığa;Manyetizma ve Elektromanyetik İndükleme;Alternatif Akım;Transformatörler
AYT|Fizik|Dalgalar|Dalga ve Dalga Hareketinin Temel Değişkenleri;Su Dalgası;Deprem Dalgaları ve Depremlerin Özellikleri
AYT|Fizik|Optik|Aydınlanma;Gölge;Yansıma;Düz Aynalar;Küresel Aynalar;Kırılma;Renk;Prizmalar;Mercekler
AYT|Fizik|Çembersel Hareket|Düzgün Çembersel Hareket;Dönerek Öteleme Hareketi;Açısal Momentum;Kütle Çekim Kuvveti;Kepler Kanunları
AYT|Fizik|Dalga Mekaniği|Dalgalarda Kırınım, Girişim ve Doppler Olayı
AYT|Kimya|Kimyasal Tepkimelerde Denge|Kimyasal Denge;Asit-Baz Dengesi;Çözünürlük Dengesi
AYT|Biyoloji|Yaşam Bilimi ve Biyoloji|Bilimsel Bilginin Doğası ve Biyoloji;Canlıların Ortak Özellikleri;Canlıların Yapısında Bulunan Ortak Bileşikler
AYT|Biyoloji|Canlılar Dünyası|Canlılığın Temel Birimi Hücre;Canlıların Çeşitliliği ve Sınıflandırılması;Canlı Alemleri ve Özellikleri
AYT|Biyoloji|Güncel Çevre Sorunları|Güncel Çevre Sorunları ve İnsan;Doğal Kaynaklar ve Biyolojik Çeşitliliğin Korunması
AYT|Biyoloji|Üreme|Mitoz ve Eşeysiz Üreme;Mayoz ve Eşeyli Üreme;Büyüme ve Gelişme
AYT|Biyoloji|Kalıtımın Genel İlkeleri|Kalıtım ve Biyolojik Çeşitlilik
AYT|Biyoloji|Dünyamız|Ekosistem Ekolojisi
AYT|Biyoloji|İnsan Fizyolojisi|Denetleyici ve Düzenleyici Sistem, Duyu Organları;Destek ve Hareket Sistemi;Sindirim Sistemi;Dolaşım Sistemleri;Solunum Sistemi;Üriner Sistem;Üreme Sistemi ve Embriyonik Gelişim
AYT|Biyoloji|Komünite ve Popülasyon Ekolojisi|Komünite Ekolojisi;Popülasyon Ekolojisi
AYT|Biyoloji|Genden Proteine|Nükleik Asitlerin Keşfi ve Önemi;Genetik Şifre ve Protein Sentezi
AYT|Biyoloji|Canlılarda Enerji Dönüşümleri|Canlılık ve Enerji;Fotosentez;Kemosentez;Hücresel Solunum
AYT|Edebiyat|9. Sınıf Metinleri|Hikâye;Şiir;Masal;Fabl;Roman;Tiyatro;Biyografi;Otobiyografi;Mektup;E-Posta;Günlük;Blog
AYT|Edebiyat|10. Sınıf Metinleri|Hikâye;Şiir;Destan;Efsane;Roman;Tiyatro;Anı;Hatıra;Haber Metni;Gezi Yazısı
AYT|Edebiyat|11. Sınıf Metinleri|Hikâye;Şiir;Makale;Sohbet ve Fıkra;Roman;Tiyatro;Eleştiri;Mülakat;Röportaj
AYT|Edebiyat|12. Sınıf Metinleri|Hikâye;Şiir;Roman;Tiyatro;Deneme;Söylev;Nutuk
AYT|Coğrafya-1|Doğal Sistemler|Doğa ve İnsan;Dünya’nın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi;İklim Bilgisi;Yerin Şekillenmesi;Doğanın Varlıkları
AYT|Coğrafya-1|Beşeri Sistemler|Beşeri Yapı;Nüfusun Gelişimi, Dağılışı ve Niteliği;Göçlerin Nedenleri ve Sonuçları;Geçim Tarzları
AYT|Coğrafya-1|Mekânsal Bir Sentez: Türkiye|Türkiye’nin Yeryüzü Şekilleri ve Özellikleri;Türkiye İklimi ve Özellikleri;Türkiye’nin Doğal Varlıkları;Türkiye’de Yerleşme, Nüfus ve Göç
AYT|Coğrafya-1|Küresel Ortam: Bölgeler ve Ülkeler|Bölge Türleri ve Sınırları;Konum ve Etkileşim;Coğrafi Keşifler
AYT|Coğrafya-1|Çevre ve Toplum|Doğa ile İnsan Arasındaki Etkileşim;Doğal Afetler
AYT|Coğrafya-2|Doğal Sistemler|Doğa ve İnsan;Dünya’nın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi;İklim Bilgisi;Yerin Şekillenmesi;Doğanın Varlıkları
AYT|Coğrafya-2|Beşeri Sistemler|Beşeri Yapı;Nüfusun Gelişimi, Dağılışı ve Niteliği;Göçlerin Nedenleri ve Sonuçları;Geçim Tarzları
AYT|Coğrafya-2|Mekânsal Bir Sentez: Türkiye|Ülkemizin Yeryüzü Şekilleri ve Özellikleri;Ülkemizin İklimi ve Özellikleri;Ülkemizin Doğal Varlıkları;Ülkemizin Yerleşme, Nüfus ve Göç
AYT|Coğrafya-2|Küresel Ortam: Bölgeler ve Ülkeler|Bölge Türleri ve Sınırları;Konum ve Etkileşim;Coğrafi Keşifler
AYT|Coğrafya-2|Çevre ve Toplum|Doğa ile İnsan Arasındaki Etkileşim;Doğal Afetler
AYT|Felsefe Grubu|Felsefe|Felsefe’nin Alanı;Bilgi Felsefesi;Bilim Felsefesi;Varlık Felsefesi;Ahlak Felsefesi;Siyaset Felsefesi;Sanat Felsefesi;Din Felsefesi
AYT|Felsefe Grubu|Mantık|Mantığa Giriş;Klasik Mantık;Mantık ve Dil;Sembolik Mantık
AYT|Felsefe Grubu|Psikoloji|Psikoloji Bilimini Tanıyalım;Psikolojinin Temel Süçleri;Öğrenme Bellek Düşünme;Ruh Sağlığının Temelleri
AYT|Felsefe Grubu|Sosyoloji|Sosyolojiye Giriş;Birey ve Toplum;Toplumsal Yapı;Toplumsal Değişme ve Gelişme;Toplum ve Kültür;Toplumsal Kurumlar
    """.trimIndent()
}
