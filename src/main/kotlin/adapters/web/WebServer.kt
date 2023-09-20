package adapters.web

import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.security.RouteRole
import org.ktlib.instancesFromFilesRelativeToClass
import org.ktlib.web.Javalin
import org.ktlib.web.Router

object WebServer : Javalin(AccessManager, {
    routes {
        instancesFromFilesRelativeToClass<WebServer, Router>().forEach { it.route() }
    }
})

object AccessManager : io.javalin.security.AccessManager {
    override fun manage(handler: Handler, ctx: Context, routeRoles: Set<RouteRole>) {
        when {
            ctx.path().startsWith("use-cases/employee/") -> {
                if (ctx.userLoginOrNull?.user?.employee == true) handler.handle(ctx) else ctx.status(HttpStatus.NOT_FOUND)
            }

            else -> handler.handle(ctx)
        }
    }
}