package com.shaban.onliner.dao

import com.shaban.onliner.model.Apartment
import com.shaban.onliner.model.Price
import io.reactivex.Completable
import io.reactivex.Observable
import java.time.Instant

interface ApartmentsDao {

    fun saveApartmentCRx(apartment: Apartment): Completable

    fun loadAllPricesORx(day: Instant): Observable<Price>
    fun loadAllPricesORx(apartmentId: Long): Observable<Price>

    fun loadAllApartmentsWithPricesForDayORx(pricesForDay: Instant): Observable<Apartment>
}