package com.shaban.onliner.model

data class Price(
        val apartmentId: Long,
        val timestamp: Long,
        val amount: Double,
        val currency: Currency,
        val converted: Map<Currency, Double>
) {
    fun getBynPrice() = converted.getValue(Currency.BYN)

    fun getUsdPrice() = converted.getValue(Currency.USD)
}