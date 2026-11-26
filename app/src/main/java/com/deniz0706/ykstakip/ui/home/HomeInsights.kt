package com.deniz0706.ykstakip.ui.home

/** Pure text/number rules behind the home screen, kept out of Compose so they can be unit-tested. */
object HomeInsights {
    fun subtitle(todayQuestions: Int, dailyGoal: Int?): String = when {
        dailyGoal != null && dailyGoal > 0 && todayQuestions >= dailyGoal -> "Günlük hedefini tamamladın."
        dailyGoal != null && dailyGoal > 0 -> "Bugün $todayQuestions / $dailyGoal soru."
        todayQuestions > 0 -> "Bugün $todayQuestions soru çözdün."
        else -> "Hazırlığını verilerle takip et."
    }

    /** Share of the goal reached in percent; null when there is no usable goal or exam. */
    fun goalPercent(latest: Double?, goal: Float?): Int? =
        if (latest == null || goal == null || goal <= 0f) null
        else ((latest / goal) * 100).toInt().coerceIn(0, 999)

    fun trendSentence(recentAvg: Double?, olderAvg: Double?, recentCount: Int): String = when {
        recentAvg == null -> "İlk denemeni eklediğinde gelişim analizin burada görünecek."
        olderAvg == null -> "İlk $recentCount denemen kaydedildi. Birkaç deneme daha ekleyince dönemleri karşılaştırabileceksin."
        recentAvg - olderAvg > 0.5 -> "Son 5 denemede yükseliş var. Önceki 5 denemeye göre ortalaman arttı."
        recentAvg - olderAvg < -0.5 -> "Son 5 denemede ortalama geriledi. Zayıf branşlarını inceleyebilirsin."
        else -> "Son 5 denemede performansın dengeli seyrediyor."
    }
}
