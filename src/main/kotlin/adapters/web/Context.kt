package adapters.web

import entities.user.UserLogin
import entities.user.UserLogins
import io.javalin.http.Context

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

val Context.organizationIdOrNull: String? get() = header("Organization")
