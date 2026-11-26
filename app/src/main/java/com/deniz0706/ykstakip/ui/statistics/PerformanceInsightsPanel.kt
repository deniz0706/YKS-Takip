package com.deniz0706.ykstakip.ui.statistics
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.deniz0706.ykstakip.model.*
import com.deniz0706.ykstakip.ui.components.DashboardMetric
import com.deniz0706.ykstakip.ui.components.DashboardPanel
import com.deniz0706.ykstakip.util.PerformanceIntelligence
import java.util.Locale

@Composable
fun PerformanceInsightsPanel(exams:List<Exam>,type:ExamType,field:AytField,targetNet:Float?){
 val report=remember(exams,type,field){PerformanceIntelligence.analyze(exams,type,if(type==ExamType.AYT)field else null)}
 val colors=MaterialTheme.colorScheme
 DashboardPanel(Modifier.fillMaxWidth(),accent=true){
  Text("GELİŞİM RADARI",color=colors.secondary,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
  Spacer(Modifier.height(5.dp));Text("Son dönem ve branş eğilimleri",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,color=colors.onSurface)
  Spacer(Modifier.height(10.dp))
  if(report.examCount==0)Text("Bu alanda henüz deneme kaydı yok. İlk kayıt eklendiğinde eğilimler burada görünecek.",color=colors.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
  else{
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
    DashboardMetric("SON 5 ORT.",report.recentAverage?.let{"%.1f".format(Locale("tr","TR"),it)}?:"—",Modifier.weight(1f),"${report.examCount} deneme",colors.primary)
    DashboardMetric("ÖNCEKİ 5",report.previousAverage?.let{"%.1f".format(Locale("tr","TR"),it)}?:"—",Modifier.weight(1f),"Karşılaştırma",colors.secondary)
    DashboardMetric("DEĞİŞİM",report.change?.let{"%+.1f".format(Locale("tr","TR"),it)}?:"—",Modifier.weight(1f),"Net",if((report.change?:0.0)>=0)colors.primary else colors.error)
   }
   if(targetNet!=null&&report.recentAverage!=null){
    val gap=targetNet.toDouble()-report.recentAverage
    Spacer(Modifier.height(8.dp))
    Text(if(gap>0.05) "Hedefe kalan: ${"%.1f".format(Locale("tr","TR"),gap)} net" else "Hedef ${"%.1f".format(Locale("tr","TR"),-gap)} net aşıldı",
     color=if(gap>0.05)colors.onSurfaceVariant else colors.primary,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.SemiBold)
   }
   if(report.insufficientData){Spacer(Modifier.height(8.dp));Text("Eğilim için en az 3 deneme önerilir; mevcut veri sınırlı.",color=colors.onSurfaceVariant,style=MaterialTheme.typography.labelSmall)}
   Spacer(Modifier.height(12.dp));Text("BRANŞ ÖNCELİKLERİ",color=colors.secondary,style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold)
   report.subjects.take(4).forEach{s->
    Spacer(Modifier.height(7.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){
     Text(s.subject,color=colors.onSurface,fontWeight=FontWeight.Medium)
     Text("${"%.1f".format(Locale("tr","TR"),s.recentAverage)} net",color=colors.primary,fontWeight=FontWeight.SemiBold)
    }
    val trend=s.change?.let{" · ${"%+.1f".format(Locale("tr","TR"),it)} önceki döneme göre"}?:" · karşılaştırma verisi oluşmadı"
    val time=s.averageMinutesPerQuestion?.let{" · ${"%.2f".format(Locale("tr","TR"),it)} dk/soru"}.orEmpty()
    Text("Doğru oranı ${(s.averageAccuracy*100).toInt()}%$trend$time",color=colors.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
    if(s.weakTopics.isNotEmpty())Text("Tekrar etiketleri: ${s.weakTopics.joinToString()}",color=colors.onSurfaceVariant,style=MaterialTheme.typography.bodySmall)
   }
   Spacer(Modifier.height(7.dp));Text("Net ve konu etiketleri kullanılır; tek başına dikkat/bilgi hatası teşhisi koymaz.",color=colors.onSurfaceVariant,style=MaterialTheme.typography.labelSmall)
  }
 }
}
