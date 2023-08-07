package web

import entities.organization.OrganizationUsers
import entities.organization.UserRole
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.HttpStatus
import io.javalin.security.RouteRole
import org.ktapi.web.Javalin

object WebServer : Javalin(AccessManager)

enum class ApiRole : RouteRole {
    Owner, Admin, User, UserNoOrg, Anyone;

    val userRole: UserRole? by lazy { UserRole.values().find { it.name == name } }
}

object AccessManager : io.javalin.security.AccessManager {
    override fun manage(handler: Handler, ctx: Context, routeRoles: Set<RouteRole>) {
        when {
            ctx.path().startsWith("/employee/") -> {
                if (ctx.userOrNull?.employee == true) handler.handle(ctx) else ctx.status(HttpStatus.NOT_FOUND)
            }

            routeRoles.contains(ApiRole.Anyone) -> handler.handle(ctx)
            routeRoles.contains(ApiRole.UserNoOrg) && ctx.userLoginOrNull != null -> handler.handle(ctx)
            userHasRole(ctx, routeRoles) -> handler.handle(ctx)
            else -> ctx.status(HttpStatus.UNAUTHORIZED).json("Unauthorized")
        }
    }

    private fun userHasRole(ctx: Context, roles: Set<RouteRole>) =
        OrganizationUsers.findByUserIdAndOrganizationId(ctx.userId, ctx.organizationId)
            ?.hasPermission(roles.mapNotNull { (it as ApiRole).userRole }) == true
}