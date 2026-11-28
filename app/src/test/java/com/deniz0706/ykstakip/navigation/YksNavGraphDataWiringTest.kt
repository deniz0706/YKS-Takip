package com.deniz0706.ykstakip.navigation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.deniz0706.ykstakip.data.repository.SqlExamRepository
import com.deniz0706.ykstakip.data.repository.SqlLearningRepository
import com.deniz0706.ykstakip.data.repository.SqlStudyRepository
import com.deniz0706.ykstakip.data.StudyPlanRepository
import com.deniz0706.ykstakip.domain.model.Exam
import com.deniz0706.ykstakip.domain.model.ExamType
import com.deniz0706.ykstakip.domain.model.SubjectResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class YksNavGraphDataWiringTest {

    private val context: Context =
        ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun exam_repository_can_read_exams() = runBlocking {
        val repo = SqlExamRepository(context.applicationContext)
        val exams = repo.getAllExams().first()
        assertNotNull(exams)
    }

    @Test
    fun study_repository_can_read_study_days() = runBlocking {
        val repo = SqlStudyRepository(context.applicationContext)
        val days = repo.getAll()
        assertNotNull(days)
    }

    @Test
    fun learning_repository_can_read_outcomes() {
        runBlocking {
            val repo = SqlLearningRepository(context.applicationContext)
            val outcomes = repo.getOutcomes().first()
            assertNotNull(outcomes)
        }
    }

    @Test
    fun study_plan_repository_can_read_plan() = runBlocking {
        val repo = StudyPlanRepository(context.applicationContext)
        val plan = repo.getAll()
        assertNotNull(plan)
    }

    @Test
    fun exam_has_valid_subjects() {
        val exam = Exam(
            id = 1,
            title = "TYT Deneme",
            date = "2025-01-01",
            type = ExamType.TYT,
            subjects = listOf(
                SubjectResult("Türkçe", 40, 0, 0, 40)
            )
        )
        assert(exam.totalCorrect == 40)
        assert(exam.totalWrong == 0)
        assert(exam.totalBlank == 0)
    }

    @Test
    fun app_settings_obp_roundtrip() {
        val appContext = context.applicationContext
        val original = com.deniz0706.ykstakip.data.AppSettings.getObp(appContext)
        com.deniz0706.ykstakip.data.AppSettings.setObp(appContext, 450.0f)
        val after = com.deniz0706.ykstakip.data.AppSettings.getObp(appContext)
        org.junit.Assert.assertEquals(450.0f, after!!, 0.001f)
        com.deniz0706.ykstakip.data.AppSettings.setObp(appContext, original)
    }

    @Test
    fun app_settings_target_net_roundtrip() {
        val appContext = context.applicationContext
        val original = com.deniz0706.ykstakip.data.AppSettings.getTargetNet(appContext, ExamType.TYT)
        com.deniz0706.ykstakip.data.AppSettings.setTargetNet(appContext, ExamType.TYT, 300.0f)
        val after = com.deniz0706.ykstakip.data.AppSettings.getTargetNet(appContext, ExamType.TYT)
        org.junit.Assert.assertEquals(300.0f, after!!, 0.001f)
        original?.let { com.deniz0706.ykstakip.data.AppSettings.setTargetNet(appContext, ExamType.TYT, it) }
    }

    @Test
    fun app_settings_daily_goal_roundtrip() {
        val appContext = context.applicationContext
        val original = com.deniz0706.ykstakip.data.AppSettings.getDailyGoal(appContext)
        com.deniz0706.ykstakip.data.AppSettings.setDailyGoal(appContext, 10)
        val after = com.deniz0706.ykstakip.data.AppSettings.getDailyGoal(appContext)
        org.junit.Assert.assertEquals(10, after)
        original?.let { com.deniz0706.ykstakip.data.AppSettings.setDailyGoal(appContext, it) }
    }

    @Test
    fun ai_memory_repository_all_returns_list() {
        val repo = com.deniz0706.ykstakip.data.AiMemoryRepository(context.applicationContext)
        val memories = repo.all()
        assertNotNull(memories)
    }

}
