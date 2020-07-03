package com.shaban.onliner.dao

import com.google.gson.Gson
import com.shaban.onliner.model.Apartment
import com.shaban.onliner.model.Price
import com.shaban.onliner.util.dayIndex
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

class MySqlApartmentsDao : ApartmentsDao {
    val gson = Gson()

    init {
        val dbCredentials = getDbCredentials()
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:" + dbCredentials.host + "/" + dbCredentials.dbName
            driverClassName = "com.mysql.cj.jdbc.Driver"
            username = dbCredentials.username
            password = dbCredentials.password
            maximumPoolSize = 10
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
    }

    object ApartmentsTable : Table("apartments") {
        val id: Column<Long> = long("id")
        val dayIndex = long("day_index")
        val timestamp = long("timestamp")
        val pureJson = text("pure_json")
        override val primaryKey = PrimaryKey(id, dayIndex, timestamp)
    }

    fun initDatabase() {
        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(ApartmentsTable)
        }
    }

    override fun saveApartmentCRx(apartment: Apartment): Completable {
        return execCRx {
            transaction {
                ApartmentsTable.insert {
                    it[id] = apartment.id
                    it[dayIndex] = apartment.prices[0].timestamp.dayIndex().toEpochMilli()
                    it[timestamp] = apartment.prices[0].timestamp
                    it[pureJson] = Gson().toJson(apartment)
                }
            }
        }
    }

    override fun loadAllApartmentsWithPricesForDayORx(pricesForDay: Instant): Observable<Apartment> {
        return execORx {
            var result = emptyList<Apartment>()
            transaction {
                result = ApartmentsTable
                        .selectAll()
                        .andWhere { Op.build { ApartmentsTable.dayIndex eq pricesForDay.dayIndex().toEpochMilli() } }
                        .map { gson.fromJson(it[ApartmentsTable.pureJson], Apartment::class.java) }
            }
            result
        }
    }

    override fun loadAllPricesORx(day: Instant): Observable<Price> {
        return execORx {
            var result = emptyList<Price>()
            transaction {
                result = ApartmentsTable
                        .selectAll()
                        .andWhere { Op.build { ApartmentsTable.dayIndex eq day.dayIndex().toEpochMilli() } }
                        .map { gson.fromJson(it[ApartmentsTable.pureJson], Apartment::class.java) }
                        .map { it.prices }
                        .flatten()
            }
            result
        }
    }


    private fun execCRx(request: () -> Unit): Completable = Completable
            .fromAction(request)
            .subscribeOn(Schedulers.io())

    private fun <T> execORx(request: () -> List<T>): Observable<T> = Observable
            .fromCallable(request)
            .flatMap { Observable.fromIterable(it) }
            .subscribeOn(Schedulers.io())

}