package adapters

import adapters.web.WebServer
import org.ktlib.Application
import services.JobRunner

fun main() = Application {
    JobRunner.start()
    WebServer.start()
}
