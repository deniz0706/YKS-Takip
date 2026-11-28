package com.deniz0706.ykstakip.util
import com.deniz0706.ykstakip.domain.model.*
import org.junit.Assert.*
import org.junit.Test
class PerformanceIntelligenceTest {
 private fun exam(id:Long,date:String,net:Int,field:AytField?=null)=Exam(id,"Deneme $id",date,if(field==null)ExamType.TYT else ExamType.AYT,field,listOf(SubjectResult("Matematik", net, 0, 0, 60, listOf("Problemler")) ))
 @Test fun comparesWindowsAndDoesNotMixExamTypes(){
  val xs=listOf(exam(1,"2026-01-01",20),exam(2,"2026-01-02",22),exam(3,"2026-01-03",24),exam(4,"2026-01-04",30),exam(5,"2026-01-05",32),exam(6,"2026-01-06",34),exam(7,"2026-01-07",39,AytField.SAYISAL))
  val r=PerformanceIntelligence.analyze(xs,ExamType.TYT,window=2)
  assertEquals(6,r.examCount);assertEquals(33.0,r.recentAverage!!,0.001);assertEquals(27.0,r.previousAverage!!,0.001);assertEquals(6.0,r.change!!,0.001)
 }
 @Test fun reportsWeakTopicsAndSubjectTrends(){
  val r=PerformanceIntelligence.analyze(listOf(exam(1,"2026-01-01",20),exam(2,"2026-01-02",21),exam(3,"2026-01-03",22)),ExamType.TYT)
  val m=r.subjects.first{it.subject=="Matematik"};assertTrue(m.weakTopics.contains("Problemler"));assertEquals(3,m.sampleCount);assertFalse(r.insufficientData)
 }
 @Test fun insufficientDataDoesNotInventComparison(){
  val r=PerformanceIntelligence.analyze(listOf(exam(1,"2026-01-01",20)),ExamType.TYT)
  assertTrue(r.insufficientData);assertNull(r.change);assertNull(r.previousAverage)
 }
 @Test fun respectsAytField(){
  val r=PerformanceIntelligence.analyze(listOf(exam(1,"2026-01-01",20,AytField.SAYISAL),exam(2,"2026-01-02",25,AytField.ESIT_AGIRLIK),exam(3,"2026-01-03",30,AytField.SAYISAL)),ExamType.AYT,AytField.SAYISAL)
  assertEquals(2,r.examCount);assertEquals(25.0,r.recentAverage!!,0.001)
 }
}
