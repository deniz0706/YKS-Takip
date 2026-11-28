package com.deniz0706.ykstakip.util
import com.deniz0706.ykstakip.domain.model.AytField
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import kotlin.math.sqrt

/** Deterministic, evidence-bounded analysis of recorded exam results. */
object PerformanceIntelligence {
 data class SubjectTrend(val subject:String,val recentAverage:Double,val previousAverage:Double?,val change:Double?,val latestNet:Double,val averageAccuracy:Double,val averageMinutesPerQuestion:Double?,val volatility:Double,val weakTopics:List<String>,val sampleCount:Int)
 data class Report(val examCount:Int,val recentAverage:Double?,val previousAverage:Double?,val change:Double?,val consistency:Double?,val subjects:List<SubjectTrend>,val insufficientData:Boolean)
 fun analyze(exams:List<Exam>,type:ExamType,aytField:AytField?=null,window:Int=5):Report {
  require(window>=1)
  val filtered=exams.filter{it.type==type&&(type!=ExamType.AYT||aytField==null||it.aytField==aytField)}.sortedBy{it.date}
  val recent=filtered.takeLast(window); val previous=filtered.dropLast(window).takeLast(window)
  val recentAverage=recent.takeIf{it.isNotEmpty()}?.map{it.totalNet}?.average()
  val previousAverage=previous.takeIf{it.isNotEmpty()}?.map{it.totalNet}?.average()
  val nets=recent.map{it.totalNet}
  val consistency=if(nets.size>=2){val mean=nets.average();sqrt(nets.map{(it-mean)*(it-mean)}.average())}else null
  val names=recent.flatMap{it.subjects}.map{it.subject}.distinct()
  val trends=names.mapNotNull{name->
   val rows=recent.mapNotNull{exam->exam.subject(name)?.let{exam.date to it}}
   if(rows.isEmpty()) return@mapNotNull null
   val old=previous.mapNotNull{it.subject(name)}; val values=rows.map{it.second.net}; val avg=values.average()
   val accuracy=rows.mapNotNull{(_,s)->if(s.questionCount<=0)null else (s.correct.toDouble()/s.questionCount).coerceIn(0.0,1.0)}
   val times=rows.mapNotNull{it.second.minutesPerQuestion?.takeIf{v->v>0.0}}
   val topics=rows.flatMap{it.second.weakTopics}.map{it.trim()}.filter{it.isNotEmpty()}.groupingBy{it}.eachCount()
    .entries.sortedWith(compareByDescending<Map.Entry<String,Int>>{it.value}.thenBy{it.key}).take(3).map{it.key}
   val sd=if(values.size>=2)sqrt(values.map{(it-avg)*(it-avg)}.average())else 0.0
   val oldAvg=old.takeIf{it.isNotEmpty()}?.map{it.net}?.average()
   SubjectTrend(name,avg,oldAvg,oldAvg?.let{avg-it},rows.last().second.net,accuracy.takeIf{it.isNotEmpty()}?.average()?:0.0,times.takeIf{it.isNotEmpty()}?.average(),sd,topics,rows.size)
  }.sortedWith(compareBy<SubjectTrend>{it.averageAccuracy}.thenBy{it.recentAverage})
  return Report(filtered.size,recentAverage,previousAverage,if(recentAverage!=null&&previousAverage!=null)recentAverage-previousAverage else null,consistency,trends,filtered.size<3)
 }
 fun toCoachSummary(report:Report,label:String):String {
  if(report.examCount==0)return "$label: Henüz analiz edilecek deneme kaydı yok."
  val lines=mutableListOf("$label performans analizi: ${report.examCount} deneme.")
  report.recentAverage?.let{lines+="Son dönem ortalaması: ${"%.2f".format(java.util.Locale("tr","TR"),it)} net."}
  if(report.change!=null){val dir=when{report.change>0.05->"artış";report.change< -0.05->"düşüş";else->"yatay"};lines+="Önceki dönemle fark: ${"%+.2f".format(java.util.Locale("tr","TR"),report.change)} net ($dir)."}else lines+="Dönem karşılaştırması için yeterli geçmiş henüz yok."
  report.consistency?.let{lines+="Son dönem net standart sapması: ${"%.2f".format(java.util.Locale("tr","TR"),it)} (düşük değer daha istikrarlı)."}
  report.subjects.take(5).forEach{s->
   val change=s.change?.let{" / dönem farkı ${"%+.2f".format(java.util.Locale("tr","TR"),it)}"}.orEmpty()
   val topics=s.weakTopics.takeIf{it.isNotEmpty()}?.joinToString()?: "etiketlenmiş konu yok"
   lines+="${s.subject}: son ort. ${"%.2f".format(java.util.Locale("tr","TR"),s.recentAverage)} net$change; doğru oranı ${(s.averageAccuracy*100).toInt()}%; değişkenlik ${"%.2f".format(java.util.Locale("tr","TR"),s.volatility)}; tekrar etiketleri: $topics."
  }
  if(report.insufficientData)lines+="Kayıt sayısı az; bu sonuçları kesin eğilim olarak sunma."
  lines+="Net ve süre verisinden dikkat hatası/bilgi eksiği teşhisi koyma; bunun için soru veya hata türü kaydı gerekir."
  return lines.joinToString("\n")
 }
}
