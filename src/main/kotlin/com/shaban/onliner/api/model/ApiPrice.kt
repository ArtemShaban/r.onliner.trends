package com.shaban.onliner.api.model

data class ApiPrice(
        val amount: String,
        val currency: String,
        val converted: Map<String, Converted>
) {
    public data class Converted(
           public val amount: Double,
           public  val currency: String
    )
}

