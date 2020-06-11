package com.shaban.onliner.api.model

data class ApiApartment(
        val id: Long,
        val author_id: Long,
        val location: ApiLocation,
        val price: ApiPrice,
        val resale: Boolean,
        val number_of_rooms: Int,
        val floor: Int,
        val number_of_floors: Int,
        val area: ApiArea,
        val photo: String,
        val seller: ApiSeller,
        val created_at: String,
        val last_time_up: String,
        val up_available_in: Int,
        val url: String,
        val auction_bid: AuctionBid?
)