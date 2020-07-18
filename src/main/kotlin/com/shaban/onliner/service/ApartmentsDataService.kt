package com.shaban.onliner.service

import com.shaban.onliner.api.ApartmentsLoader
import com.shaban.onliner.api.RegionMetaData
import com.shaban.onliner.dao.ApartmentsDao
import com.shaban.onliner.util.splitByX
import com.shaban.onliner.util.splitByY
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import org.locationtech.spatial4j.context.SpatialContext
import org.locationtech.spatial4j.shape.Rectangle
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ApartmentsDataService(
        private val apartmentsLoader: ApartmentsLoader,
        private val apartmentsDao: ApartmentsDao
) {
    private val logger = KotlinLogging.logger { }
    private val period = Duration.ofDays(1).toMillis()
    private val BELARUS = SpatialContext.GEO.shapeFactory.rect(23.1994938494, 32.6936430193, 51.3195034857, 56.1691299506) //x - longitude, y - latitude

    fun startCronCRx(): Completable {
        return Completable
                .defer {
                    val delay = getDelay()
                    logger.info { "We will fetch apartments at ${Instant.now().plusMillis(delay)}" }
                    Completable
                            .timer(delay, TimeUnit.MILLISECONDS)
                            .andThen(fetchAllApartmentsAndSaveSRx(3)
                                    .ignoreElement()
                                    .onErrorComplete()
                            )
                }
                .repeat()
                .subscribeOn(Schedulers.io())
    }

    private fun getDelay(): Long = period - (Instant.now().toEpochMilli() % period)

    fun fetchAllApartmentsAndSaveSRx(retryCount: Long = 0): Single<Long> {
        return getRegionsToFetchApartmentsORx()
                //use concat map to avoid throttling from onliner side
                .concatMap { region -> apartmentsLoader.fetchApartmentsORx(region) }
                .flatMap {
                    apartmentsDao
                            .saveApartmentCRx(it)
                            .andThen(Observable.just(it))
                }
                .retry { times, t ->
                    val retry = times < retryCount
                    if (retry) logger.warn(t) { "Error on fetching and saving apartments. Retry ${times + 1} time." }

                    retry
                }
                .retry(retryCount)
                .count()
                .doOnSuccess { count -> logger.info { "$count apartments have been fetched and saved successfully =)" } }
                .doOnError { logger.error(it) { "Fatal error on fetching and saving apartments." } }
    }

    private fun getRegionsToFetchApartmentsORx(): Observable<Rectangle> {
        return apartmentsLoader
                .getRegionMetaDataSRx(BELARUS)
                .flatMapObservable { metaData ->
                    val maxItemsCountPerRegion = metaData.pageCount * metaData.pageItemsLimit
                    getRegionsWithLimitedApartmentsCountORx(maxItemsCountPerRegion, BELARUS, metaData)
                }
    }

    private fun getRegionsWithLimitedApartmentsCountORx(limit: Int, baseRectangle: Rectangle, baseRectangleMetaData: RegionMetaData): Observable<Rectangle> {
        if (baseRectangleMetaData.total > limit) {
            logger.debug { "MetaData.total=${baseRectangleMetaData.total}. limit=$limit. split rectangle for 4 and check each new rectangle. rectangle=$baseRectangle" }
            return Observable
                    .just(baseRectangle)
                    .flatMap { rectangle ->
                        Observable.fromIterable(rectangle
                                .splitByX()
                                .toList()
                                .map { it.splitByY() }
                                .map { it.toList() }
                                .flatten()
                        )
                    }
                    //use concat map to avoid throttling from onliner side
                    .concatMap { rectangle ->
                        apartmentsLoader
                                .getRegionMetaDataSRx(rectangle)
                                .flatMapObservable { metaData -> getRegionsWithLimitedApartmentsCountORx(limit, rectangle, metaData) }
                    }

        } else {
            logger.debug { "MetaData.total=${baseRectangleMetaData.total}. limit=$limit. it's ok =) rectangle=$baseRectangle" }
            return Observable.just(baseRectangle)
        }
    }
}