package com.shaban.onliner.service

import com.shaban.onliner.api.ApartmentsLoader
import com.shaban.onliner.dao.ApartmentsDao
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

class ApartmentsDataService(
        private val apartmentsLoader: ApartmentsLoader,
        private val apartmentsDao: ApartmentsDao
) {
    private val logger = KotlinLogging.logger { }
    private val period = Duration.ofDays(1).toMillis()

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
        return apartmentsLoader
                .fetchAllApartmentsORx()
                .flatMap {
                    apartmentsDao
                            .saveApartmentCRx(it)
                            .andThen(Observable.just(it))
                }
                .doOnComplete { logger.info { "All apartments have been fetched and saved successfully =)" } }
                .retry { times, t ->
                    val retry = times < retryCount
                    if (retry) logger.warn(t) { "Error on fetching and saving apartments. Retry ${times + 1} time." }

                    retry
                }
                .retry(retryCount)
                .doOnError { logger.error(it) { "Fatal error on fetching and saving apartments." } }
                .count()
    }
}