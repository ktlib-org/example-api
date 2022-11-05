package api.controller

import api.*
import api.ApiRole.Anyone
import api.ApiRole.UserNoOrg
import com.fasterxml.jackson.annotation.JsonIgnore
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpCode
import model.OrganizationUser
import model.OrganizationUsers
import model.preloadOrganizations
import model.user.User
import model.user.UserLogin
import model.user.UserLoginData
import model.user.Users
import org.ktapi.web.Router
import org.ktapi.web.documentedHandler
import org.ktapi.web.jsonValidationErrors
import service.UserService
import service.UserService.UserUpdate

object UserController : Router {
    override fun route() {
        path("/user") {
            get(currentUser, UserNoOrg)
            patch(updateUserInfo, UserNoOrg)
            post("/signup", signup, Anyone)
            post("/verify-email", verifyEmail, Anyone)
            post("/forgot-password", forgotPassword, Anyone)
            post("/token-login", tokenLogin, Anyone)
            post("/login", login, Anyone)
            post("/update-password", updatePassword, UserNoOrg)
            post("/logout", logout, UserNoOrg)
            post("/accept-invite", acceptInvite, Anyone)
        }
    }

    private const val tag = "User"

    private val verifyEmail = documentedHandler {
        doc("verifyEmail", "Verifies the users email and logs them in", tag) {
            queryParam<String>("token") { it.required = true }
            json<UserLoginData>("200")
        }
        handler { ctx ->
            handleUserLogin(ctx, UserService.verifyEmail(ctx.queryParam("token") ?: ""))
        }
    }

    private fun handleUserLogin(ctx: Context, userLogin: UserLogin?) =
        if (userLogin == null) {
            ctx.status(HttpCode.BAD_REQUEST)
        } else {
            ctx.json(userLogin)
        }

    private val forgotPassword = documentedHandler {
        doc("forgotPassword", "Sends an email to the user to reset their password", tag) {
            queryParam<String>("email") { it.required = true }
            result<Unit>("200")
        }
        handler { ctx ->
            UserService.forgotPassword(ctx.queryParam("email"))
        }
    }

    private val tokenLogin = documentedHandler {
        doc("tokenLogin", "Logs a user in with the specified token", tag) {
            queryParam<String>("token") { it.required = true }
            json<UserLoginData>("200")
        }
        handler { ctx ->
            handleUserLogin(ctx, UserService.tokenLogin(ctx.queryParam("token") ?: ""))
        }
    }

    data class LoginData(val email: String, val password: String)
    data class LoginResult(val userLocked: Boolean, val loginFailed: Boolean = true)

    private val login = documentedHandler {
        doc("login", "Logs a user in with the specified email and password", tag) {
            body<LoginData>()
            json<UserLoginData>("200")
            json<LoginResult>("400")
        }
        handler { ctx ->
            val (email, password) = ctx.bodyFromJson<LoginData>()
            val (user, userLogin) = UserService.login(email, password)

            if (userLogin != null) {
                ctx.json(userLogin)
            } else {
                ctx.status(HttpCode.BAD_REQUEST)
                ctx.json(LoginResult(user?.locked == true))
            }
        }
    }

    data class Signup(val email: String, val firstName: String? = null, val lastName: String? = null)

    private val signup = documentedHandler {
        doc("signup", "Sends an email validation to the user", tag) {
            body<Signup>()
            result<Unit>("200")
        }
        handler { ctx ->
            val (email, firstName, lastName) = ctx.bodyFromJson<Signup>()
            UserService.signup(email, firstName ?: "", lastName ?: "")
            ctx.status(HttpCode.OK)
        }
    }

    data class CurrentUserRoleData(@JsonIgnore private val organizationUser: OrganizationUser) {
        val organizationId = organizationUser.organizationId
        val organizationName = organizationUser.organization.name
        val role = organizationUser.role
    }

    data class CurrentUserData(@JsonIgnore private val user: User) {
        val id = user.id
        val createdAt = user.createdAt
        val updatedAt = user.updatedAt
        val firstName = user.firstName
        val lastName = user.lastName
        val email = user.email
        val passwordSet = user.passwordSet
        val roles = OrganizationUsers.findByUserId(user.id).preloadOrganizations().map { CurrentUserRoleData(it) }
    }

    private val currentUser = documentedHandler {
        doc("currentUser", "Loads the current user", tag) {
            json<CurrentUserData>("200")
        }
        handler { ctx ->
            when (val user = Users.findById(ctx.userIdOrNull)) {
                null -> ctx.status(HttpCode.BAD_REQUEST)
                else -> ctx.json(CurrentUserData(user))
            }
        }
    }

    private val logout = documentedHandler {
        doc("logout", "Logs the user out", tag) {
            result<Unit>("200")
        }
        handler { ctx ->
            ctx.userLoginOrNull?.invalidate()
            ctx.status(HttpCode.OK)
        }
    }

    data class PasswordData(val password: String)

    private val updatePassword = documentedHandler {
        doc("updatePassword", "Updates the user's password", tag) {
            body<PasswordData>()
            result<Unit>("200")
            jsonValidationErrors()
        }
        handler { ctx ->
            val (password) = ctx.bodyFromJson<PasswordData>()
            UserService.updatePassword(ctx.userId, password)
        }
    }

    private val acceptInvite = documentedHandler {
        doc("acceptInvite", "Accepts an invite to an organization with the given token", tag) {
            queryParam<String>("token") { it.required = true }
            json<UserLoginData>("200")
        }
        handler { ctx ->
            handleUserLogin(ctx, UserService.acceptInvite(ctx.userLoginOrNull, ctx.queryParam("token") ?: ""))
        }
    }

    private val updateUserInfo = documentedHandler {
        doc("updateUserInfo", "Updates the user's information", tag) {
            body<UserUpdate>()
            json<CurrentUserData>("200")
        }
        handler { ctx ->
            UserService.update(ctx.user, ctx.bodyFromJson())
            ctx.json(CurrentUserData(ctx.user))
        }
    }
}