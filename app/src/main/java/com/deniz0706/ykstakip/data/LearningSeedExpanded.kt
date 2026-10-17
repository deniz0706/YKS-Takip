package com.deniz0706.ykstakip.data

import com.deniz0706.ykstakip.model.LearningOutcome

/**
 * Extended YKS topic/skill tracker, NOT verbatim MEB/ÖSYM learning outcomes.
 * Stable topic-index identifiers are deliberately independent of the old starter
 * IDs to preserve existing user's checkmarks, wrong counts and attached questions.
 */
internal object LearningSeedExpanded {
    private val catalog = """
TYT|Türkçe|Sözcükte Anlam|Gerçek ve mecaz anlamı bağlamda ayırt eder;Deyim atasözü ve terim anlamı yorumlar;Sözcükler arası anlam ilişkisi kurar
TYT|Türkçe|Cümlede Anlam|Neden ve amaç sonuç ilişkilerini çözümler;Koşul karşılaştırma ve varsayımı ayırt eder;Cümleden çıkarılabilen yargıyı belirler
TYT|Türkçe|Paragrafta Anlam|Ana düşünce ve konuyu ayırt eder;Yardımcı düşünce ve çıkarımı belirler;Yazarın tavrını ve bakış açısını yorumlar
TYT|Türkçe|Paragrafta Yapı|Giriş gelişme sonuç düzenini kurar;Akışı bozan cümleyi saptar;İki paragrafa bölme noktasını bulur
TYT|Türkçe|Anlatım Biçimleri|Açıklayıcı ve tartışmacı anlatımı ayırt eder;Öyküleme ve betimleme tekniklerini tanır;Anlatıcı ve anlatım özelliklerini yorumlar
TYT|Türkçe|Düşünceyi Geliştirme|Örnekleme ve tanık göstermeyi bulur;Benzetme ve karşılaştırmayı ayırt eder;Tanımlama ve sayısal verileri yorumlar
TYT|Türkçe|Ses Bilgisi|Ünlü düşmesi ve daralmasını belirler;Ünsüz benzeşmesi ve yumuşamasını ayırt eder;Kaynaştırma ve ses türemesini yorumlar
TYT|Türkçe|Yazım Kuralları|Büyük harf ve birleşik sözcük kurallarını uygular;De da ve ki yazımını ayırt eder;Sayı ve kısaltma yazımını uygular
TYT|Türkçe|Noktalama|Virgül ve noktalı virgülün görevlerini ayırt eder;İki nokta ve üç noktayı uygular;Kesme tırnak ve çizgi kullanımını çözer
TYT|Türkçe|Sözcükte Yapı|Kök gövde yapım eklerini ayırt eder;Çekim eklerinin görevlerini belirler;Basit türemiş ve birleşik sözcüğü ayırt eder
TYT|Türkçe|İsim Sıfat Tamlamaları|İsim ve sıfat türlerini ayırt eder;İsim ve sıfat tamlamalarını çözümler;Adlaşmış sıfatları yorumlar
TYT|Türkçe|Zamir Zarf Edat|Zamirlerin işlevlerini belirler;Zarf türlerini ve görevlerini ayırt eder;Edat bağlaç ve ünlemleri tanır
TYT|Türkçe|Fiil ve Kip|Haber ve dilek kiplerini ayırt eder;Fiilde kişi zaman ve anlam kaymasını yorumlar;Çatıya göre fiilleri sınıflandırır
TYT|Türkçe|Fiilimsiler|İsim fiil sıfat fiil zarf fiili ayırt eder;Fiilimsi eklerini belirler;Fiilimsi gruplarının görevlerini yorumlar
TYT|Türkçe|Cümlenin Ögeleri|Özne ve yüklemi bulur;Nesne ve dolaylı tümleci ayırt eder;Zarf tümlecini belirler
TYT|Türkçe|Cümle Türleri|Yüklemine göre cümle türlerini ayırt eder;Yüklemin yerine göre sınıflandırır;Yapısına ve anlamına göre cümle çözümler
TYT|Türkçe|Anlatım Bozuklukları|Anlam ve mantık bozukluğunu saptar;Özne yüklem uyumsuzluğunu belirler;Ek ve tamlama yanlışlarını düzeltir
TYT|Türkçe|Sözel Mantık|Sıralama koşullarını tabloya aktarır;Eşleştirme mantığını uygular;Kesin ve olası çıkarımı ayırt eder
TYT|Matematik|Sayı Kümeleri|Doğal tam ve rasyonel sayıları karşılaştırır;Tek çift ve ardışık sayılarla işlem yapar;Asallık ve asal çarpan yöntemlerini uygular
TYT|Matematik|Sayı Basamakları|Rakam ve basamak değerini ilişkilendirir;Sayı çözümlemesi yapar;Basamak koşullarıyla bilinmeyen bulur
TYT|Matematik|Bölme Bölünebilme|Bölünebilme kurallarını uygular;Kalanlı bölme bağıntısı kurar;Çok koşullu kalan problemleri çözer
TYT|Matematik|EBOB EKOK|EBOB hesaplar ve yorumlar;EKOK periyot problemleri çözer;EBOB EKOK oranı üzerinden sayı bulur
TYT|Matematik|Rasyonel Sayılar|Kesirlerde dört işlem yapar;Kesirleri karşılaştırır;Devirli ondalık gösterime dönüştürür
TYT|Matematik|Basit Eşitsizlikler|Birinci dereceden eşitsizlik çözer;İşaret dönüşümünü uygular;Aralık gösterimi yapar
TYT|Matematik|Mutlak Değer|Uzaklık anlamını kullanır;Mutlak değerli denklemi çözer;Mutlak değerli eşitsizliği çözer
TYT|Matematik|Üslü Sayılar|Üslü ifadelerin özelliklerini uygular;Negatif ve rasyonel üstleri yorumlar;Üslü ifadeleri karşılaştırır
TYT|Matematik|Köklü Sayılar|Köklü ifadeleri sadeleştirir;Köklü ifadelerle işlem yapar;Köklü ifadeleri sıralar
TYT|Matematik|Çarpanlara Ayırma|Ortak çarpan ve gruplamayı kullanır;Özdeşliklerle çarpanlara ayırır;İkinci dereceden ifadeyi ayırır
TYT|Matematik|Oran Orantı|Doğru ve ters orantı kurar;Bileşik orantı çözer;Ölçek uygulamaları yapar
TYT|Matematik|Denklemler|Birinci derece denklem çözer;Denklem sistemi çözer;Sözel ifadeyi cebirsel modele dönüştürür
TYT|Matematik|Kümeler|Alt küme sayısını hesaplar;Birleşim kesişim fark işlemlerini uygular;Venn diyagramı kullanır
TYT|Matematik|Mantık|Önerme doğruluk değerlerini hesaplar;Mantık bağlaçlarını kullanır;Koşullu önerme ve denkliği yorumlar
TYT|Matematik|Fonksiyon Temelleri|Fonksiyon olma şartını kontrol eder;Tanım ve görüntü kümesi bulur;Fonksiyonlarda işlem yapar
TYT|Matematik|Sayı Problemleri|Sayı ilişkilerini denklemle modeller;Rakam problemlerini çözer;Ardışık sayılarda toplam bulur
TYT|Matematik|Kesir Problemleri|Bütün parça ilişkisini kurar;İşlem sonrası kalan miktarı hesaplar;Kesirli miktarlara dair problem çözer
TYT|Matematik|Yaş Problemleri|Yaş farkının sabitliğini kullanır;Geçmiş ve gelecek yaşı hesaplar;Aile içi yaş problemleri çözer
TYT|Matematik|İşçi Havuz Problemleri|Bireysel iş hızını hesaplar;Birlikte iş yapma süresini bulur;Dolma boşalma problemlerini çözer
TYT|Matematik|Hareket Problemleri|Yol hız zaman bağıntısı kurar;Karşılaşma ve yetişmeyi çözer;Akıntı ve araç problemlerini modeller
TYT|Matematik|Yüzde Problemleri|Yüzde artış azalışı hesaplar;Ardışık yüzde değişimlerini uygular;Yüzdeden başlangıç miktarını bulur
TYT|Matematik|Kâr Zarar Problemleri|Maliyet ve satış arasındaki ilişkiyi kurar;İndirim ve kâr oranını hesaplar;Vergi ve fiyat problemleri çözer
TYT|Matematik|Karışım Problemleri|Yüzde derişim hesabı yapar;İki karışımın oranını bulur;Su ilavesiyle derişimi hesaplar
TYT|Matematik|Grafik Tablo Problemleri|Grafikteki verileri yorumlar;Pasta ve sütun grafiklerinde oran bulur;Tablodan denklem kurar
TYT|Matematik|Sayma ve Olasılık|Çarpma ve toplama ilkelerini kullanır;Permütasyon kombinasyon hesabı yapar;Basit olasılık çözer
TYT|Matematik|İstatistik|Ortalama medyan mod hesaplar;Veri dağılımını yorumlar;Açıklık ve değişkenliği hesaplar
TYT|Geometri|Doğruda Açılar|Tümler bütünler ve ters açıları uygular;Paralel doğruda açı hesaplar;Açıortay özelliklerini kullanır
TYT|Geometri|Üçgende Açılar|İç ve dış açı toplamını kullanır;Açı kenar ilişkisini uygular;Özel üçgenlerin açılarını yorumlar
TYT|Geometri|Üçgende Kenar|Üçgen eşitsizliğini uygular;Özel üçgenlerde kenar bulur;Kenar uzunluklarını karşılaştırır
TYT|Geometri|Açıortay|İç açıortay teoremini uygular;Dış açıortay teoremini kullanır;Açıortay uzunluğu hesaplar
TYT|Geometri|Kenarortay|Ağırlık merkezini bulur;Kenarortay bağıntıları uygular;Kenarortay ve alan ilişkisini yorumlar
TYT|Geometri|Benzerlik|Benzerlik oranı bulur;Benzer üçgenlerde uzunluk çözer;Alan oranı kullanır
TYT|Geometri|Üçgende Alan|Taban yükseklik bağıntısı kurar;Alan oranlarını kullanır;Sinüslü alan hesabı yapar
TYT|Geometri|Çokgenler|İç dış açı toplamını hesaplar;Köşegen sayısı bulur;Düzgün çokgenleri çözer
TYT|Geometri|Dörtgenler|Dörtgen açılarını hesaplar;Dörtgenlerin özelliklerini ayırt eder;Dörtgen alanı bulur
TYT|Geometri|Paralelkenar|Kenar ve köşegen özelliklerini uygular;Paralelkenarda alan hesaplar;Özel durumları çözer
TYT|Geometri|Dikdörtgen ve Kare|Köşegen uzunluğunu hesaplar;Kare ve dikdörtgen alanı bulur;Alan paylaşımı soruları çözer
TYT|Geometri|Yamuk|Orta taban uzunluğu hesaplar;Yamuk alanı bulur;İkizkenar yamuğu yorumlar
TYT|Geometri|Eşkenar Dörtgen ve Deltoid|Köşegenleri kullanır;Alan ilişkileri uygular;Açı bağıntıları çözer
TYT|Geometri|Çemberde Açılar|Merkez çevre açıları hesaplar;Teğet kiriş açısı bulur;Çemberde dörtgeni yorumlar
TYT|Geometri|Çemberde Uzunluk|Kiriş uzunluğunu hesaplar;Kesen ve teğet bağıntısı kurar;Çemberin kuvvetini uygular
TYT|Geometri|Daire|Daire alan ve çevresini hesaplar;Daire dilimi alanı bulur;Taralı bölge çözer
TYT|Geometri|Analitik Geometri|Noktalar arası uzaklık bulur;Eğim hesaplar;Doğru denklemi yazar
TYT|Geometri|Katı Cisimler|Prizma hacim ve alanı hesaplar;Silindir hacim ve alanı bulur;Koni küre piramidi karşılaştırır
TYT|Tarih|Tarih Bilimi|Tarihsel kanıtları ayırt eder;Olay ve olgu ayrımını yapar;Takvim ve çağ kavramlarını yorumlar
TYT|Tarih|İlk Çağ|Mezopotamya uygarlıklarını ayırt eder;Anadolu uygarlıklarını karşılaştırır;İlk çağ yönetimini yorumlar
TYT|Tarih|İlk Türk Devletleri|Hun Göktürk Uygur özelliklerini açıklar;Töre kut kurultayı ayırt eder;Göç ve yaşam biçimini yorumlar
TYT|Tarih|İslam Tarihi|Hicret ve halifeler dönemini yorumlar;Emevi Abbasi dönemini karşılaştırır;İslam tarihindeki siyasi değişimleri açıklar
TYT|Tarih|Türk İslam Devletleri|Karahanlı ve Gaznelileri tanır;Büyük Selçuklu devletini yorumlar;İkta ve gulam sistemlerini açıklar
TYT|Tarih|Türkiye Selçukluları|Malazgirt sonrası Anadolu gelişmelerini açıklar;Haçlı seferlerinin etkilerini yorumlar;Anadolu Selçuklu kültürünü ayırt eder
TYT|Tarih|Osmanlı Kuruluş|Kuruluş ve fetih politikasını yorumlar;Ankara Savaşı sonuçlarını açıklar;Rumeli iskânını yorumlar
TYT|Tarih|Osmanlı Yükseliş|İstanbul fethini yorumlar;Merkez ve taşra yönetimini ayırt eder;Tımar sistemini açıklar
TYT|Tarih|Osmanlı Islahatları|17. yüzyıl ıslahatlarını yorumlar;Lale Devri yeniliklerini açıklar;Tanzimat ve Islahat Fermanlarını karşılaştırır
TYT|Tarih|19. Yüzyıl Osmanlı|Meşrutiyetleri karşılaştırır;Osmanlı fikir akımlarını yorumlar;Dağılma dönemi savaşlarını açıklar
TYT|Tarih|Trablusgarp ve Balkan Savaşları|Trablusgarp Savaşı nedenlerini açıklar;Balkan savaşlarını karşılaştırır;Savaşların Osmanlı siyasetini yorumlar
TYT|Tarih|I. Dünya Savaşı|Osmanlı cephelerini ayırt eder;Mondros Ateşkesini yorumlar;Savaş sonrası işgalleri açıklar
TYT|Tarih|Millî Mücadele Hazırlık|Amasya Erzurum Sivas kararlarını karşılaştırır;Milli cemiyetleri ayırt eder;Misakımillî ve TBMM gelişmelerini yorumlar
TYT|Tarih|Kurtuluş Savaşı|İnönü ve Sakarya savaşlarını sıralar;Büyük Taarruzun sonucunu açıklar;Lozan ve Mudanya süreçlerini ayırt eder
TYT|Tarih|Atatürk İlkeleri ve İnkılaplar|Altı ilkeyi örneklerle ayırt eder;Siyasi hukuk eğitim yeniliklerini açıklar;Dış politika gelişmelerini yorumlar
TYT|Coğrafya|Doğa ve İnsan|Coğrafyanın bölümlerini ayırt eder;İnsan doğa ilişkisini yorumlar;Coğrafi dağılış ilkelerini uygular
TYT|Coğrafya|Harita Bilgisi|Ölçekten uzunluk hesaplar;İzohipste eğim bulur;Harita yorumlar
TYT|Coğrafya|Dünya Hareketleri|Enlem ve boylam etkisini yorumlar;Yerel saat hesaplar;Mevsim oluşumunu açıklar
TYT|Coğrafya|Atmosfer ve Sıcaklık|Atmosfer katmanlarını ayırt eder;Sıcaklık etkileyen faktörleri yorumlar;Sıcaklık haritalarını açıklar
TYT|Coğrafya|Basınç ve Rüzgâr|Basınç alanlarını ayırt eder;Rüzgâr hızı ve yönünü yorumlar;Yerel rüzgârları tanır
TYT|Coğrafya|Nem ve Yağış|Bağıl ve mutlak nemi ayırt eder;Yağış oluşumunu yorumlar;Yağış dağılışını açıklar
TYT|Coğrafya|İklim Tipleri|Küresel iklimleri ayırt eder;Türkiye iklimlerini yorumlar;İklim grafiği analiz eder
TYT|Coğrafya|İç Kuvvetler|Deprem ve fayları açıklar;Orojenez ve epirojenezi ayırt eder;Volkanizmayı yorumlar
TYT|Coğrafya|Dış Kuvvetler|Akarsu yer şekillerini yorumlar;Rüzgâr buzul ve karst şekillerini ayırt eder;Kıyı şekillerini sınıflandırır
TYT|Coğrafya|Su ve Toprak|Göl türlerini ayırt eder;Akarsu rejimini yorumlar;Toprak tiplerini açıklar
TYT|Coğrafya|Nüfus ve Göç|Nüfus piramitlerini yorumlar;Göç nedenlerini açıklar;Nüfus dağılışını değerlendirir
TYT|Coğrafya|Yerleşme ve Ekonomi|Yerleşme türlerini karşılaştırır;Tarım ve sanayi faaliyetlerini yorumlar;Ulaşım ve turizmi açıklar
TYT|Coğrafya|Bölgeler ve Afetler|Bölge türlerini ayırt eder;Doğal afet risklerini yorumlar;Sürdürülebilir kaynak yönetimini açıklar
TYT|Felsefe|Felsefeye Giriş|Felsefi soruyu tanır;Felsefenin özelliklerini ayırt eder;Felsefi disiplinleri sınıflandırır
TYT|Felsefe|Bilgi Felsefesi|Rasyonalizm empirizmi karşılaştırır;Şüphecilik ve kritisizmi yorumlar;Doğru bilgi sorununu açıklar
TYT|Felsefe|Varlık Felsefesi|İdealizm ve materyalizmi ayırt eder;Varlık ve oluş yaklaşımlarını yorumlar;Ontolojik soruları belirler
TYT|Felsefe|Ahlak Felsefesi|Determinizm ve özgür iradeyi ayırt eder;Haz fayda ödev ahlakını yorumlar;Evrensel ahlak görüşlerini değerlendirir
TYT|Felsefe|Siyaset Felsefesi|Adalet ve özgürlük ilişkisini açıklar;İdeal düzen görüşlerini ayırt eder;Devletin kaynağına ilişkin yaklaşımları yorumlar
TYT|Felsefe|Sanat Felsefesi|Güzellik ve estetik yargıyı ayırt eder;Taklit ve yaratmayı karşılaştırır;Sanat ve zanaati ayırt eder
TYT|Felsefe|Din Felsefesi|Teizm deizm ateizm agnostisizmi ayırt eder;Tanrının varlığına ilişkin argümanları yorumlar;Kötülük problemini açıklar
TYT|Felsefe|Bilim Felsefesi|Bilimsel yöntem ve sınırları açıklar;Popper ve Kuhn görüşlerini ayırt eder;Doğrulama ve yanlışlamayı yorumlar
TYT|Din Kültürü|İnanç|İman esaslarını ayırt eder;Akıl ve inanç ilişkisini yorumlar;İnançla ilgili temel kavramları açıklar
TYT|Din Kültürü|İbadet|İbadetlerin amaçlarını açıklar;İbadet ve ahlak ilişkisini kurar;Hak ve sorumluluğu yorumlar
TYT|Din Kültürü|Hz. Muhammed|Mekke Medine dönemini ayırt eder;Peygamberin örnek davranışlarını açıklar;Sünnet kavramını yorumlar
TYT|Din Kültürü|Kur'an|Kur'an'ın temel konularını ayırt eder;Ayetlerdeki ana mesajı yorumlar;Sure ve ayet kavramlarını açıklar
TYT|Fizik|Fiziğin Doğası|Temel ve türetilmiş büyüklükleri ayırt eder;Skaler ve vektörel büyüklükleri karşılaştırır;Birim dönüşümlerini yapar
TYT|Fizik|Madde ve Özellikleri|Özkütle hesaplar;Yüzey gerilimi ve kılcallığı açıklar;Genleşmeyi yorumlar
TYT|Fizik|Hareket|Yol ve yer değiştirmeyi karşılaştırır;Konum zaman grafiğini yorumlar;Hız ve ivme hesaplar
TYT|Fizik|Kuvvet|Newton yasalarını uygular;Sürtünme kuvvetini hesaplar;Kuvvet dengesi kurar
TYT|Fizik|İş Güç Enerji|İş ve güç hesaplar;Enerji korunumu uygular;Enerji dönüşümünü yorumlar
TYT|Fizik|Isı Sıcaklık|Özısı ve ısı hesabı yapar;Isıl dengeyi yorumlar;Hal değişimlerini açıklar
TYT|Fizik|Basınç ve Kaldırma|Katı sıvı gaz basıncını ayırt eder;Pascal ilkesini uygular;Arşimet ilkesini çözer
TYT|Fizik|Elektrostatik|Yüklenme yollarını ayırt eder;Elektroskopu yorumlar;Yük korunumu kullanır
TYT|Fizik|Elektrik Devreleri|Akım gerilim direnç hesaplar;Seri paralel devre kurar;Elektriksel güç hesaplar
TYT|Fizik|Manyetizma|Manyetik alanı tanır;Mıknatıs etkileşimini açıklar;Akımın manyetik etkisini yorumlar
TYT|Fizik|Dalgalar|Dalga hızı frekans periyot bağıntısı kurar;Atma ve yay dalgasını yorumlar;Ses dalgalarını karşılaştırır
TYT|Fizik|Optik|Yansıma ve kırılma yorumlar;Ayna çeşitlerini ayırt eder;Mercekleri ve görüntü oluşumunu çözer
TYT|Kimya|Kimya Bilimi|Alt disiplinleri ayırt eder;Laboratuvar güvenliğini uygular;Element bileşik karışımı sınıflandırır
TYT|Kimya|Atom Modelleri|Atom modellerini karşılaştırır;Temel tanecikleri hesaplar;İzotop izobar izotonu ayırt eder
TYT|Kimya|Periyodik Sistem|Grup ve periyot belirler;Periyodik eğilimleri yorumlar;Metal ametal ve soygazı ayırt eder
TYT|Kimya|Kimyasal Türler|İyon ve molekülü ayırt eder;İyonik kovalent metalik bağı karşılaştırır;Zayıf etkileşimleri tanır
TYT|Kimya|Maddenin Halleri|Hal değişimlerini yorumlar;Kaynama ve buhar basıncını açıklar;Katı sıvı gazları ayırt eder
TYT|Kimya|Gazlar|Gazların temel özelliklerini tanır;Basınç hacim sıcaklık ilişkisini yorumlar;Gaz karışımlarını açıklar
TYT|Kimya|Karışımlar|Homojen heterojen karışımı ayırt eder;Ayırma yöntemlerini seçer;Derişim hesaplar
TYT|Kimya|Asit Baz Tuz|Asit ve baz özelliklerini karşılaştırır;pH ve pOH yorumlar;Nötralleşme tepkimesini açıklar
TYT|Kimya|Kimya Her Yerde|Günlük hayatta temizlik maddelerini ayırt eder;Polimer ve kozmetiği yorumlar;Gıda kimyasını açıklar
TYT|Kimya|Mol Hesapları|Mol kütle tanecik ilişkisini kurar;Tepkime denkleştirir;Kütle korunumu hesaplar
TYT|Biyoloji|Canlıların Ortak Özellikleri|Metabolizma ve homeostazı yorumlar;Canlı organizasyonunu sıralar;Üreme ve adaptasyonu açıklar
TYT|Biyoloji|İnorganik Bileşikler|Su ve mineralleri açıklar;Mineral eksikliği durumunu yorumlar;Asit baz ve tuzları ayırt eder
TYT|Biyoloji|Organik Bileşikler|Karbonhidrat lipid protein karşılaştırır;Vitamin ve enzim görevlerini açıklar;Nükleik asitleri tanır
TYT|Biyoloji|Enzimler|Aktivasyon enerjisini yorumlar;Enzim hızına etki eden etkenleri açıklar;İnhibisyon ve özgüllüğü ayırt eder
TYT|Biyoloji|Hücre|Organellerin görevlerini karşılaştırır;Bitki hayvan hücresini ayırt eder;Prokaryot ökaryotu açıklar
TYT|Biyoloji|Madde Geçişleri|Difüzyon ve osmozu yorumlar;Aktif taşımayı ayırt eder;Endositoz ve ekzositozu karşılaştırır
TYT|Biyoloji|Sınıflandırma|Taksonomi basamaklarını uygular;Canlı âlemlerini sınıflandırır;Akrabalık ilişkisi yorumlar
TYT|Biyoloji|Hücre Bölünmeleri|Mitoz mayoz karşılaştırır;Kromozom sayısını yorumlar;Bölünme evrelerini sıralar
TYT|Biyoloji|Kalıtım|Mendel yasalarını uygular;Çaprazlama yapar;Eş baskınlık ve çok alelliliği yorumlar
TYT|Biyoloji|Ekoloji|Popülasyon ve komüniteyi ayırt eder;Besin ağını yorumlar;Madde döngülerini açıklar
AYT|Matematik|Fonksiyonlar|Bire bir ve örten fonksiyonu ayırt eder;Ters ve bileşke fonksiyon kurar;Grafik dönüşümlerini uygular
AYT|Matematik|İkinci Dereceden Denklemler|Diskriminantla kök sayısı belirler;Kökler toplamı çarpımı uygular;Parametreli denklem çözer
AYT|Matematik|Parabol|Tepe noktası bulur;Grafik kesişimlerini hesaplar;Maksimum minimum problemleri çözer
AYT|Matematik|Polinomlar|Kalan ve çarpan teoremi uygular;Polinomlarda özdeşlik kurar;Polinom bölmesi yapar
AYT|Matematik|Karmaşık Sayılar|Sanal birim işlemleri yapar;Karmaşık eşlenik ve modül hesaplar;Karmaşık denklemleri çözer
AYT|Matematik|Eşitsizlik Sistemleri|İkinci derece eşitsizlik çözer;Rasyonel eşitsizlikte işaret tablosu kurar;Eşitsizlik sistemini yorumlar
AYT|Matematik|Trigonometri Temelleri|Birim çemberi kullanır;Trigonometrik oranları hesaplar;Özdeşlikleri uygular
AYT|Matematik|Trigonometrik Denklemler|Trigonometrik denklemleri çözer;Periyodu hesaplar;Toplam fark açı formüllerini uygular
AYT|Matematik|Trigonometrik Grafikler|Sinüs kosinüs grafiklerini çizer;Genlik ve periyodu yorumlar;Ters trigonometrik fonksiyonları tanır
AYT|Matematik|Logaritma|Tanım koşullarını uygular;Taban değiştirme hesaplar;Logaritmik denklem çözer
AYT|Matematik|Üstel Fonksiyon|Üstel fonksiyonun grafiğini yorumlar;Üstel eşitsizlik çözer;Üstel değişim modeli kurar
AYT|Matematik|Diziler|Aritmetik dizide n. terimi bulur;Geometrik dizi toplamı hesaplar;Özyinelemeli dizileri yorumlar
AYT|Matematik|Seriler|Sigma gösterimini uygular;Aritmetik toplamları hesaplar;Geometrik toplamları hesaplar
AYT|Matematik|Permütasyon Kombinasyon|Sıralama ve seçme hesabı yapar;Tekrarlı permütasyonu uygular;Kombinasyon özdeşliklerini kullanır
AYT|Matematik|Binom|Binom katsayısı bulur;Terim ve katsayı hesaplar;Binom özdeşlikleri uygular
AYT|Matematik|Olasılık|Koşullu olasılık hesaplar;Bağımsız olayları ayırt eder;Olasılık ağacı kurar
AYT|Matematik|Limit|Sağdan soldan limit bulur;Belirsizlikleri giderir;Grafikten limit yorumlar
AYT|Matematik|Süreklilik|Süreklilik koşullarını uygular;Parametreli süreklilik soruları çözer;Süreksizlik türlerini ayırt eder
AYT|Matematik|Türev|Türev kurallarını uygular;Zincir çarpım bölüm kuralı kullanır;Türevin geometrik anlamını yorumlar
AYT|Matematik|Türev Uygulamaları|Artma azalma aralıklarını bulur;Ekstremum hesaplar;Teğet normal ve optimizasyon çözer
AYT|Matematik|İntegral|Belirsiz integral alır;Değişken değiştirme yapar;Belirli integral hesaplar
AYT|Matematik|Alan İntegrali|İki eğri arasında alan bulur;İşaretli alanı yorumlar;Alanı parçalara ayırarak hesaplar
AYT|Matematik|Analitik Geometri|Doğru denklemi oluşturur;Noktanın doğruya uzaklığı bulur;Doğrular arası açıyı hesaplar
AYT|Geometri|Çemberin Analitiği|Çember denklemini kurar;Merkez ve yarıçapı bulur;Doğru çember konumunu yorumlar
AYT|Geometri|Dönüşüm Geometrisi|Öteleme ve yansımayı uygular;Dönme dönüşümü yapar;Bileşik dönüşümü çözer
AYT|Geometri|Uzay Geometri|Doğru düzlem ilişkisini yorumlar;Katı cisim kesitlerini hesaplar;Uzayda açı ve uzaklık bulur
AYT|Fizik|Vektörler|Bileşenlerine ayırır;Vektörleri toplar;Yön ve birim vektörü yorumlar
AYT|Fizik|Bağıl Hareket|Bir boyutta bağıl hız hesaplar;İki boyutta bağıl hız bulur;Nehir rüzgâr problemleri çözer
AYT|Fizik|Newton Yasaları|Serbest cisim diyagramı çizer;Sürtünmeli sistem çözer;Eğik düzlemde ivme bulur
AYT|Fizik|Sabit İvmeli Hareket|Hareket denklemlerini uygular;Konum hız ivme grafiklerini çözer;Bağıl ivme yorumlar
AYT|Fizik|Atışlar|Yatay atış hesabı yapar;Eğik atış menzilini hesaplar;Uçuş süresi ve yükseklik bulur
AYT|Fizik|Enerji|İş enerji teoremi uygular;Enerji korunumu kullanır;Potansiyel enerjiyi yorumlar
AYT|Fizik|İtme Momentum|İtme ve momentum hesaplar;Momentum korunumu uygular;Çarpışmaları karşılaştırır
AYT|Fizik|Tork Denge|Torku hesaplar;Denge koşullarını uygular;Kütle merkezini bulur
AYT|Fizik|Basit Makineler|Makara düzeneklerini çözer;Kaldıraçta kuvvet kazancı bulur;Dişli çarkları yorumlar
AYT|Fizik|Çembersel Hareket|Merkezcil ivme hesaplar;Merkezcil kuvvet bulur;Düşey çembersel hareketi çözer
AYT|Fizik|Dönme Hareketi|Açısal hız ve ivme hesaplar;Atalet momentini yorumlar;Açısal momentum korunumu uygular
AYT|Fizik|Kütle Çekim|Evrensel çekim hesaplar;Yörünge hızını bulur;Çekim potansiyelini yorumlar
AYT|Fizik|Basit Harmonik Hareket|Yay ve sarkaç periyodu hesaplar;BHH grafikleri yorumlar;Enerji dönüşümünü açıklar
AYT|Fizik|Elektrik Alan|Coulomb kuvveti hesaplar;Elektrik alan bileşkesi bulur;Elektrik alandaki hareketi yorumlar
AYT|Fizik|Elektrik Potansiyel|Elektrik potansiyeli hesaplar;Eşpotansiyel yüzeyleri yorumlar;Potansiyel enerji bağıntısını kullanır
AYT|Fizik|Kondansatörler|Sığa eşdeğerini hesaplar;Kondansatörde enerji bulur;Seri paralel sığa çözer
AYT|Fizik|Manyetik Alan|Manyetik kuvvetin yönünü bulur;Manyetik alanda hareketi çözer;Akımın manyetik alanını hesaplar
AYT|Fizik|Elektromanyetik İndüksiyon|Faraday ve Lenz yasası uygular;Manyetik akıyı yorumlar;İndüksiyon elektromotor kuvveti hesaplar
AYT|Fizik|Alternatif Akım|Etkin akım gerilim hesaplar;AC DC farkını açıklar;Transformatör ve güç aktarımı çözer
AYT|Fizik|Dalgalar|Girişim kırınımı açıklar;Çift yarık saçak hesabı yapar;Doppler olayını çözer
AYT|Fizik|Elektromanyetik Dalgalar|Spektrumu sıralar;Dalga boyu frekans enerji ilişkilendirir;EM dalgaların kullanımını yorumlar
AYT|Fizik|Modern Fizik|Fotoelektrik etkiyi çözer;De Broglie dalga boyu hesaplar;Göreliliğin temel sonuçlarını yorumlar
AYT|Fizik|Atom ve Çekirdek|Bohr atomunu uygular;Radyoaktif yarı ömür hesaplar;Fisyon füzyon enerjisini yorumlar
AYT|Kimya|Modern Atom Teorisi|Kuantum sayıları belirler;Elektron dizilimi oluşturur;Orbital ve periyodik özellikleri yorumlar
AYT|Kimya|Gaz Yasaları|Boyle Charles ve Gay Lussac uygular;İdeal gaz hesabı yapar;Kısmi basıncı bulur
AYT|Kimya|Çözeltiler|Molarite hesaplar;Seyreltme ve karışım çözer;Çözünürlüğü yorumlar
AYT|Kimya|Koligatif Özellikler|Kaynama noktası yükselmesini hesaplar;Donma noktası alçalmasını yorumlar;Osmoz ve basıncı açıklar
AYT|Kimya|Tepkime Enerjisi|Tepkime entalpisini hesaplar;Hess yasası uygular;Bağ enerjisini yorumlar
AYT|Kimya|Tepkime Hızı|Hız bağıntısı kurar;Aktivasyon enerjisini yorumlar;Katalizör etkisini açıklar
AYT|Kimya|Kimyasal Denge|Denge sabitini hesaplar;Le Chatelier prensibini uygular;Kc Kp farkını ayırt eder
AYT|Kimya|Asit Baz Dengesi|pH pOH hesaplar;Kuvvetli ve zayıf asitleri karşılaştırır;Tampon çözeltiyi yorumlar
AYT|Kimya|Çözünürlük Dengesi|Kçç hesaplar;Ortak iyon etkisini yorumlar;Çökelme koşullarını çözer
AYT|Kimya|Elektrokimya|Anot katot belirler;Pil potansiyeli hesaplar;İndirgenme yükseltgenme ayırt eder
AYT|Kimya|Elektroliz|Faraday hesabı yapar;Elektroliz ürünlerini yorumlar;Kaplama problemleri çözer
AYT|Kimya|Organik Kimya|Karbon bağlarını açıklar;İzomerleri karşılaştırır;Hibritleşmeyi yorumlar
AYT|Kimya|Alkanlar ve Sikloalkanlar|Alkanları adlandırır;Tepkimelerini yorumlar;Yapı izomerliğini çözer
AYT|Kimya|Alken ve Alkinler|Adlandırma yapar;Katılma tepkimesi çözer;Geometrik izomerliği yorumlar
AYT|Kimya|Aromatik Bileşikler|Benzen ve türevlerini tanır;Aromatik özellikleri yorumlar;Adlandırma yapar
AYT|Kimya|Organik Fonksiyonel Gruplar|Alkol eter aldehit ketonu ayırt eder;Karboksilik asit ve esterleri tanır;Organik tepkimeleri yorumlar
AYT|Biyoloji|Sinir Sistemi|Nöron ve sinapsı açıklar;İmpuls iletimini yorumlar;Merkezi çevresel sinir sistemini ayırt eder
AYT|Biyoloji|Endokrin Sistem|Hipofiz hormonlarını açıklar;Tiroit pankreas hormonlarını karşılaştırır;Geri bildirim mekanizmasını yorumlar
AYT|Biyoloji|Duyu Organları|Göz yapısını yorumlar;Kulak ve dengeyi açıklar;Duyu reseptörlerini ayırt eder
AYT|Biyoloji|Destek Hareket|Kemik dokusunu ayırt eder;Kas kasılmasını açıklar;Eklem türlerini yorumlar
AYT|Biyoloji|Sindirim Sistemi|Sindirim enzimlerini eşleştirir;Emilimi yorumlar;Sindirim organlarını açıklar
AYT|Biyoloji|Dolaşım Sistemi|Kalbin çalışmasını yorumlar;Kan bileşenlerini açıklar;Büyük küçük dolaşımı karşılaştırır
AYT|Biyoloji|Solunum Sistemi|Gazların taşınmasını yorumlar;Soluk alıp vermeyi açıklar;Solunum organlarını ayırt eder
AYT|Biyoloji|Boşaltım Sistemi|Nefronu yorumlar;Süzülme geri emilimi çözer;Su tuz dengesini açıklar
AYT|Biyoloji|Üreme Sistemi|Gamet oluşumunu yorumlar;Döllenme ve gelişmeyi açıklar;Üreme organlarını ayırt eder
AYT|Biyoloji|Komünite Ekolojisi|Popülasyon büyümesini yorumlar;Av avcı ilişkisini açıklar;Süksesyonu yorumlar
AYT|Biyoloji|Nükleik Asitler|DNA RNA farkını açıklar;DNA replikasyonunu yorumlar;Gen ve nükleotidi ayırt eder
AYT|Biyoloji|Protein Sentezi|Transkripsiyonu açıklar;Translasyonu yorumlar;Kod antikodon ilişkisini çözer
AYT|Biyoloji|Genetik Mühendisliği|PCR ve DNA analizini açıklar;Klonlama ve gen aktarımını yorumlar;Biyoteknolojiyi sınıflandırır
AYT|Biyoloji|Enerji Dönüşümleri|ATP kullanımını açıklar;Fotosentez kemosentezi karşılaştırır;Enerji metabolizmasını yorumlar
AYT|Biyoloji|Fotosentez|Işığa bağımlı evreyi çözer;Calvin döngüsünü yorumlar;Fotosentez hızını hesaplar
AYT|Biyoloji|Hücresel Solunum|Glikoliz ve Krebs döngüsü açıklar;Elektron taşıma sistemi yorumlar;Fermantasyonu ayırt eder
AYT|Biyoloji|Bitkisel Dokular|Meristem ve kalıcı doku ayırt eder;İletim doku görevlerini açıklar;Bitki organlarını yorumlar
AYT|Biyoloji|Bitkilerde Taşıma|Ksilem floemi karşılaştırır;Terlemeyi açıklar;Madde taşıma süreçlerini yorumlar
AYT|Biyoloji|Bitkilerde Üreme|Çift döllenmeyi açıklar;Tohum çimlenmesini yorumlar;Tozlaşmayı ayırt eder
AYT|Biyoloji|Bitki Hormonları|Oksin giberellin etkilerini açıklar;Etilen absisik asidi karşılaştırır;Fototropizmayı yorumlar
AYT|Biyoloji|Evrim ve Adaptasyon|Adaptasyon ve doğal seçilimi açıklar;Biyoçeşitliliği yorumlar;Popülasyon dinamiklerini karşılaştırır
AYT|Edebiyat|Şiir Bilgisi|Ölçü uyak ve redifi ayırt eder;Nazım biçimlerini belirler;Şiirde imgeyi yorumlar
AYT|Edebiyat|Söz Sanatları|Benzetme ve istiareyi ayırt eder;Teşhis ve intakı belirler;Tezat ve kinayeyi yorumlar
AYT|Edebiyat|İslamiyet Öncesi Edebiyat|Koşuk sagu savı ayırt eder;Destanları yorumlar;Göktürk ve Uygur metinlerini tanır
AYT|Edebiyat|Geçiş Dönemi|Kutadgu Bilig ve Divanü Lügati't-Türkü ayırt eder;Atabetü'l Hakayık'ı tanır;Divan-ı Hikmeti yorumlar
AYT|Edebiyat|Halk Edebiyatı|Anonim ve âşık edebiyatını ayırt eder;Tekke edebiyatını yorumlar;Koşma semai ve varsağıyı tanır
AYT|Edebiyat|Divan Edebiyatı|Gazel kaside ve mesneviyi ayırt eder;Mazmun ve aruzu yorumlar;Sanatçı eser eşleştirmesi yapar
AYT|Edebiyat|Tanzimat|İki dönemi karşılaştırır;Edebî ilkleri tanır;Sanatçı eserlerini ayırt eder
AYT|Edebiyat|Servetifünun Fecriati|Dönem özelliklerini açıklar;Şiir ve romanı yorumlar;Sanatçıları eserleriyle eşleştirir
AYT|Edebiyat|Millî Edebiyat|Yeni Lisan'ı açıklar;Şiir ve roman özelliklerini yorumlar;Yazar eser eşleştirmesi yapar
AYT|Edebiyat|Cumhuriyet Şiiri|Garip ve İkinci Yeniyi karşılaştırır;Saf şiiri tanır;Toplumcu şiiri yorumlar
AYT|Edebiyat|Cumhuriyet Romanı|Dönem roman eğilimlerini ayırt eder;Toplumcu romanları yorumlar;Yazar eser eşleştirmesi yapar
AYT|Edebiyat|Tiyatro ve Öğretici Metinler|Tiyatro türlerini ayırt eder;Deneme makale eleştiriyi tanır;Anı ve biyografiyi yorumlar
AYT|Edebiyat|Edebî Akımlar|Klasisizm romantizmi ayırt eder;Realizm natüralizmi karşılaştırır;Sembolizm ve sürrealizmi yorumlar
AYT|Tarih-1|Osmanlı Siyasi Tarihi|Kuruluşu açıklar;Yükseliş dönemi fetihlerini yorumlar;Osmanlı antlaşmalarını ayırt eder
AYT|Tarih-1|Osmanlı Kültür Uygarlık|Toprak sistemi açıklar;Merkez teşkilatını ayırt eder;İlmiye kalemiye seyfiyeyi yorumlar
AYT|Tarih-1|Millî Mücadele|Kongreleri karşılaştırır;TBMM çalışmalarını yorumlar;Cepheleri ayırt eder
AYT|Tarih-1|Atatürk İnkılapları|Hukuk ve eğitim reformlarını açıklar;Altı ilkeyi ayırt eder;Dış politikayı yorumlar
AYT|Tarih-1|Çağdaş Türk Dünya Tarihi|İki savaş arasını açıklar;İkinci Dünya Savaşını yorumlar;Soğuk Savaşı tanır
AYT|Coğrafya-1|Doğal Sistemler|Türkiye iklimini yorumlar;Yer şekillerini karşılaştırır;Su ve toprağı ayırt eder
AYT|Coğrafya-1|Ekonomik Faaliyetler|Tarımı yorumlar;Sanayiyi sınıflandırır;Ulaşım ve ticareti açıklar
AYT|Coğrafya-1|Nüfus Yerleşme|Nüfus politikalarını yorumlar;Göçü açıklar;Şehirleşmeyi karşılaştırır
AYT|Tarih-2|Avrupa Tarihi|Coğrafi keşifleri yorumlar;Reform ve Rönesans'ı karşılaştırır;Sanayi Devrimini açıklar
AYT|Tarih-2|Dünya Savaşları|Savaş nedenlerini açıklar;Cepheleri ayırt eder;Savaş sonrası değişimleri yorumlar
AYT|Tarih-2|Soğuk Savaş|Kore ve Küba krizini açıklar;NATO ve Varşova'yı ayırt eder;Bloklaşmayı yorumlar
AYT|Coğrafya-2|Küresel Sistem|Küresel ticareti yorumlar;Ekonomik bölgeleri karşılaştırır;Kaynak dağılımını açıklar
AYT|Coğrafya-2|Ekoloji|Ekosistemi açıklar;Biyoçeşitliliği yorumlar;Sürdürülebilirliği açıklar
AYT|Felsefe Grubu|Psikoloji|Öğrenme ve belleği açıklar;Gelişim kuramlarını yorumlar;Araştırma yöntemlerini ayırt eder
AYT|Felsefe Grubu|Sosyoloji|Toplumsal kurumları açıklar;Tabakalaşmayı yorumlar;Kültür ve sosyalleşmeyi ayırt eder
AYT|Felsefe Grubu|Mantık|Kavram ve önermeyi açıklar;Kıyas kurallarını yorumlar;Çıkarım yapar
AYT|Din Kültürü / Ek Felsefe|İnanç ve Değer|İnanç esaslarını ayırt eder;Din ahlak ilişkisini açıklar;Evrensel değerleri yorumlar
    """.trimIndent()

    /** Missing exam units as single, user-checkable topics (not artificial subskills). */
    private val supplementalUnits = """
TYT|Türkçe|Ek Fiil
TYT|Türkçe|Fiilde Çatı
TYT|Matematik|Temel Kavramlar
TYT|Tarih|Osmanlı Kültür ve Medeniyeti
TYT|Tarih|Avrupa ve Osmanlı'da Değişim
TYT|Coğrafya|Çevre ve Toplum
TYT|Coğrafya|Ekosistem ve Biyoçeşitlilik
TYT|Din Kültürü|Din ve Hayat
TYT|Din Kültürü|Ahlaki Tutum ve Davranışlar
TYT|Din Kültürü|İslam Düşüncesinde Yorumlar
TYT|Din Kültürü|Din, Kültür ve Medeniyet
TYT|Kimya|Doğa ve Kimya
TYT|Kimya|Kimyanın Temel Kanunları
AYT|Matematik|Fonksiyonlarda Uygulamalar
AYT|Geometri|Üçgenler
AYT|Geometri|Dörtgenler ve Çokgenler
AYT|Geometri|Çember ve Daire
AYT|Fizik|Dalga Mekaniği
AYT|Fizik|Atom Fiziğine Giriş ve Radyoaktivite
AYT|Fizik|Modern Fiziğin Teknolojideki Uygulamaları
AYT|Kimya|Karbon Kimyasına Giriş
AYT|Kimya|Enerji Kaynakları ve Bilimsel Gelişmeler
AYT|Biyoloji|Canlılar ve Çevre
AYT|Edebiyat|Hikâye ve Roman
AYT|Edebiyat|Destan ve Efsane
AYT|Tarih-1|Tarih ve Zaman
AYT|Tarih-1|İlk ve Orta Çağ Uygarlıkları
AYT|Tarih-1|İlk Türk Devletleri
AYT|Tarih-1|Türk-İslam Devletleri
AYT|Tarih-1|Selçuklu Türkiyesi
AYT|Coğrafya-1|Küresel Ortam: Bölgeler ve Ülkeler
AYT|Coğrafya-1|Çevre ve Toplum
AYT|Tarih-2|20. Yüzyıl Başlarında Osmanlı ve Dünya
AYT|Tarih-2|Millî Mücadele
AYT|Tarih-2|Atatürkçülük ve Türk İnkılabı
AYT|Tarih-2|İki Savaş Arası Dönemde Türkiye ve Dünya
AYT|Tarih-2|İkinci Dünya Savaşı Sonrası Türkiye ve Dünya
AYT|Tarih-2|21. Yüzyılın Eşiğinde Türkiye ve Dünya
AYT|Coğrafya-2|Doğal Sistemler
AYT|Coğrafya-2|Beşerî Sistemler
AYT|Coğrafya-2|Çevre ve Toplum
AYT|Din Kültürü / Ek Felsefe|Bilgi ve İnanç
AYT|Din Kültürü / Ek Felsefe|İslam ve İbadet
AYT|Din Kültürü / Ek Felsefe|Hz. Muhammed ve Kur'an
AYT|Din Kültürü / Ek Felsefe|Din ve Ahlak
AYT|Din Kültürü / Ek Felsefe|İslam Düşüncesinde Yorumlar
AYT|Din Kültürü / Ek Felsefe|İslam ve Bilim
AYT|Din Kültürü / Ek Felsefe|Güncel Dinî Meseleler
AYT|Din Kültürü / Ek Felsefe|Dünya Dinleri
    """.trimIndent()

    fun normalizedTopic(exam: String, subject: String, topic: String): String {
        if (exam == "TYT" && subject == "Türkçe" &&
            topic in setOf("Paragrafta Anlam", "Paragrafta Yapı", "Anlatım Biçimleri", "Düşünceyi Geliştirme")) return "Paragraf"
        if (exam == "TYT" && subject == "Matematik" &&
            topic in setOf("Sayı Problemleri", "Kesir Problemleri", "Yaş Problemleri", "İşçi Havuz Problemleri", "Hareket Problemleri", "Yüzde Problemleri", "Kâr Zarar Problemleri", "Karışım Problemleri", "Grafik Tablo Problemleri")) return "Problemler"
        if (exam == "TYT" && subject == "Geometri" &&
            topic in setOf("Üçgende Açılar", "Üçgende Kenar", "Açıortay", "Kenarortay", "Benzerlik", "Üçgende Alan")) return "Üçgenler"
        if (exam == "TYT" && subject == "Geometri" &&
            topic in setOf("Çokgenler", "Dörtgenler", "Paralelkenar", "Dikdörtgen ve Kare", "Yamuk", "Eşkenar Dörtgen ve Deltoid")) return "Dörtgenler ve Çokgenler"
        if (exam == "TYT" && subject == "Geometri" &&
            topic in setOf("Çemberde Açılar", "Çemberde Uzunluk", "Daire")) return "Çember ve Daire"
        if (exam == "TYT" && subject == "Tarih" &&
            topic in setOf("Osmanlı Kuruluş", "Osmanlı Yükseliş", "Osmanlı Islahatları", "19. Yüzyıl Osmanlı")) return "Osmanlı Devleti"
        if (exam == "TYT" && subject == "Tarih" &&
            topic in setOf("Trablusgarp ve Balkan Savaşları", "I. Dünya Savaşı")) return "20. Yüzyıl Başlarında Osmanlı"
        if (exam == "TYT" && subject == "Tarih" &&
            topic in setOf("Millî Mücadele Hazırlık", "Kurtuluş Savaşı")) return "Millî Mücadele"
        if (exam == "TYT" && subject == "Coğrafya" &&
            topic in setOf("Atmosfer ve Sıcaklık", "Basınç ve Rüzgâr", "Nem ve Yağış", "İklim Tipleri")) return "İklim Bilgisi"
        if (exam == "TYT" && subject == "Coğrafya" &&
            topic in setOf("İç Kuvvetler", "Dış Kuvvetler")) return "Yer Şekilleri"
        if (exam == "TYT" && subject == "Fizik" &&
            topic in setOf("Hareket", "Kuvvet")) return "Hareket ve Kuvvet"
        if (exam == "TYT" && subject == "Fizik" &&
            topic in setOf("Elektrostatik", "Elektrik Devreleri")) return "Elektrik"
        if (exam == "TYT" && subject == "Kimya" &&
            topic in setOf("Atom Modelleri", "Periyodik Sistem")) return "Atom ve Periyodik Sistem"
        if (exam == "TYT" && subject == "Kimya" &&
            topic in setOf("Maddenin Halleri", "Gazlar")) return "Maddenin Halleri"
        if (exam == "TYT" && subject == "Biyoloji" &&
            topic in setOf("İnorganik Bileşikler", "Organik Bileşikler", "Enzimler")) return "Canlıların Temel Bileşenleri"
        if (exam == "TYT" && subject == "Biyoloji" &&
            topic in setOf("Hücre", "Madde Geçişleri")) return "Hücre"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Trigonometri Temelleri", "Trigonometrik Denklemler", "Trigonometrik Grafikler")) return "Trigonometri"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("İkinci Dereceden Denklemler", "Parabol")) return "İkinci Dereceden Denklemler ve Parabol"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Logaritma", "Üstel Fonksiyon")) return "Üstel ve Logaritmik Fonksiyonlar"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Diziler", "Seriler")) return "Diziler"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Permütasyon Kombinasyon", "Binom")) return "Sayma ve Binom"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Limit", "Süreklilik")) return "Limit ve Süreklilik"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("Türev", "Türev Uygulamaları")) return "Türev"
        if (exam == "AYT" && subject == "Matematik" &&
            topic in setOf("İntegral", "Alan İntegrali")) return "İntegral"
        if (exam == "AYT" && subject == "Fizik" &&
            topic in setOf("Vektörler", "Bağıl Hareket", "Newton Yasaları", "Sabit İvmeli Hareket", "Atışlar", "Enerji", "İtme Momentum", "Tork Denge", "Basit Makineler")) return "Kuvvet ve Hareket"
        if (exam == "AYT" && subject == "Fizik" &&
            topic in setOf("Çembersel Hareket", "Dönme Hareketi", "Kütle Çekim")) return "Çembersel Hareket"
        // MEB 11th-grade Physics names the complete unit "Elektrik ve Manyetizma".
        if (exam == "AYT" && subject == "Fizik" &&
            topic in setOf("Elektrik Alan", "Elektrik Potansiyel", "Kondansatörler",
                "Manyetik Alan", "Elektromanyetik İndüksiyon", "Alternatif Akım")) {
            return "Elektrik ve Manyetizma"
        }
        // MEB 12th-grade: diffraction, interference and EM waves share one unit.
        if (exam == "AYT" && subject == "Fizik" &&
            topic in setOf("Dalgalar", "Elektromanyetik Dalgalar")) return "Dalga Mekaniği"
        if (exam == "AYT" && subject == "Fizik" && topic == "Atom ve Çekirdek") {
            return "Atom Fiziğine Giriş ve Radyoaktivite"
        }
        if (exam == "AYT" && subject == "Fizik" && topic == "Modern Fizik") {
            return "Modern Fizik"
        }
        if (exam == "AYT" && subject == "Kimya" && topic == "Gaz Yasaları") return "Gazlar"
        if (exam == "AYT" && subject == "Kimya" &&
            topic in setOf("Çözeltiler", "Koligatif Özellikler")) return "Sıvı Çözeltiler ve Çözünürlük"
        if (exam == "AYT" && subject == "Kimya" && topic == "Tepkime Enerjisi") {
            return "Kimyasal Tepkimelerde Enerji"
        }
        if (exam == "AYT" && subject == "Kimya" && topic == "Tepkime Hızı") {
            return "Kimyasal Tepkimelerde Hız"
        }
        if (exam == "AYT" && subject == "Kimya" &&
            topic in setOf("Kimyasal Denge", "Asit Baz Dengesi", "Çözünürlük Dengesi")) {
            return "Kimyasal Tepkimelerde Denge"
        }
        if (exam == "AYT" && subject == "Kimya" &&
            topic in setOf("Elektrokimya", "Elektroliz")) return "Kimya ve Elektrik"
        if (exam == "AYT" && subject == "Kimya" &&
            topic in setOf("Organik Kimya", "Alkanlar ve Sikloalkanlar", "Alken ve Alkinler", "Aromatik Bileşikler", "Organik Fonksiyonel Gruplar")) return "Organik Bileşikler"
        if (exam == "AYT" && subject == "Biyoloji" && topic == "Komünite Ekolojisi") {
            return "Komünite ve Popülasyon Ekolojisi"
        }
        if (exam == "AYT" && subject == "Biyoloji" &&
            topic in setOf("Sinir Sistemi", "Endokrin Sistem", "Duyu Organları", "Destek Hareket", "Sindirim Sistemi", "Dolaşım Sistemi", "Solunum Sistemi", "Boşaltım Sistemi", "Üreme Sistemi")) return "İnsan Fizyolojisi"
        if (exam == "AYT" && subject == "Biyoloji" &&
            topic in setOf("Nükleik Asitler", "Protein Sentezi", "Genetik Mühendisliği")) return "Genden Proteine"
        if (exam == "AYT" && subject == "Biyoloji" &&
            topic in setOf("Enerji Dönüşümleri", "Fotosentez", "Hücresel Solunum")) {
            return "Canlılarda Enerji Dönüşümleri"
        }
        if (exam == "AYT" && subject == "Biyoloji" && topic == "Evrim ve Adaptasyon") {
            return "Canlılar ve Çevre"
        }
        if (exam == "AYT" && subject == "Biyoloji" &&
            topic in setOf("Bitkisel Dokular", "Bitkilerde Taşıma", "Bitkilerde Üreme", "Bitki Hormonları")) return "Bitki Biyolojisi"
        if (exam == "AYT" && subject == "Edebiyat" &&
            topic in setOf("Cumhuriyet Şiiri", "Cumhuriyet Romanı", "Tiyatro ve Öğretici Metinler")) return "Cumhuriyet Dönemi Edebiyatı"
        return topic
    }

    /** One checklist row per real topic, rather than three artificial skill rows. */
    val starter: List<LearningOutcome> by lazy {
        val topics = linkedMapOf<String, LearningOutcome>()
        catalog.lineSequence().filter { it.isNotBlank() }.forEach { row ->
            val parts = row.trim().split('|', limit = 4)
            require(parts.size == 4) { "Invalid learning catalog entry" }
            val exam = parts[0].trim()
            val subject = parts[1].trim()
            val title = normalizedTopic(exam, subject, parts[2].trim())
            val id = "unit|$exam|$subject|$title"
            topics.putIfAbsent(id, LearningOutcome(
                id = id, examType = exam, subject = subject, topic = subject, title = title
            ))
        }
        // Add only truly missing unit rows. Keep all existing unit IDs stable
        // so ticks, scheduled reviews and linked question images are untouched.
        supplementalUnits.lineSequence().filter { it.isNotBlank() }.forEach { row ->
            val parts = row.split('|', limit = 3)
            require(parts.size == 3) { "Invalid supplemental unit" }
            val exam = parts[0].trim()
            val subject = parts[1].trim()
            val title = parts[2].trim()
            val id = "unit|$exam|$subject|$title"
            topics.putIfAbsent(id, LearningOutcome(
                id = id, examType = exam, subject = subject, topic = subject, title = title
            ))
        }
        topics.values.toList()
    }

    /** Previous three-skills-per-topic catalog, used ONLY for migration. */
    val legacyStarter: List<LearningOutcome> by lazy {
        catalog.lineSequence().filter { it.isNotBlank() }.flatMap { row ->
            val parts = row.trim().split('|', limit = 4)
            require(parts.size == 4) { "Invalid learning catalog entry" }
            val exam = parts[0].trim()
            val subject = parts[1].trim()
            val topic = parts[2].trim()
            val skills = parts[3].split(';').map { it.trim() }.filter { it.isNotBlank() }
            skills.asSequence().mapIndexed { index, title ->
                LearningOutcome(
                    id = "expanded|$exam|$subject|$topic|$index",
                    examType = exam,
                    subject = subject,
                    topic = topic,
                    title = title
                )
            }
        }.toList()
    }
}
