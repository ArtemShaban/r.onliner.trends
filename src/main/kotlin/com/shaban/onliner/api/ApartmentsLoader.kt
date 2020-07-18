package com.shaban.onliner.api

import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.rx.rxObject
import com.shaban.onliner.api.model.ApartmentsResponse
import com.shaban.onliner.api.model.ApiApartment
import com.shaban.onliner.model.*
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.atomicfu.atomic
import mu.KotlinLogging
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ApartmentsLoader {
    private val logger = KotlinLogging.logger {}

    fun fetchAllApartmentsORx(): Observable<Apartment> {
        val count = atomic(0L)
        return getApartmentsPageSRx(1)
                .flatMapObservable { apartmentsResponse ->
                    var result = Observable.fromIterable(apartmentsResponse.apartments)
                    for (i in apartmentsResponse.page.current + 1..apartmentsResponse.page.last)
                        result = result.mergeWith(getApartmentsPageSRx(i)
                                .flatMapObservable { t -> Observable.fromIterable(t.apartments) })

                    result
                }
                .map(this::convertApiApartment)
                .doOnNext { count.incrementAndGet() }
                .doOnSubscribe { logger.info { "Start fetching all apartments from pk.api.onliner.by" } }
                .doOnComplete { logger.info { "Fetched $count apartments from pk.api.onliner.by" } }
    }

    private fun getApartmentsPageSRx(pageNumber: Int): Single<ApartmentsResponse> {
        return "https://pk.api.onliner.by/search/apartments"
                .httpGet(listOf("page" to pageNumber))
                .rxObject(gsonDeserializer<ApartmentsResponse>())
                .map { it.get() }
    }

    private fun convertApiApartment(apiApartment: ApiApartment): Apartment {
        val location = apiApartment.location
        return Apartment(
                apiApartment.id,
                apiApartment.author_id,
                Location(location.latitude, location.longitude, location.user_address),
                convertPrices(apiApartment),
                apiApartment.resale,
                apiApartment.number_of_rooms,
                apiApartment.floor,
                apiApartment.number_of_floors,
                Area(apiApartment.area.total, apiApartment.area.living, apiApartment.area.kitchen ?: 0.toDouble()),
                apiApartment.photo,
                Seller.valueOf(apiApartment.seller.type.toUpperCase()),
                ZonedDateTime.parse(apiApartment.created_at, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")).toInstant().toEpochMilli(),
                apiApartment.url
        )
    }

    private fun convertPrices(apiApartment: ApiApartment): List<Price> {
        val price = apiApartment.price
        return listOf(Price(
                apiApartment.id,
                Instant.now().toEpochMilli(),
                price.amount.toDouble(),
                Currency.valueOf(price.currency.toUpperCase()),
                price.converted
                        .mapKeys { entry -> Currency.valueOf(entry.key.toUpperCase()) }
                        .mapValues { entry -> entry.value.amount }
        ))
    }
}