package adapters.db

import entities.PerformanceData
import entities.PerformanceInfo
import entities.PerformanceInfoRepo
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.less
import org.ktorm.jackson.json
import org.ktorm.schema.datetime
import java.time.LocalDateTime

interface PerformanceInfoKtorm : EntityKtorm<PerformanceInfoKtorm>, PerformanceInfo

object PerformanceInfoTable : Table<PerformanceInfoKtorm, PerformanceInfo>("performance_info"),
    PerformanceInfoRepo {
    val time = datetime("time").bindTo { it.time }
    val data = json<List<PerformanceData>>("data").bindTo { it.data }

    override fun findByTime(time: LocalDateTime) = findOne { it.time eq time }

    override fun create(time: LocalDateTime, data: List<PerformanceData>) = insert {
        set(it.id, generateId())
        set(it.time, time)
        set(it.data, data)
    }

    override fun updateData(time: LocalDateTime, data: List<PerformanceData>) = update {
        set(it.data, data)
        where { it.time eq time }
    }

    override fun findInRange(range: ClosedRange<LocalDateTime>) = findList {
        (time greaterEq range.start) and (time less range.endInclusive)
    }
}