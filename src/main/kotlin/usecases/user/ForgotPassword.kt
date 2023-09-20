package usecases.user

import entities.user.User
import entities.user.UserValidation
import entities.user.UserValidations
import entities.user.Users
import entities.user.Users.update
import org.ktlib.email.Email
import org.ktlib.entities.transaction
import org.ktlib.urlEncode
import usecases.Role
import usecases.UseCase
import usecases.UseCaseConfig
import usecases.UseCaseConfig.webAppUrl

class ForgotPassword : UseCase<ForgotPassword.Input, UserValidation?>(Role.Anyone) {
    data class Input(val email: String)

    override fun doExecute() = Users.findByEmail(input.email)?.let { executeForUser(it) }

    private fun executeForUser(user: User) = transaction {
        user.passwordSet = false
        user.update()

        UserValidations.createForForgotPassword(user).sendForgotPassword()
    }

    private fun UserValidation.sendForgotPassword(): UserValidation {
        val url = "${webAppUrl}/?action=resetPassword&token=${token.urlEncode()}"
        Email.send(
            template = UseCaseConfig.forgotPasswordTemplate,
            to = toEmailData(),
            data = mapOf("url" to url)
        )
        return this
    }
}
