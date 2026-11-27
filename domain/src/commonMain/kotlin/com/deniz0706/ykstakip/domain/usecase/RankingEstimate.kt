package com.deniz0706.ykstakip.domain.usecase

/** Ranking estimate result shared across all platforms. */
data class RankingEstimate(
    val center: Int,
    val lower: Int,
    val upper: Int
)
