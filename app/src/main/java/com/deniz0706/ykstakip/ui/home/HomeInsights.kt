package com.deniz0706.ykstakip.ui.home

/** Pure text/number rules behind the home screen, kept out of Compose so they can be unit-tested. */
object HomeInsights {
    fun subtitle(todayQuestions: Int, dailyGoal: Int?): String = when {
        dailyGoal != null && dailyGoal > 0 && todayQuestions >= dailyGoal -> "Günlük hedefini tamamladın."
        dailyGoal != null && dailyGoal > 0 -> "Bugün $todayQuestions / $dailyGoal soru."
        todayQuestions > 0 -> "Bugün $todayQuestions soru çözdün."
        else -> "Hazırlığını verilerle takip et."
    }

    /** Share of the goal reached in percent; null when the input is missing or unusable. */
    fun goalPercent(latest: Double?, goal: Float?): Int? {
        if (latest == null || goal == null || !latest.isFinite() || !goal.isFinite() || goal <= 0f) return null
        return ((latest / goal) * 100).toInt().coerceIn(0, 999)
    }

    fun trendSentence(recentAvg: Double?, olderAvg: Double?, recentCount: Int): String = when {
        recentAvg == null -> "İlk denemeni eklediğinde gelişim analizin burada görünecek."
        olderAvg == null -> "Son $recentCount denemenin ortalaması hazır. Dönem karşılaştırması için en az 5 eski deneme daha ekle."
        recentAvg - olderAvg > 0.5 -> "Son 5 denemede yükseliş var. Önceki 5 denemeye göre ortalaman arttı."
        recentAvg - olderAvg < -0.5 -> "Son 5 denemede ortalama geriledi. Zayıf branşlarını inceleyebilirsin."
        else -> "Son 5 denemede performansın dengeli seyrediyor."
    }
}