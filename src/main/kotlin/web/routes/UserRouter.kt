package web.routes

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.preloadOrganizations
import entities.user.User
import entities.user.UserLogin
import entities.user.UserLoginData
import entities.user.Users
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.*
import org.ktapi.entities.ValidationError
import org.ktapi.web.Router
import usecases.user.*
import web.*
import web.ApiRole.Anyone
import web.ApiRole.UserNoOrg

object UserRouter : Router {
    override fun route() {
        path("/user") {
            get(this::currentUser, UserNoOrg)
            patch(this::updateUserInfo, UserNoOrg)
            post("/signup", this::signup, Anyone)
            post("/verify-email", this::verifyEmail, Anyone)
            post("/forgot-password", this::forgotPassword, Anyone)
            post("/token-login", this::tokenLogin, Anyone)
            post("/login", this::login, Anyone)
            post("/update-password", this::updatePassword, UserNoOrg)
            post("/logout", this::logout, UserNoOrg)
            post("/accept-invite", this::acceptInvite, Anyone)
        }
    }

    private const val tag = "User"

    @OpenApi(
        path = "/user/verify-email",
        methods = [HttpMethod.POST],
        operationId = "verifyEmail",
        summary = "Verifies the users email and logs them in",
        tags = [tag],
        queryParams = [OpenApiParam(name = "token", type = String::class, required = true)],
        responses = [
            OpenApiResponse("200", [OpenApiContent(UserLoginData::class)]),
            OpenApiResponse("400")
        ]
    )
    private fun verifyEmail(ctx: Context) {
        handleUserLogin(ctx, VerifyEmail.verifyEmail(ctx.queryParam("token") ?: ""))
    }

    private fun handleUserLogin(ctx: Context, userLogin: UserLogin?) =
        if (userLogin == null) {
            ctx.status(HttpStatus.BAD_REQUEST)
        } else {
            ctx.json(userLogin)
        }

    @OpenApi(
        path = "/user/forgot-password",
        methods = [HttpMethod.POST],
        operationId = "forgotPassword",
        summary = "Sends an email to the user to reset their password",
        tags = [tag],
        queryParams = [OpenApiParam(name = "email", type = String::class, required = true)],
        responses = [
            OpenApiResponse("200")
        ]
    )
    private fun forgotPassword(ctx: Context) {
        ForgotPassword.forgotPassword(ctx.queryParam("email"))
    }

    @OpenApi(
        path = "/user/token-login",
        methods = [HttpMethod.POST],
        operationId = "tokenLogin",
        summary = "Logs a user in with the specified token",
        tags = [tag],
        queryParams = [OpenApiParam(name = "token", type = String::class, required = true)],
        responses = [
            OpenApiResponse("200", [OpenApiContent(UserLoginData::class)]),
            OpenApiResponse("400")
        ]
    )
    private fun tokenLogin(ctx: Context) {
        handleUserLogin(ctx, TokenLogin.tokenLogin(ctx.queryParam("token") ?: ""))
    }

    data class LoginData(val email: String, val password: String)
    data class LoginResult(val userLocked: Boolean, val loginFailed: Boolean, val userLogin: UserLoginData?)

    @OpenApi(
        path = "/user/login",
        methods = [HttpMethod.POST],
        operationId = "login",
        summary = "Logs a user in with the specified email and password",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(LoginData::class)]),
        responses = [OpenApiResponse("200", [OpenApiContent(LoginResult::class)])]
    )
    private fun login(ctx: Context) {
        val (email, password) = ctx.bodyFromJson<LoginData>()
        val (user, userLogin) = Login.login(email, password)

        ctx.json(LoginResult(userLocked = user?.locked == true, loginFailed = userLogin == null, userLogin = userLogin))
    }

    data class Signup(val email: String, val firstName: String? = null, val lastName: String? = null)

    @OpenApi(
        path = "/user/signup",
        methods = [HttpMethod.POST],
        operationId = "signup",
        summary = "Sends an email validation to the user",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(Signup::class)]),
        responses = [OpenApiResponse("200")]
    )
    private fun signup(ctx: Context) {
        val (email, firstName, lastName) = ctx.bodyFromJson<Signup>()
        usecases.user.Signup.signup(email, firstName ?: "", lastName ?: "")
        ctx.status(HttpStatus.OK)
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

    @OpenApi(
        path = "/user",
        methods = [HttpMethod.GET],
        operationId = "currentUser",
        summary = "Loads the current user",
        tags = [tag],
        responses = [
            OpenApiResponse("200", [OpenApiContent(CurrentUserData::class)]),
            OpenApiResponse("400")
        ]
    )
    private fun currentUser(ctx: Context) {
        when (val user = Users.findById(ctx.userIdOrNull)) {
            null -> ctx.status(HttpStatus.BAD_REQUEST)
            else -> ctx.json(CurrentUserData(user))
        }
    }

    @OpenApi(
        path = "/user/logout",
        methods = [HttpMethod.POST],
        operationId = "logout",
        summary = "Logs the user out",
        tags = [tag],
        responses = [OpenApiResponse("200")]
    )
    private fun logout(ctx: Context) {
        ctx.userLoginOrNull?.invalidate()
        ctx.status(HttpStatus.OK)
    }

    data class PasswordData(val password: String)

    @OpenApi(
        path = "/user/update-password",
        methods = [HttpMethod.POST],
        operationId = "updatePassword",
        summary = "Updates the user's password",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(PasswordData::class)]),
        responses = [
            OpenApiResponse("200"),
            OpenApiResponse("400", [OpenApiContent(Array<ValidationError>::class)])
        ]
    )
    private fun updatePassword(ctx: Context) {
        val (password) = ctx.bodyFromJson<PasswordData>()
        UpdatePassword.updatePassword(ctx.userId, password)
        ctx.status(HttpStatus.OK)
    }

    @OpenApi(
        path = "/user/accept-invite",
        methods = [HttpMethod.POST],
        operationId = "acceptInvite",
        summary = "Accepts an invite to an organization with the given token",
        tags = [tag],
        queryParams = [OpenApiParam(name = "token", type = String::class, required = true)],
        responses = [
            OpenApiResponse("200", [OpenApiContent(UserLoginData::class)]),
            OpenApiResponse("400")
        ]
    )
    private fun acceptInvite(ctx: Context) {
        handleUserLogin(ctx, AcceptInvite.acceptInvite(ctx.userLoginOrNull, ctx.queryParam("token") ?: ""))
    }

    @OpenApi(
        path = "/user",
        methods = [HttpMethod.PATCH],
        operationId = "updateUserInfo",
        summary = "Updates the user's information",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(UpdateUser.UpdateUserData::class)]),
        responses = [
            OpenApiResponse("200", [OpenApiContent(CurrentUserData::class)]),
            OpenApiResponse("400", [OpenApiContent(Array<ValidationError>::class)])
        ]
    )
    private fun updateUserInfo(ctx: Context) {
        UpdateUser.update(ctx.user, ctx.bodyFromJson())
        ctx.json(CurrentUserData(ctx.user))
    }
}