package service

import model.PerformanceData
import model.PerformanceInfos
import org.ktapi.Application
import org.ktapi.minutesAgo
import org.ktapi.toStartOfMinute
import org.ktapi.trace.TraceData
import java.time.LocalDateTime

object PerformanceService {
    fun loadRecentAndProcess() {
        process(Application.TraceLogger.loadTraceData(6.minutesAgo()..LocalDateTime.now(), null, null, null, null))
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