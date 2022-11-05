package api

import org.ktapi.Application
import org.ktapi.db.Database
import service.JobService

fun main() = Application {
    Database.init()
    JobService.start()
    WebServer.start()
}
