package com.shaban.onliner

import com.shaban.onliner.api.ApartmentsLoader
import com.shaban.onliner.dao.MySqlApartmentsDao
import com.shaban.onliner.logic.Trend
import com.shaban.onliner.logic.TrendsLogic
import com.shaban.onliner.service.ApartmentsDataService
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.coroutines.rx2.await
import mu.KotlinLogging
import java.time.Duration
import java.time.Instant


/**

+ 1 - grab data from onliner several times per day (6 times)

2 - calc trend:
+ 1.a - trend of cost of flats (compare cost of every flat) Show like that: 1-room flat - 23% goes down, 77% goes up.  2-rooms flat ...
1.b - trend of average cost square meter in 1-2-3-4 rooms flat in whole Minsk

+3 - start as service (ktot https://www.youtube.com/watch?v=zHQ7oBYSHrY)
+4 - start service at some cloud (amazon, google, azure)

4.1 - https://movile.blog/quickly-building-a-kotlin-rest-api-server-using-ktor/

5 - visualize trend
6 - make possible to see trends for area ( leonida bedy street)


 */

val logger = KotlinLogging.logger {}

fun Application.main() {

    val apartmentsDao = MySqlApartmentsDao()
    apartmentsDao.initDatabase()

    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {

        get("/about") { call.respondText("Online Trends", ContentType.Text.Html) }
        get("/") { handleGetTrends(call, apartmentsDao) }
        get("/trends") { handleGetTrends(call, apartmentsDao) }
        get("/apartment/{id}") { handleGetApartment(call, apartmentsDao) }
        get("/fetch") {
            val count = ApartmentsDataService(ApartmentsLoader(), apartmentsDao)
                    .fetchAllApartmentsAndSaveSRx()
                    .await()
            call.respondText { "$count apartments have been successfully fetched and saved to db" }
        }
    }

    logger.info { "Hello, World!" }

    runApartmentsService(apartmentsDao)
}

private suspend fun handleGetApartment(call: ApplicationCall, apartmentsDao: MySqlApartmentsDao) {
    val id = call.parameters["id"]
    val prices = TrendsLogic(apartmentsDao)
            .getPricesOverAllTimeForApartmentORx(id!!.toLong())
            .map { Pair(it.timestamp, it.getUsdPrice()) }
            .toList()
            .await()
    call.respondText(prices.toString(), ContentType.Text.Plain)
}

private suspend fun handleGetTrends(call: ApplicationCall, apartmentsDao: MySqlApartmentsDao) {
    val days = call.parameters["days"]?.toLong() ?: 5
    val allFlatsTrendString = getAllFlatsTrendString(apartmentsDao, days)
    call.respondText(allFlatsTrendString, ContentType.Text.Plain)
}

private fun getAllFlatsTrendString(apartmentsDao: MySqlApartmentsDao, days: Long): String {
    val trendOverAllFlatsList = TrendsLogic(apartmentsDao)
            .getTrendOverAllFlatsSRx(Instant.now().minus(Duration.ofDays(days)), Instant.now())
            .blockingGet()

    val map = trendOverAllFlatsList.map { it.first to Pair(it.second, it.third) }.toMap()
    val sb = StringBuilder()
    map.keys.forEach {
        val apartmentIdsString = if (it != Trend.STAND) "-->${map[it]?.second.toString()}" else ""
        sb.append("$it --> ${map[it]?.first} $apartmentIdsString").append("\n")
    }
    return sb.toString()
}

private fun runApartmentsService(apartmentsDao: MySqlApartmentsDao) {
    ApartmentsDataService(ApartmentsLoader(), apartmentsDao)
            .startCronCRx()
            .subscribe() //todo
}


