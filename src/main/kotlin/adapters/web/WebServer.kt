package adapters.web

import org.ktlib.instancesFromFilesRelativeToClass
import org.ktlib.web.Javalin
import org.ktlib.web.Router

object WebServer : Javalin({
    routes {
        instancesFromFilesRelativeToClass<WebServer, Router>().forEach { it.route() }
    }
})
