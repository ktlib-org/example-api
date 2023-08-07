package entities

import org.ktapi.entities.*
import org.ktapi.trace.TraceData
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.greaterEq
import org.ktorm.dsl.less
import org.ktorm.entity.Entity
import org.ktorm.jackson.json
import org.ktorm.schema.datetime
import java.time.LocalDateTime

data class PerformanceData(
    val type: String,
    var count: Int = 0,
    var time: Long = 0,
    var dbCount: Int = 0,
    var dbTime: Long = 0
) {
    fun add(data: TraceData) {
        if (data.traceType == type) {
            count++
            time += data.duration
            dbCount += data.dbRequests
            dbTime += data.dbTime
        }
    }
}

interface PerformanceInfoData : WithId {
    var time: LocalDateTime
    var data: List<PerformanceData>
}

interface PerformanceInfo : EntityWithId<PerformanceInfo>, PerformanceInfoData {
    companion object : Entity.Factory<PerformanceInfo>()
}

object PerformanceInfos : EntityTable<PerformanceInfo>("performance_info") {
    val time = datetime("time").bindTo { it.time }
    val data = json<List<PerformanceData>>("data").bindTo { it.data }

    fun findByTime(time: LocalDateTime) = findOne { PerformanceInfos.time eq time }

    fun create(time: LocalDateTime, data: List<PerformanceData>) = insert {
        set(PerformanceInfos.time, time)
        set(PerformanceInfos.data, data)
    }

    fun updateData(time: LocalDateTime, data: List<PerformanceData>) = update {
        set(PerformanceInfos.data, data)
        where { PerformanceInfos.time eq time }
    }

    fun findInRange(range: ClosedRange<LocalDateTime>) = findList {
        (time greaterEq range.start) and (time less range.endInclusive)
    }
}
