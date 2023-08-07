package services

import entities.Job
import entities.Jobs
import mu.KotlinLogging
import org.ktapi.Application
import org.ktapi.reportAndSwallow
import org.ktapi.trace.Trace
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.round
import kotlin.reflect.full.memberFunctions

object JobService : TimerTask() {
    private val logger = KotlinLogging.logger {}
    private const val timeBetweenRuns: Long = 1000 * 60
    private val offset = round(Random().nextFloat() * 2000).toLong()

    fun start() = Timer().scheduleAtFixedRate(this, offset, timeBetweenRuns)

    override fun run() {
        reportAndSwallow {
            val jobs = Jobs.findAllEnabled()
            if (jobs.isNotEmpty()) {
                logger.warn("Processing jobs")
                Trace.start("Background", "processJobs", mapOf("class" to this::class.qualifiedName!!))
                jobs.forEach { processJob(it) }
                Trace.end()
            }
        }
    }

    fun processJob(job: Job) {
        reportAndSwallow {
            val now = ZonedDateTime.now()
            logger.debug { "Processing job ${job.name}" }
            if (job.shouldRun(now) && updateLastRun(job, now)) {
                val clazz = Class.forName(job.className).kotlin

                if (clazz.objectInstance == null) {
                    Application.ErrorReporter.report("Invalid job setup. The class is not an object: ${job.className}")
                } else {
                    val function = clazz.memberFunctions.find { it.name == job.classFunctionName }
                    if (function == null) {
                        Application.ErrorReporter.report("Invalid job setup. The function does not exist: ${job.function}")
                    } else {
                        Thread {
                            logger.warn("Running scheduled job ${job.name}")
                            Trace.start("Job", job.name, mapOf("function" to job.function))
                            function.call(clazz.objectInstance)
                            Trace.end()
                            logger.warn("Finished scheduled job ${job.name}")
                        }.start()
                    }
                }
            }
        }
    }

    private fun updateLastRun(job: Job, now: ZonedDateTime) =
        Jobs.updateStartTime(job.id, job.lastStartTime, now.toLocalDateTime())
}