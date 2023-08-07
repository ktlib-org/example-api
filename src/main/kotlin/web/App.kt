package web

import org.ktapi.Application
import org.ktapi.db.Database
import services.JobService

fun main() = Application {
    Database.init()
    JobService.start()
    WebServer.start()
}
