package adapters

import adapters.db.EntityInitializer
import adapters.web.WebServer
import org.ktlib.Application

fun main() = Application {
    EntityInitializer.init()
    JobRunner.start()
    WebServer.start()
}
