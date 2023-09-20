package usecases.user

import entities.user.UserLogin
import entities.user.UserLogins
import entities.user.UserValidations
import entities.user.UserValidations.delete
import entities.user.Users
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