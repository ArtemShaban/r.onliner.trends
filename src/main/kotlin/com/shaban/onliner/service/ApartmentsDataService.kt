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

    fun runCRx(): Completable {
        return Completable
                .defer {
                    val delay = getDelay()
                    logger.info { "We will fetch all apartments at ${Instant.now().plusMillis(delay)}" }
                    Completable
                            .timer(delay, TimeUnit.MILLISECONDS)
                            .andThen(loadAndSaveApartmentsCRx())
                }
                .repeat()
                .subscribeOn(Schedulers.io())
    }

    private fun getDelay(): Long = period - (Instant.now().toEpochMilli() % period)

    private fun loadAndSaveApartmentsCRx(): Completable {
        return Completable.defer {
            apartmentsLoader
                    .loadAllApartmentsORx()
                    .flatMapCompletable { apartmentsDao.saveApartmentCRx(it) }
        }
    }
}