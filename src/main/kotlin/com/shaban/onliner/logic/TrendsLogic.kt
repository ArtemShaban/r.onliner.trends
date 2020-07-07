package com.shaban.onliner.logic

import com.shaban.onliner.dao.ApartmentsDao
import com.shaban.onliner.model.Price
import io.reactivex.Observable
import io.reactivex.Single
import mu.KotlinLogging
import java.time.Instant

class TrendsLogic(private val apartmentsDao: ApartmentsDao) {

    private val logger = KotlinLogging.logger {}

    fun getPricesOverAllTimeForApartmentORx(apartmentId: Long): Observable<Price> {
        return apartmentsDao.loadAllPricesORx(apartmentId)
    }

    fun getTrendOverAllTimeForApartment(apartmentId: Long): Single<Trend> {
        return apartmentsDao
                .loadAllPricesORx(apartmentId)
                .toList()
                .map { calcUsdTrend(it) }
    }

    fun getTrendOverAllFlatsSRx(fromDay: Instant, toDay: Instant): Single<List<Triple<Trend, Double, List<Long>>>> {
        return Observable
                .merge(
                        apartmentsDao.loadAllPricesORx(fromDay),
                        apartmentsDao.loadAllPricesORx(toDay)
                )
                .groupBy { it.apartmentId }
                .flatMapSingle { groupedObservable ->
                    val apartmentId = groupedObservable.key!!
                    groupedObservable
                            .toList()
                            .map { prices ->
                                if (prices.size < 2) {
                                    logger.debug { "Can not calc trend for apartment id=$apartmentId" }
                                }
                                calcUsdTrend(prices)
                            }
                            .map { t -> Pair(apartmentId, t) }
                }
                .collect({ HashMap<Trend, List<Long>>() }, { map, apartmendIdTrend ->
                    map.compute(apartmendIdTrend.second) { _, apartmentIds ->
                        val list = apartmentIds ?: mutableListOf()
                        (list as MutableList).add(apartmendIdTrend.first)
                        list
                    }
                })
                .map { trendCountMap ->
                    val calcTrendsPercentage = calcTrendsPercentage(trendCountMap)
                    calcTrendsPercentage
                }

    }

    private fun calcUsdTrend(prices: List<Price>): Trend {
        val sortedPrices = prices.sortedBy { it.timestamp }
        val diff = sortedPrices.last().getUsdPrice() - sortedPrices.first().getUsdPrice()
        return when {
            diff < 0 -> Trend.FALL
            diff > 0 -> Trend.GROW
            else -> Trend.STAND
        }
    }

    private fun calcTrendsPercentage(trendCountMap: Map<Trend, List<Long>>): List<Triple<Trend, Double, List<Long>>> {
        var totalCount: Long = 0
        Trend.values().forEach { totalCount += trendCountMap[it]?.size ?: 0 }
        return Trend.values().map { trend: Trend ->
            val percentage = (trendCountMap[trend]?.size ?: 0) / totalCount.toDouble() * 100
            val apartmentIds = trendCountMap[trend] ?: emptyList()
            Triple(trend, percentage, apartmentIds)
        }.toList()
    }

    data class TrendResult(
            val roomsCount: Int,
            val trendPercent: List<Pair<Trend, Double>>
    )

}