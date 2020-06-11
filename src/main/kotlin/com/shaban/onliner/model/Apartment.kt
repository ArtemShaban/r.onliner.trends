package com.shaban.onliner.model

import com.shaban.onliner.util.dayIndex
import java.time.Instant

data class Apartment(
        val id: Long,
        val authorId: Long,
        val location: Location,
        val prices: List<Price>,
        val resale: Boolean,
        val numberOfRooms: Int,
        val floor: Int,
        val numberOfFloors: Int,
        val area: Area,
        val photo: String,
        val seller: Seller,
        val createdAt: Long,
        val url: String
) {

    fun getPricesForDay(day: Instant): List<Price> = prices.filter { it.timestamp.dayIndex() == day.dayIndex() }
}


