package com.shaban.onliner.service

import com.shaban.onliner.api.ApartmentsLoader
import com.shaban.onliner.dao.ApartmentsDao
import io.reactivex.Completable
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
    private val retryCount = 3L

    fun runCRx(): Completable {
        return Completable
                .defer {
                    val delay = getDelay()
                    logger.info { "We will fetch all apartments at ${Instant.now().plusMillis(delay)}" }
                    Completable
                            .timer(delay, TimeUnit.MILLISECONDS)
                            .andThen(fetchAllApartmentsAndSaveCRx())
                }
                .repeat()
                .subscribeOn(Schedulers.io())
    }

    private fun getDelay(): Long = period - (Instant.now().toEpochMilli() % period)

    private fun fetchAllApartmentsAndSaveCRx(): Completable {
        return Completable
                .defer {
                    apartmentsLoader
                            .fetchAllApartmentsORx()
                            .flatMapCompletable { apartmentsDao.saveApartmentCRx(it) }
                }
                .doOnComplete { logger.info { "All apartments have been fetched and saved successfully =)" } }
                .doOnError { logger.warn(it) { "Error on fetching and saving all apartments. We will retry $retryCount times." } }
                .retry(retryCount)
                .doOnError { logger.error(it) { "Fatal error on fetching and saving all apartments." } }
                .onErrorComplete()
    }
}