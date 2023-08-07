package entities

import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.ktapi.entities.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.isNull
import org.ktorm.entity.Entity
import org.ktorm.schema.boolean
import org.ktorm.schema.datetime
import org.ktorm.schema.varchar
import java.time.LocalDateTime
import java.time.ZonedDateTime

interface JobData : WithDates {
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

interface Job : EntityWithDates<Job>, JobData {
    companion object : Entity.Factory<Job>()
}

object Jobs : EntityWithDatesTable<Job>("job") {
    val enabled = boolean("enabled").bindTo { it.enabled }
    val lastStartTime = datetime("last_start_time").bindTo { it.lastStartTime }
    val function = varchar("function").bindTo { it.function }
    val cron = varchar("cron").bindTo { it.cron }
    val name = varchar("name").bindTo { it.name }

    fun findAllEnabled() = findList { enabled eq true }

    fun create(name: String, cron: String, function: String, enabled: Boolean) {
        insert {
            set(Jobs.name, name)
            set(Jobs.cron, cron)
            set(Jobs.function, function)
            set(Jobs.enabled, enabled)
        }
    }

    fun updateStartTime(id: Long, currentTime: LocalDateTime?, newTime: LocalDateTime) =
        if (currentTime == null) {
            update {
                set(lastStartTime, newTime)
                where { lastStartTime.isNull() and (it.id eq id) }
            }
        } else {
            update {
                set(lastStartTime, newTime)
                where { (lastStartTime eq currentTime) and (it.id eq id) }
            }
        }.let { it > 0 }
}
