package web

import entities.user.User
import entities.user.UserLogin
import entities.user.UserLogins
import io.javalin.http.Context
import org.ktapi.fromJson

val Context.userLoginOrNull: UserLogin?
    get() {
        var login = attribute<UserLogin>("userLogin")

        if (login == null) {
            val token = header("Authorization")?.substringAfter("Bearer ");
            login = UserLogins.findByToken(token)
            attribute("userLogin", login)
            attribute("userLoginId", login?.id)
        }

        return login
    }

val Context.userLogin: UserLogin
    get() = userLoginOrNull!!

val Context.userIdOrNull: Long?
    get() = userLoginOrNull?.userId

val Context.userId: Long
    get() = userLogin.userId

val Context.userOrNull: User?
    get() = userLoginOrNull?.user

val Context.user: User
    get() = userLogin.user

val Context.organizationIdOrNull: Long?
    get() = header("Organization")?.toLongOrNull()

val Context.organizationId: Long
    get() = header("Organization")!!.toLong()

inline fun <reified T> Context.bodyFromJson() = bodyAsBytes().fromJson<T>()
