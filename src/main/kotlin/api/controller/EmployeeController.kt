package api.controller

import api.ApiRole
import api.userOrNull
import com.fasterxml.jackson.annotation.JsonIgnore
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import model.OrganizationData
import model.OrganizationUser
import model.Organizations
import model.user.*
import org.ktapi.model.findAll
import org.ktapi.web.Router
import org.ktapi.web.documentedHandler

object EmployeeController : Router {
    override fun route() {
        path("/employee") {
            get(isEmployee, ApiRole.Anyone)
            get("/organizations", organizations)
            get("/users", users)
            get("/user-logins", userLogins)
        }
    }

    private const val tag = "Employee"

    data class IsEmployee(val value: Boolean)

    private val isEmployee = documentedHandler {
        doc("isEmployee", "Returns true if user is employee", tag) {
            json<Boolean>("200")
        }
        handler { ctx -> ctx.json(ctx.userOrNull?.employee == true) }
    }

    private val organizations = documentedHandler {
        doc("organizations", "Returns all organization", tag) {
            jsonArray<OrganizationData>("200")
        }
        handler { ctx -> ctx.json(Organizations.findAll()) }
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

    private val users = documentedHandler {
        doc("users", "Returns all users", tag) {
            jsonArray<UserDataAll>("200")
        }
        handler { ctx -> ctx.json(Users.findAll().preloadRoles().map { UserDataAll(it) }) }
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

    private val userLogins = documentedHandler {
        doc("userLogins", "Returns user logins", tag) {
            jsonArray<UserLoginData>("200")
        }
        handler { ctx -> ctx.json(UserLogins.findRecent().preloadUsers().map { UserLoginData(it) }) }
    }
}
