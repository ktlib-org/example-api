package usecases.user

import entities.user.UserLogin
import entities.user.UserLogins
import entities.user.UserValidations

object TokenLogin {
    fun tokenLogin(token: String): UserLogin? {
        val validation = UserValidations.findByToken(token) ?: return null

        validation.delete()

        if (!validation.isValid || validation.userId == null) return null

        return UserLogins.create(validation.userId!!)
    }
}