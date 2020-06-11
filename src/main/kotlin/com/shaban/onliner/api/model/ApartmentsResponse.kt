package com.shaban.onliner.api.model

data class ApartmentsResponse(
        val apartments: List<ApiApartment>,
        val total: Int,
        val page: Page
)