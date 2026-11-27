package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.domain.model.LearningOutcome

/**
 * YKS müfredatı: ders > ünite > konu.
 * Ünite ve konu adları uygulamanın öğrenme ekranında doğrudan gösterilir.
 * Konuların altında yapay alt-kazanım listeleri oluşturulmaz.
 */
object LearningKitapsecCatalog {
    private data class Entry(
        val exam: String,
        val subject: String,
        val unit: String,
        val title: String
    )

    private val entries: List<Entry> by lazy {
        rows.trimIndent().lineSequence()
            .filter { it.isNotBlank() }
            .flatMap { row ->
                val parts = row.split('|', limit = 4)
                require(parts.size == 4) { "Geçersiz müfredat satırı: $row" }
                parts[3].split(';')
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .asSequence()
                    .map { title ->
                        Entry(parts[0].trim(), parts[1].trim(), parts[2].trim(), title)
                    }
            }
            .toList()
            .also { list ->
                val ids = list.map { "unit|${it.exam}|${it.subject}|${it.title}" }
                require(ids.distinct().size == ids.size) { "Müfredatta yinelenen konu kimliği var." }
            }
    }

    val starter: List<LearningOutcome> by lazy {
        entries.map { entry ->
            LearningOutcome(
                id = "unit|${entry.exam}|${entry.subject}|${entry.title}",
                examType = entry.exam,
                subject = entry.subject,
                topic = entry.unit,
                title = entry.title
            )
        }
    }

    fun forOutcome(outcome: LearningOutcome): List<String> = emptyList()

    fun expandableIds(): Set<String> = emptySet()

    fun groupedSubjects(): Map<String, Int> =
        entries.groupingBy { it.exam + "/" + it.subject }.eachCount()

    private val rows = """
TYT|Türkçe|Anlam Bilgisi|Sözcükte Anlam;Cümlede Anlam;Paragrafta Anlam ve Yapı;Anlatım Biçimleri ve Düşünceyi Geliştirme Yolları
TYT|Türkçe|Ses ve Yapı Bilgisi|Ses Bilgisi;Sözcükte Yapı ve Ekler
TYT|Türkçe|Yazım ve Noktalama|Yazım Kuralları;Noktalama İşaretleri
TYT|Türkçe|Sözcük Türleri|İsim;Sıfat;Zamir;Zarf;Edat – Bağlaç – Ünlem;Fiiller (Anlam, Kip, Ek Fiil, Çatı);Fiilimsiler
TYT|Türkçe|Cümle Bilgisi|Cümlenin Ögeleri;Cümle Türleri;Anlatım Bozuklukları
TYT|Matematik|Temel Kavramlar ve Sayılar|Temel Kavramlar;Sayı Basamakları;Bölme ve Bölünebilme;EBOB – EKOK;Rasyonel Sayılar
TYT|Matematik|Cebir Temelleri|Basit Eşitsizlikler;Mutlak Değer;Üslü Sayılar;Köklü Sayılar;Çarpanlara Ayırma;Oran – Orantı;Birinci Dereceden Denklemler
TYT|Matematik|Problemler|Sayı ve Kesir Problemleri;Yaş Problemleri;Yüzde Problemleri;Kâr – Zarar Problemleri;Karışım Problemleri;Hareket (Hız) Problemleri;İşçi Problemleri;Grafik ve Tablo Problemleri;Rutin Olmayan Problemler
TYT|Matematik|Mantık, Kümeler ve Veri|Mantık;Kümeler ve Kartezyen Çarpım;Veri ve İstatistik
TYT|Matematik|TYT İleri Cebir ve Sayma|Fonksiyonlar (Temel Kavramlar ve Grafikler);Polinomlar (Temel İşlemler);İkinci Dereceden Denklemler (Temel);Permütasyon;Kombinasyon;Binom;Olasılık
TYT|Geometri|Üçgenler|Doğruda Açılar;Üçgende Açılar;Dik Üçgen ve Özel Üçgenler;İkizkenar Üçgen;Eşkenar Üçgen;Açıortay;Kenarortay;Üçgende Eşlik ve Benzerlik;Üçgende Alan;Açı – Kenar Bağıntıları
TYT|Geometri|Çokgenler ve Dörtgenler|Çokgenler;Genel Dörtgenler ve Deltoid;Paralelkenar;Eşkenar Dörtgen;Dikdörtgen;Kare;Yamuk
TYT|Geometri|Katı Cisimler|Prizmalar;Piramitler;Silindir;Koni;Küre
TYT|Fizik|Fizik Bilimi ve Madde|Fizik Bilimine Giriş;Madde ve Özellikleri
TYT|Fizik|Mekanik|Hareket ve Kuvvet;Newton'un Hareket Yasaları (Temel);İş, Güç ve Enerji (Temel)
TYT|Fizik|Basınç ve Kaldırma Kuvveti|Katı, Sıvı ve Gaz Basıncı;Sıvıların Kaldırma Kuvveti
TYT|Fizik|Isı ve Sıcaklık|Isı, Sıcaklık ve Hal Değişimi;Isı İletimi ve Genleşme
TYT|Fizik|Elektrik ve Manyetizma|Elektrostatik;Elektrik Akımı ve Devreler;Mıknatıslar ve Manyetizma
TYT|Fizik|Dalgalar|Temel Dalga Kavramları ve Yay Dalgaları;Su Dalgaları;Ses ve Deprem Dalgaları
TYT|Fizik|Optik|Aydınlanma ve Gölge;Yansıma ve Düzlem Aynalar;Küresel Aynalar;Kırılma ve Renkler;Mercekler ve Prizmalar
TYT|Kimya|Kimya Temelleri|Kimya Bilimi;Atom ve Periyodik Sistem;Kimyasal Türler Arası Etkileşimler
TYT|Kimya|Madde ve Hesaplamalar|Maddenin Halleri;Doğa ve Kimya;Kimyanın Temel Kanunları;Mol Kavramı;Kimyasal Tepkimeler ve Hesaplamalar
TYT|Kimya|Karışımlar ve Günlük Kimya|Karışımlar ve Ayırma Yöntemleri;Asitler, Bazlar ve Tuzlar;Kimya Her Yerde
TYT|Biyoloji|Yaşam ve Hücre|Canlıların Ortak Özellikleri;Canlıların Temel Bileşenleri;Hücre ve Organelleri;Hücre Zarından Madde Geçişleri
TYT|Biyoloji|Canlılar ve Kalıtım|Canlıların Sınıflandırılması;Mitoz ve Eşeysiz Üreme;Mayoz ve Eşeyli Üreme;Kalıtım ve Soy Ağaçları
TYT|Biyoloji|Ekoloji|Ekosistem Ekolojisi;Güncel Çevre Sorunları
TYT|Tarih|Tarih ve İlk Çağlar|Tarih ve Zaman;İnsanlığın İlk Dönemleri;Orta Çağ'da Dünya
TYT|Tarih|Türk ve İslam Tarihi|İlk ve Orta Çağlarda Türk Dünyası;İslam Medeniyetinin Doğuşu;Türklerin İslamiyet'i Kabulü ve İlk Türk-İslam Devletleri
TYT|Tarih|Osmanlı Tarihi|Beylikten Devlete Osmanlı Siyaseti;Devletleşme Sürecinde Savaşçılar ve Askerler;Beylikten Devlete Osmanlı Medeniyeti;Dünya Gücü Osmanlı;Değişim Çağında Avrupa ve Osmanlı;Uluslararası İlişkilerde Denge Stratejisi (1774-1914)
TYT|Tarih|İnkılap Tarihi|XX. Yüzyıl Başlarında Osmanlı Devleti ve Dünya;Millî Mücadele;Atatürkçülük ve Türk İnkılabı
TYT|Coğrafya|Doğa ve Konum|Doğa ve İnsan;Dünya'nın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi
TYT|Coğrafya|İklim ve Yer Şekilleri|Atmosfer ve İklim;İklim Tipleri;İç Kuvvetler;Dış Kuvvetler;Su, Toprak ve Bitki Varlığı
TYT|Coğrafya|Beşeri Coğrafya ve Afetler|Nüfus ve Yerleşme;Bölgeler ve Ulaşım Yolları;Çevre ve Toplum;Doğal Afetler
TYT|Felsefe|Felsefenin Temelleri|Felsefeyi Tanıma;Felsefe ile Düşünme
TYT|Felsefe|Felsefe Disiplinleri|Bilgi Felsefesi;Varlık Felsefesi;Ahlak Felsefesi;Sanat Felsefesi;Din Felsefesi;Siyaset Felsefesi;Bilim Felsefesi
TYT|Felsefe|Felsefe Tarihi|MÖ 6. Yüzyıl – MS 2. Yüzyıl Felsefesi;MS 2. Yüzyıl – MS 15. Yüzyıl Felsefesi;15. Yüzyıl – 17. Yüzyıl Felsefesi;18. Yüzyıl – 19. Yüzyıl Felsefesi;20. Yüzyıl Felsefesi
TYT|Din Kültürü ve Ahlak Bilgisi|İnanç ve İbadet|Bilgi ve İnanç;Din ve İslam;İslam ve İbadet;Gençlik ve Değerler
TYT|Din Kültürü ve Ahlak Bilgisi|Ahlak ve Düşünce|Gönül Coğrafyamız;Allah – İnsan İlişkisi;Hz. Muhammed (S.A.V.);Vahiy ve Akıl;İslam Düşüncesinde Yorumlar;Yaşayan Dinler
AYT|Matematik|Fonksiyonlar ve Polinomlar|Fonksiyonlarda Dönüşümler;Bileşke ve Ters Fonksiyon;Polinomlarda Bölme ve Çarpanlara Ayırma
AYT|Matematik|Denklem ve Eşitsizlik Sistemleri|İkinci Dereceden Denklemler ve Karmaşık Sayılar;Parabol;Eşitsizlikler ve Eşitsizlik Sistemleri
AYT|Matematik|Logaritma ve Diziler|Üstel ve Logaritmik Fonksiyonlar;Logaritma Özellikleri ve Denklemleri;Aritmetik ve Geometrik Diziler
AYT|Matematik|Trigonometri|Yönlü Açılar ve Trigonometrik Fonksiyonlar;Trigonometrik Özdeşlikler ve İndirgeme;Toplam-Fark ve Yarım Açı Formülleri;Trigonometrik Denklemler
AYT|Matematik|Limit ve Süreklilik|Limit Kavramı ve Özellikleri;Belirsizlik Durumları;Süreklilik
AYT|Matematik|Türev|Türev Alma Kuralları;Türevin Geometrik Yorumu (Teğet ve Normal);Artan-Azalan Fonksiyonlar ve Ekstremum Noktaları;Maksimum – Minimum Problemleri
AYT|Matematik|İntegral|Belirsiz İntegral ve Değişken Değiştirme;Belirli İntegral ve Alan Hesabı
AYT|Geometri|Çember ve Daire|Çemberde Açılar;Çemberde Uzunluk;Dairede Çevre ve Alan
AYT|Geometri|Analitik Geometri|Noktanın Analitik İncelenmesi;Doğrunun Analitik İncelenmesi;Dönüşüm Geometrisi (Öteleme, Dönme, Simetri);Çemberin Analitik İncelenmesi
AYT|Fizik|Kuvvet ve Hareket|Vektörler;Bağıl Hareket;Newton'un Hareket Yasaları;Bir Boyutta Sabit İvmeli Hareket;Atışlar (İki Boyutta Hareket)
AYT|Fizik|Enerji, Momentum ve Denge|İş, Güç ve Enerji;İtme ve Çizgisel Momentum;Tork ve Denge;Kütle ve Ağırlık Merkezi;Basit Makineler
AYT|Fizik|Elektrik ve Manyetizma|Elektriksel Kuvvet ve Elektrik Alan;Elektriksel Potansiyel ve İş;Düzgün Elektrik Alan ve Sığa;Manyetizma ve Manyetik Kuvvet;Elektromanyetik İndükleme;Alternatif Akım;Transformatörler
AYT|Fizik|Çembersel ve Harmonik Hareket|Düzgün Çembersel Hareket;Dönerek Öteleme ve Açısal Momentum;Kütle Çekimi ve Kepler Yasaları;Basit Harmonik Hareket
AYT|Fizik|Dalga Mekaniği|Dalgalarda Kırınım, Girişim ve Doppler Olayı;Elektromanyetik Dalgalar
AYT|Fizik|Modern Fizik|Atom Modelleri ve Büyük Patlama;Radyoaktivite;Özel Görelilik;Siyah Cisim Işıması ve Fotoelektrik Olay;Compton Saçılması ve De Broglie Dalga Boyu;Modern Fiziğin Teknolojideki Uygulamaları
AYT|Kimya|Modern Atom ve Gazlar|Modern Atom Teorisi ve Periyodik Özellikler;Gaz Yasaları ve İdeal Gaz Denklemi;Gaz Karışımları ve Gerçek Gazlar
AYT|Kimya|Sıvı Çözeltiler ve Enerji|Derişim Birimleri ve Koligatif Özellikler;Çözünürlük;Kimyasal Tepkimelerde Enerji (Entalpi ve Hess Yasası)
AYT|Kimya|Hız ve Denge|Kimyasal Tepkimelerde Hız;Kimyasal Denge (Kc, Kp ve Le Chatelier);Asit-Baz Dengesi (pH, Tampon, Titrasyon);Çözünürlük Dengesi (Kçç)
AYT|Kimya|Kimya ve Elektrik|İndirgenme-Yükseltgenme (Redoks) ve Aktiflik;Elektrokimyasal Piller (Galvanik Hücreler);Elektroliz ve Korozyon
AYT|Kimya|Organik Kimya|Karbon Kimyasına Giriş ve Hibritleşme;Alkanlar;Alkenler;Alkinler;Aromatik Bileşikler;Alkoller ve Eterler;Aldehitler ve Ketonlar;Karboksilik Asitler ve Esterler
AYT|Biyoloji|İnsan Fizyolojisi (Sistemler)|Sinir Sistemi;Duyu Organları;Endokrin Sistem;Destek ve Hareket Sistemi;Sindirim Sistemi;Dolaşım ve Bağışıklık Sistemi;Solunum Sistemi;Üriner Sistem;Üreme Sistemi ve Embriyonik Gelişim
AYT|Biyoloji|Komünite ve Popülasyon|Komünite Ekolojisi;Popülasyon Ekolojisi
AYT|Biyoloji|Genden Proteine|Nükleik Asitler (DNA – RNA);Genetik Şifre ve Protein Sentezi;Biyoteknoloji ve Genetik Mühendisliği
AYT|Biyoloji|Canlılarda Enerji Dönüşümleri|Canlılık ve ATP;Fotosentez ve Kemosentez;Hücresel Solunum ve Fermantasyon
AYT|Biyoloji|Bitki Biyolojisi|Bitkilerin Yapısı;Bitkilerde Taşıma, Beslenme ve Büyüme;Bitkilerde Eşeyli Üreme
AYT|Biyoloji|Canlılar ve Çevre|Canlılar ve Çevre (Adaptasyon ve Doğal Seçilim)
"""
}