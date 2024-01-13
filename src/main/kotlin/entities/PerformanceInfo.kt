package entities

import org.ktlib.entities.Entity
import org.ktlib.entities.Repository
import org.ktlib.entities.Factory
import org.ktlib.lookup
import org.ktlib.trace.TraceData
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

interface PerformanceInfo : Entity {
    companion object : Factory<PerformanceInfo>()

    var time: LocalDateTime
    var data: List<PerformanceData>
}

object PerformanceInfos : PerformanceInfoRepo by lookup()

interface PerformanceInfoRepo : Repository<PerformanceInfo> {
    fun findByTime(time: LocalDateTime): PerformanceInfo?
    fun create(time: LocalDateTime, data: List<PerformanceData>): Int
    fun updateData(time: LocalDateTime, data: List<PerformanceData>): Int
    fun findInRange(range: ClosedRange<LocalDateTime>): List<PerformanceInfo>
}
