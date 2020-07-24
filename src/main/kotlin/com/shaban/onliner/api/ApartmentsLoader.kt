package com.shaban.onliner.api

import com.github.kittinunf.fuel.core.Parameters
import com.github.kittinunf.fuel.gson.gsonDeserializer
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.rx.rxObject
import com.shaban.onliner.api.model.ApartmentsResponse
import com.shaban.onliner.api.model.ApiApartment
import com.shaban.onliner.model.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.atomicfu.atomic
import mu.KotlinLogging
import org.locationtech.spatial4j.context.SpatialContext
import org.locationtech.spatial4j.shape.Rectangle
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ApartmentsLoader {
    private val logger = KotlinLogging.logger {}

    fun getRegionMetaDataSRx(region: Rectangle): Single<RegionMetaData> {
        return getApartmentsPageSRx(region = region)
                .map { RegionMetaData(it.total, it.page.last, it.page.limit) }
    }

    fun fetchApartmentsORx(region: Rectangle): Observable<Apartment> {
        val count = atomic(0L)
        return getApartmentsPageSRx(1, region, true)
                .flatMapObservable { apartmentsResponse ->
                    var result = Observable.fromIterable(apartmentsResponse.apartments)
                    for (i in apartmentsResponse.page.current + 1..apartmentsResponse.page.last)
                        result = result.mergeWith(getApartmentsPageSRx(i, region, true)
                                .flatMapObservable { t -> Observable.fromIterable(t.apartments) })

                    result
                }
                .map(this::convertApiApartment)
                .doOnNext { count.incrementAndGet() }
                .doOnSubscribe { logger.info { "Start fetching apartments from pk.api.onliner.by for region=$region" } }
                .doOnComplete { logger.info { "Fetched $count apartments from pk.api.onliner.by for region=$region" } }
    }

    private fun getApartmentsPageSRx(pageNumber: Int = 1, region: Rectangle = SpatialContext.GEO.worldBounds, log: Boolean = false): Single<ApartmentsResponse> {
        return Single
                .defer {
                    "https://pk.api.onliner.by/search/apartments"
                            .httpGet(getParameters(pageNumber, region))
                            .rxObject(gsonDeserializer<ApartmentsResponse>())
                            .map { it.get() }
                }
                .doOnSubscribe { if (log) logger.info { "Start fetching page № $pageNumber for region:$region" } }
                .doOnSuccess { if (log) logger.info { "Fetched page № $pageNumber for region:$region" } }
                .doOnError { if (log) logger.error(it) { "Error on fetching page № $pageNumber for region:$region" } }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
    }

    private fun getParameters(pageNumber: Int, region: Rectangle): Parameters {
        return listOf(
                "page" to pageNumber,
                "bounds[lb][lat]" to region.minY,
                "bounds[lb][long]" to region.minX,
                "bounds[rt][lat]" to region.maxY,
                "bounds[rt][long]" to region.maxX
        )
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