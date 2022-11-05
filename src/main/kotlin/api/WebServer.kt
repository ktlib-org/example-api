package api

import io.javalin.core.security.RouteRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpCode
import model.UserRole
import org.ktapi.web.Javalin
import service.UserService

object WebServer : Javalin(AccessManager)

enum class ApiRole : RouteRole {
    Owner, Admin, User, UserNoOrg, Anyone;

    val userRole: UserRole? by lazy { UserRole.values().find { it.name == name } }
}

object AccessManager : io.javalin.core.security.AccessManager {
    override fun manage(handler: Handler, ctx: Context, permittedRoles: MutableSet<RouteRole>) {
        when {
            ctx.path().startsWith("/employee/") -> {
                if (ctx.user.employee) handler.handle(ctx) else ctx.status(HttpCode.NOT_FOUND)
            }

            permittedRoles.contains(ApiRole.Anyone) -> handler.handle(ctx)
            permittedRoles.contains(ApiRole.UserNoOrg) && ctx.userLoginOrNull != null -> handler.handle(ctx)
            userHasRole(ctx, permittedRoles) -> handler.handle(ctx)
            else -> ctx.status(HttpCode.UNAUTHORIZED).json("Unauthorized")
        }
    }

    private fun userHasRole(ctx: Context, roles: MutableSet<RouteRole>) =
        UserService.hasPermission(ctx.organizationId, ctx.userId, roles.mapNotNull { (it as ApiRole).userRole })
}