package usecases.user

import entities.user.User
import entities.user.UserLogin
import entities.user.UserLogins
import entities.user.Users
import org.ktapi.Encryption

object Login {
    fun login(email: String, password: String): Pair<User?, UserLogin?> {
        val user = Users.findByEmail(email) ?: return Pair(null, null)

        return when {
            user.locked -> Pair(user, null)
            Encryption.passwordMatches(password, user.password) -> {
                user.clearPasswordFailures()
                Pair(user, UserLogins.create(user.id))
            }

            else -> {
                user.passwordFailure()
                Pair(user, null)
            }
        }
    }
}