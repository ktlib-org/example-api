package services

import entities.PerformanceData
import entities.PerformanceInfos
import org.ktlib.minutesAgo
import org.ktlib.toStartOfMinute
import org.ktlib.trace.TraceData
import org.ktlib.trace.TraceLogger
import java.time.LocalDateTime

object PerformanceRunner {
    fun loadRecentAndProcess() {
        process(TraceLogger.loadTraceData(6.minutesAgo()..LocalDateTime.now(), null, null, null, null))
    }

    fun process(traceData: List<TraceData>) {
        if (traceData.isEmpty()) return
        val types = traceData.map { it.traceType }.toSet()

        val items = traceData
            .groupBy { Triple(it.startTime.dayOfYear, it.startTime.hour, it.startTime.minute) }
            .map { (_, items) ->
                val totals = mutableMapOf<String, PerformanceData>()
                types.fold(mutableMapOf<String, PerformanceData>()) { map, type ->
                    map[type] = PerformanceData(type)
                    map
                }
                items.forEach { totals[it.traceType]?.add(it) }

                val perfTime = items.first().startTime.toStartOfMinute()

                Pair(perfTime, totals.values.toList())
            }

        val start = items.minOf { it.first }
        val end = items.maxOf { it.first }.plusMinutes(1)
        val currentItems = PerformanceInfos.findInRange(start..end)

        items.forEach { (time, data) ->
            when (currentItems.find { it.time == time }) {
                null -> PerformanceInfos.create(time, data)
                else -> PerformanceInfos.updateData(time, data)
            }
        }
    }
}