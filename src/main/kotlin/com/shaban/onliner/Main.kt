package com.shaban.onliner

import com.shaban.onliner.api.ApartmentsLoader
import com.shaban.onliner.dao.MySqlApartmentsDao
import com.shaban.onliner.logic.Trend
import com.shaban.onliner.logic.TrendsLogic
import com.shaban.onliner.service.ApartmentsDataService
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant


/**

+ 1 - grab data from onliner several times per day (6 times)

2 - calc trend:
+ 1.a - trend of cost of flats (compare cost of every flat) Show like that: 1-room flat - 23% goes down, 77% goes up.  2-rooms flat ...
1.b - trend of average cost square meter in 1-2-3-4 rooms flat in whole Minsk

2 - start service at some cloud (amazon, google, azure)

3 - visualize trend
4 - make possible to see trends for area ( leonida bedy street)


 */

val logger = KotlinLogging.logger {}

fun main(args: Array<String>) {

    logger.info { "Hello, World!" }


    val apartmentsDao = MySqlApartmentsDao()
    apartmentsDao.initDatabase()

    ApartmentsLoader()
            .loadAllApartmentsORx()
            .flatMapCompletable { apartmentsDao.saveApartmentCRx(it) }
            .blockingAwait()

    printAllFlatsTrend(apartmentsDao)
    runApartmentsService(apartmentsDao)
}

private fun printAllFlatsTrend(apartmentsDao: MySqlApartmentsDao) {
    val trendOverAllFlatsList = TrendsLogic(apartmentsDao)
            .getTrendOverAllFlatsSRx(Instant.now().minus(Duration.ofDays(4)), Instant.now())
            .blockingGet()

    val map = trendOverAllFlatsList.map { it.first to Pair(it.second, it.third) }.toMap()
    map.keys.forEach {
        val apartmentIdsString = if (it != Trend.STAND) "-->${map[it]?.second.toString()}" else ""
        logger.info { -> "$it --> ${map[it]?.first} $apartmentIdsString" }
    }
}

private fun runApartmentsService(apartmentsDao: MySqlApartmentsDao) {
    ApartmentsDataService(ApartmentsLoader(), apartmentsDao)
            .runCRx()
            .blockingAwait()
}


