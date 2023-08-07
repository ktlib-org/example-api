package web.routes

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.OrganizationData
import entities.organization.OrganizationUser
import entities.organization.Organizations
import entities.user.*
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.http.Context
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.ktapi.entities.findAll
import org.ktapi.web.Router
import web.ApiRole
import web.userOrNull

object EmployeeRouter : Router {
    override fun route() {
        path("/employee") {
            get(this::isEmployee, ApiRole.Anyone)
            get("/organizations", this::organizations)
            get("/users", this::users)
            get("/user-logins", this::userLogins)
        }
    }

    private const val tag = "Employee"

    data class IsEmployee(val value: Boolean)

    @OpenApi(
        path = "/employee",
        methods = [HttpMethod.GET],
        operationId = "isEmployee",
        summary = "Returns true if user is employee",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Boolean::class)])]
    )
    private fun isEmployee(ctx: Context) {
        ctx.json(ctx.userOrNull?.employee == true)
    }

    @OpenApi(
        path = "/employee/organizations",
        methods = [HttpMethod.GET],
        operationId = "employeeOrganizations",
        summary = "Returns all organization",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Array<OrganizationData>::class)])]
    )
    private fun organizations(ctx: Context) {
        ctx.json(Organizations.findAll())
    }

    data class UserRoleData(@JsonIgnore private val orgUser: OrganizationUser) {
        val organizationId = orgUser.organizationId
        val userId = orgUser.userId
        val role = orgUser.role
        val createdAt = orgUser.createdAt
        val updatedAt = orgUser.updatedAt
    }

    data class UserDataAll(@JsonIgnore private val user: User) {
        val id = user.id
        val firstName = user.firstName
        val lastName = user.lastName
        val email = user.email
        val employee = user.employee
        val passwordSet = user.passwordSet
        val enabled = user.enabled
        val locked = user.locked
        val passwordFailures = user.passwordFailures
        val roles = user.roles.map { UserRoleData(it) }
    }

    @OpenApi(
        path = "/employee/users",
        methods = [HttpMethod.GET],
        operationId = "employeeUsers",
        summary = "Returns all users",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Array<UserDataAll>::class)])]
    )
    private fun users(ctx: Context) {
        ctx.json(Users.findAll().preloadRoles().map { UserDataAll(it) })
    }

    data class UserLoginData(@JsonIgnore private val userLogin: UserLogin) {
        val id = userLogin.id
        val userId = userLogin.userId
        val parentId = userLogin.parentId
        val valid = userLogin.valid
        val firstName = userLogin.user.firstName
        val lastName = userLogin.user.lastName
        val email = userLogin.user.email
        val employee = userLogin.user.employee
    }

    @OpenApi(
        path = "/employee/user-logins",
        methods = [HttpMethod.GET],
        operationId = "userLogins",
        summary = "Returns user logins",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Array<UserLoginData>::class)])]
    )
    private fun userLogins(ctx: Context) {
        ctx.json(UserLogins.findRecent().preloadUsers().map { UserLoginData(it) })
    }
}
