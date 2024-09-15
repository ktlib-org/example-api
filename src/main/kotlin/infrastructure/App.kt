package infrastructure

import infrastructure.web.WebServer
import org.ktlib.Application
import domain.services.JobRunner

fun main() = Application {
    JobRunner.start()
    WebServer.start()
}
