package com.shaban.onliner.api.model

data class Page(
        val limit: Int,
        val items: Int,
        val current: Int,
        val last: Int
)