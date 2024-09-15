package usecases.user

import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.UserValidations
import domain.entities.user.UserValidations.delete
import org.ktlib.entities.ValidationError
import org.ktlib.entities.ValidationErrors
import org.ktlib.entities.ValidationException
import org.ktlib.entities.transaction
import usecases.Role
import usecases.UseCase

class TokenLogin : UseCase<TokenLogin.Input, UserLogin?>(Role.Anyone) {
    data class Input(val token: String)

    override fun doExecute() = transaction {
        UserValidations.findByToken(input.token).let { validation ->
            validation?.delete()

            if (validation?.isValid != true || validation.userId == null) {
                throw ValidationException(ValidationErrors(mutableListOf(ValidationError("token", "Invalid token."))))
            }

            UserLogins.create(validation.userId!!)
        }
    }
}