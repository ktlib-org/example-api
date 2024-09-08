package entities

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.ktlib.entities.Entity
import org.ktlib.entities.Repository
import org.ktlib.entities.Factory
import org.ktlib.lookupInstance
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

interface Job : Entity {
    companion object : Factory<Job>()

    var enabled: Boolean
    var lastStartTime: LocalDateTime?
    var name: String
    var cron: String?
    var function: String

    val className: String
        get() = function.substringBeforeLast(".")

    val classFunctionName: String
        get() = function.substringAfterLast(".")

    fun parseCron() =
        if (cron == null || cron == "once") {
            null
        } else {
            try {
                CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX)).parse(cron)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    fun shouldRun(time: ZonedDateTime = ZonedDateTime.now()) =
        when {
            !enabled -> false
            cron == "once" -> lastStartTime == null
            else -> {
                val parsedCron = parseCron()
                parsedCron != null && ExecutionTime.forCron(parsedCron).isMatch(time)
            }
        }
}

object Jobs : JobRepo by lookupInstance()

interface JobRepo : Repository<Job> {
    fun create(name: String, cron: String, function: String, enabled: Boolean)
    fun findAllEnabled(): List<Job>
    fun updateStartTime(id: UUID, currentTime: LocalDateTime?, newTime: LocalDateTime): Boolean
}
