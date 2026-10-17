package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/** Nested headings from user-supplied nine-page TYT/AYT teacher handout.
 * This reference is not the official MEB YKS scope. Unlisted units remain
 * ordinary checkboxes rather than using invented subtopics.
 */
object LearningSubtopics {
    private val catalog: Map<String, List<String>> by lazy {
        val result = linkedMapOf<String, List<String>>()
        rows.trimIndent().lineSequence().filter { it.isNotBlank() }.forEach { row ->
            val parts = row.trim().split('|', limit = 4)
            require(parts.size == 4)
            val id = parts.take(3).joinToString("|") { it.trim() }
            require(id !in result) { "Duplicate subtopic group: $id" }
            result[id] = parts[3].split(';').map { it.trim() }
                .filter { it.isNotBlank() }.distinct()
        }
        result
    }
    fun forOutcome(outcome: LearningOutcome): List<String> =
        catalog["${outcome.examType}|${outcome.subject}|${outcome.title}"].orEmpty()

    private val rows = """
TYT|Fizik|Fiziğin Doğası|Fiziğin Tanımı ve Özellikleri;Fiziğin Alt Dalları;Fiziğin Diğer Disiplinlerle İlişkisi;Fiziksel Niceliklerin Sınıflandırılması;Fizik ve Bilim Araştırma Merkezleri
TYT|Fizik|Madde ve Özellikleri|Madde ve Özkütle;Dayanıklılık;Adezyon ve Kohezyon
TYT|Fizik|Hareket ve Kuvvet|Hareket;Kuvvet;Newton'un Hareket Yasaları;Sürtünme Kuvveti
TYT|Fizik|İş Güç Enerji|İş, Güç ve Enerji;Mekanik Enerji;Enerjinin Korunumu ve Enerji Dönüşümleri;Verim;Enerji Kaynakları
TYT|Fizik|Isı Sıcaklık|Isı ve Sıcaklık;Hal Değişimi;Isıl Denge;Enerji İletim Yolları ve Enerji Tüketim Hızı;Genleşme
TYT|Fizik|Elektrostatik|Elektrik Yükü;Elektrikle Yüklenme Çeşitleri;Elektroskop;İletken ve Yalıtkanlarda Yük Dağılımı;Topraklama;Coulomb Kuvveti;Elektrik Alanı
TYT|Fizik|Elektrik|Elektrik Akımı, Potansiyel Farkı ve Direnci;Elektrik Devreleri
TYT|Fizik|Manyetizma|Mıknatıs ve Manyetik Alan;Akım ve Manyetik Alan
TYT|Fizik|Basınç ve Kaldırma|Basınç;Kaldırma Kuvveti
TYT|Fizik|Dalgalar|Temel Dalga Bilgileri;Yay Dalgası;Su Dalgası;Ses Dalgası;Deprem Dalgaları
TYT|Fizik|Optik|Aydınlanma;Gölge;Yansıma;Düzlem Ayna;Kırılma;Mercekler;Prizmalar
AYT|Fizik|Kuvvet ve Hareket|Vektörler;Bağıl Hareket;Newton'un Hareket Yasaları;Bir Boyutta Sabit İvmeli Hareket;İki Boyutta Sabit İvmeli Hareket;Enerji ve Hareket;İtme ve Çizgisel Momentum;Tork;Denge;Basit Makineler
AYT|Fizik|Elektrik ve Manyetizma|Elektriksel Kuvvet ve Elektrik Alanı;Elektriksel Potansiyel;Düzgün Elektrik Alanı ve Sığa;Manyetizma ve Elektromanyetik İndükleme;Alternatif Akım;Transformatörler
AYT|Fizik|Çembersel Hareket|Düzgün Çembersel Hareket;Dönerek Öteleme Hareketi;Açısal Momentum;Kütle Çekimi ve Kepler Kanunu
AYT|Fizik|Dalga Mekaniği|Dalgalarda Kırınım, Girişim ve Doppler Olayı;Elektromanyetik Dalgalar
AYT|Fizik|Atom Fiziğine Giriş ve Radyoaktivite|Atom Kavramının Tarihsel Gelişimi;Büyük Patlama ve Evrenin Oluşumu;Radyoaktivite
AYT|Fizik|Modern Fizik|Özel Görelilik;Kuantum Fiziğine Giriş;Fotoelektrik Olayı;Compton Saçılması ve De Broglie Dalga Boyu
AYT|Fizik|Modern Fiziğin Teknolojideki Uygulamaları|Görüntüleme Teknolojileri;Yarı İletken Teknolojisi;Süper İletkenler;Nanoteknoloji;Lazer Işınları
TYT|Kimya|Atom ve Periyodik Sistem|Atom ve Yapısı;Periyodik Sistem
TYT|Kimya|Kimyanın Temel Kanunları|Kimyanın Temel Yasaları;Kimyasal Tepkimeler
AYT|Kimya|Kimyasal Tepkimelerde Denge|Kimyasal Denge;Sıvı Çözeltilerde Denge
TYT|Geometri|Üçgenler|Doğruda ve Üçgende Açılar;Dik ve Özel Üçgenler;Dik Üçgende Trigonometrik Bağıntılar;İkizkenar ve Eşkenar Üçgen;Üçgende Alanlar;Üçgende Açıortay Bağıntıları;Üçgende Kenarortay Bağıntıları;Üçgende Eşlik ve Benzerlik;Üçgende Açı-Kenar Bağıntıları
TYT|Geometri|Dörtgenler ve Çokgenler|Çokgenler;Dörtgenler;Yamuk;Paralelkenar;Eşkenar Dörtgen ve Deltoid;Dikdörtgen
TYT|Geometri|Çember ve Daire|Çemberde Açılar;Çemberde Uzunluk;Daire
TYT|Geometri|Analitik Geometri|Koordinat Düzlemi ve Noktanın Analitiği;Doğrunun Analitiği
TYT|Geometri|Katı Cisimler|Prizmalar;Piramitler;Küre
AYT|Geometri|Dönüşüm Geometrisi|Tekrar Eden, Dönen ve Yansıyan Şekiller;Dönüşümlerle Geometri
TYT|Türkçe|Sözcükte Anlam|Sözcük Anlamı;Söz Yorumu;Deyim ve Atasözü
TYT|Türkçe|Cümlede Anlam|Cümle Anlamı;Cümle Yorumu
TYT|Türkçe|Paragraf|Paragrafta Anlatım Teknikleri;Paragrafta Konu ve Ana Düşünce;Paragrafta Yapı;Paragrafta Yardımcı Düşünce
TYT|Türkçe|Sözcükte Yapı|Sözcüğün Yapısı;Sözcük Türleri
TYT|Matematik|Problemler|Sayı Problemleri;Kesir Problemleri;Yaş Problemleri;İşçi-Havuz Problemleri;Hareket Problemleri;Yüzde Problemleri;Kâr-Zarar Problemleri;Karışım Problemleri;Grafik ve Tablo Problemleri
TYT|Matematik|Sayma ve Olasılık|Permütasyon;Kombinasyon;Binom;Olasılık
AYT|Matematik|İkinci Dereceden Denklemler ve Parabol|İkinci Dereceden Denklemler;Parabol
AYT|Matematik|Sayma ve Binom|Permütasyon;Kombinasyon;Binom
AYT|Matematik|Limit ve Süreklilik|Limit;Süreklilik
AYT|Matematik|Diziler|Diziler;Seriler
TYT|Biyoloji|Canlıların Temel Bileşenleri|İnorganik Bileşikler;Organik Bileşikler;Enzimler
TYT|Biyoloji|Hücre|Hücre;Madde Geçişleri
TYT|Biyoloji|Kalıtım|Mendel Genetiği;Kan Grupları;Cinsiyete Bağlı Kalıtım
AYT|Biyoloji|İnsan Fizyolojisi|Sistemler;Duyu Organları;İnsanda Üreme ve Gelişme
AYT|Biyoloji|Genden Proteine|DNA-RNA;Protein Sentezi
AYT|Biyoloji|Canlılarda Enerji Dönüşümleri|Solunum;Fotosentez;Kemosentez
TYT|Tarih|Osmanlı Devleti|Beylikten Devlete (1300-1453);Dünya Gücü Osmanlı Devleti (1453-1600);Arayış Yılları (17. yüzyıl);18. Yüzyılda Değişim ve Diplomasi;En Uzun Yüzyıl (1800-1922)
TYT|Tarih|Millî Mücadele|Millî Mücadele'ye Hazırlık Dönemi;Kurtuluş Savaşında Cepheler
AYT|Tarih-1|Osmanlı Siyasi Tarihi|Beylikten Devlete (1300-1453);Dünya Gücü Osmanlı Devleti (1453-1600);Arayış Yılları (17. Yüzyıl);Avrupa ve Osmanlı Devleti (18. Yüzyıl);En Uzun Yüzyıl (1800-1922)
AYT|Tarih-1|Millî Mücadele|1881'den 1919'a Mustafa Kemal;I. Dünya Savaşı;Millî Mücadele'nin Hazırlık Dönemi;Kurtuluş Savaşı'nda Cepheler
AYT|Tarih-2|Soğuk Savaş|Soğuk Savaş Dönemi;Yumuşama Dönemi ve Sonrası;Küreselleşen Dünya
TYT|Coğrafya|Bölgeler ve Afetler|Ortak Payda: Bölge;Doğal Afetler
AYT|Coğrafya-1|Doğal Sistemler|Doğa ve İnsan;Dünyanın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi;İklim Bilgisi;Yerin Şekillenmesi;Doğanın Varlıkları
AYT|Coğrafya-1|Nüfus Yerleşme|Beşerî Yapı;Nüfusun Gelişimi, Dağılışı ve Niteliği;Göçlerin Nedenleri ve Sonuçları;Geçim Tarzları
AYT|Coğrafya-2|Doğal Sistemler|Doğa ve İnsan;Dünyanın Şekli ve Hareketleri;Coğrafi Konum;Harita Bilgisi;İklim Bilgisi;Yerin Şekillenmesi;Doğanın Varlıkları
AYT|Coğrafya-2|Beşerî Sistemler|Beşerî Yapı;Nüfusun Gelişimi, Dağılışı ve Niteliği;Göçlerin Nedenleri ve Sonuçları;Geçim Tarzları
AYT|Coğrafya-2|Çevre ve Toplum|Doğa ile İnsan Arasındaki Etkileşim;Doğal Afetler;Ekonomik Faaliyetler
AYT|Felsefe Grubu|Mantık|Mantığa Giriş;Klasik Mantık;Mantık ve Dil;Sembolik Mantık
AYT|Felsefe Grubu|Psikoloji|Psikolojinin Temel Süreçleri;Öğrenme Bellek Düşünme;Ruh Sağlığının Temelleri
AYT|Felsefe Grubu|Sosyoloji|Birey ve Toplum;Toplumsal Yapı;Toplumsal Değişme ve Gelişme;Toplum ve Kültür;Toplumsal Kurumlar
TYT|Din Kültürü|Kur'an|Kur'an ve Yorumu;Vahiy ve Akıl;Kur'an Yorumları
TYT|Din Kültürü|İbadet|Zekât;İbadet
TYT|Din Kültürü|İslam Düşüncesinde Yorumlar|İslam Düşüncesinde Yorumlar;İslam Düşüncesinde Tasavvuf
AYT|Din Kültürü / Ek Felsefe|İslam Düşüncesinde Yorumlar|İslam Düşüncesinde Yorumlar;Mezhepler
    """.trimIndent()
}
