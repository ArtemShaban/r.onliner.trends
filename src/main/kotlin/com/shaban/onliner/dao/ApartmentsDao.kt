package com.shaban.onliner.dao

import com.shaban.onliner.api.model.ApiApartment
import com.shaban.onliner.model.Apartment
import com.shaban.onliner.model.Price
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.time.Instant
import java.time.temporal.ChronoUnit

interface ApartmentsDao {

    fun saveApartmentCRx(apartment: Apartment): Completable

    fun loadAllPricesORx(day: Instant): Observable<Price>
    fun loadAllApartmentsWithPricesForDayORx(pricesForDay: Instant): Observable<Apartment>
}