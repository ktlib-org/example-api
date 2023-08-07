package entities

import entities.PerformanceInfos.create
import io.kotlintest.shouldBe
import org.ktapi.test.DbStringSpec
import org.ktapi.toStartOfMinute
import java.time.LocalDateTime

class PerformanceInfoTests : DbStringSpec({
    "inserting data" {
        val perf = PerformanceInfo {
            time = LocalDateTime.now().toStartOfMinute()
            data = listOf(PerformanceData("Background"))
        }.create()

        val result = PerformanceInfos.findById(perf.id)!!

        result.data shouldBe perf.data
    }
})