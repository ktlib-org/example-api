package usecases.user

import domain.entities.user.User
import domain.entities.user.UserValidation
import domain.entities.user.UserValidations
import domain.entities.user.Users
import org.ktlib.email.Email
import org.ktlib.entities.Validation.validEmailDomain
import org.ktlib.entities.Validation.validateField
import org.ktlib.entities.transaction
import org.ktlib.urlEncode
import usecases.Role
import usecases.UseCase
import usecases.UseCaseConfig

class Signup : UseCase<Signup.Input, UserValidation?>(Role.Anyone) {
    data class Input(val email: String, val firstName: String = "", val lastName: String = "")

    override fun doExecute() = transaction {
        val (email, firstName, lastName) = input
        val user = Users.findByEmail(email)

        if (user == null) {
            validateField(User::email, email) { validEmailDomain() }

            val validation = UserValidations.createForEmailValidation(email, firstName, lastName)

            senEmailVerification(validation)

            validation
        } else {
            executeUseCase(ForgotPassword::class, ForgotPassword.Input(email))
        }
    }

    private fun senEmailVerification(validation: UserValidation) {
        val url = "${UseCaseConfig.webAppUrl}/?action=verifyEmail&token=${validation.token.urlEncode()}"
        Email.send(
            template = UseCaseConfig.emailVerificationTemplate,
            to = validation.toEmailData(),
            data = mapOf("url" to url)
        )
    }
}