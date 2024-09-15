package usecases.user

import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.UserValidations
import domain.entities.user.UserValidations.delete
import domain.entities.user.Users
import org.ktlib.entities.ValidationError
import org.ktlib.entities.ValidationErrors
import org.ktlib.entities.ValidationException
import org.ktlib.entities.transaction
import usecases.Role
import usecases.UseCase

class VerifyEmail : UseCase<VerifyEmail.Input, UserLogin>(Role.Anyone) {
    data class Input(val token: String)

    override fun doExecute() = transaction {
        UserValidations.findByToken(input.token).let { validation ->
            validation?.delete()

            if (validation?.isValid != true) {
                throw ValidationException(ValidationErrors(mutableListOf(ValidationError("token", "Invalid token."))))
            } else {
                val user =
                    Users.create(
                        email = validation.email,
                        firstName = validation.firstName,
                        lastName = validation.lastName
                    ) ?: Users.findByEmail(validation.email)!!

                UserLogins.create(user.id)
            }
        }
    }
}