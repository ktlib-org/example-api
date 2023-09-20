package adapters.db

import entities.PerformanceData
import entities.PerformanceInfo
import entities.PerformanceInfoStore
import org.ktlib.db.IdGenerator
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.less
import org.ktorm.jackson.json
import org.ktorm.schema.datetime
import java.time.LocalDateTime

interface PerformanceInfoKtorm : KtormEntity<PerformanceInfoKtorm>, PerformanceInfo

object PerformanceInfoTable :
    KtormEntityTable<PerformanceInfoKtorm, PerformanceInfo, PerformanceInfoStore>("performance_info"),
    PerformanceInfoStore {
    val time = datetime("time").bindTo { it.time }
    val data = json<List<PerformanceData>>("data").bindTo { it.data }

    override fun findByTime(time: LocalDateTime) = findOne { it.time eq time }

    override fun create(time: LocalDateTime, data: List<PerformanceData>) = insert {
        set(it.id, IdGenerator.generate())
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