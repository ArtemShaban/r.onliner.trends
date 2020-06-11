package com.shaban.onliner.util

import java.time.Instant
import java.time.temporal.ChronoUnit

fun Instant.dayIndex(): Instant = this.truncatedTo(ChronoUnit.DAYS)
fun Long.dayIndex(): Instant = Instant.ofEpochMilli(this).dayIndex()

