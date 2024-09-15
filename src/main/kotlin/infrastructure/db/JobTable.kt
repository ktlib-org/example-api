package infrastructure.db

import domain.entities.Job
import domain.entities.JobRepo
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.isNull
import org.ktorm.schema.boolean
import org.ktorm.schema.datetime
import org.ktorm.schema.varchar
import java.time.LocalDateTime
import java.util.*

interface JobKtorm : EntityKtorm<JobKtorm>, Job

object JobTable : Table<JobKtorm, Job>("job"), JobRepo {
    val enabled = boolean("enabled").bindTo { it.enabled }
    val lastStartTime = datetime("last_start_time").bindTo { it.lastStartTime }
    val function = varchar("function").bindTo { it.function }
    val cron = varchar("cron").bindTo { it.cron }
    val name = varchar("name").bindTo { it.name }

    override fun findAllEnabled() = findList { enabled eq true }

    override fun create(name: String, cron: String, function: String, enabled: Boolean) {
        insert {
            set(it.id, generateId())
            set(it.name, name)
            set(it.cron, cron)
            set(it.function, function)
            set(it.enabled, enabled)
        }
    }

    override fun updateStartTime(id: UUID, currentTime: LocalDateTime?, newTime: LocalDateTime) =
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
