package usecases.user

import domain.entities.user.User
import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.Users
import org.ktlib.Encryption
import usecases.Role
import usecases.UseCase

class Login : UseCase<Login.Input, Login.Output>(Role.Anyone) {
    data class Input(val email: String, val password: String)
    data class Output(val userLogin: UserLogin? = null, private val user: User? = null) {
        val userLocked: Boolean = user?.locked == true
        val loginFailed: Boolean = userLogin == null
    }

    override fun doExecute(): Output {
        val user = Users.findByEmail(input.email) ?: return Output()

        return when {
            user.locked -> Output(user = user)
            Encryption.passwordMatches(input.password, user.password) -> {
                user.clearPasswordFailures()
                Output(userLogin = UserLogins.create(user.id))
            }

            else -> {
                user.passwordFailure()
                Output(user = user)
            }
        }
    }
}
