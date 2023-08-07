package usecases.user

import entities.user.User
import entities.user.UserValidations
import entities.user.Users
import org.ktapi.db.transaction
import services.EmailService

object ForgotPassword {
    fun forgotPassword(email: String?) = Users.findByEmail(email)?.let { forgotPassword(it) }

    fun forgotPassword(user: User) = transaction {
        user.passwordSet = false
        user.flushChanges()

        val validation = UserValidations.createForForgotPassword(user)
        EmailService.sendForgotPassword(validation)

        validation
    }
}