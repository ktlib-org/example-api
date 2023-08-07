package usecases.user

import entities.user.UserLogins
import entities.user.UserValidations
import entities.user.Users
import org.ktapi.db.transaction

object VerifyEmail {
    fun verifyEmail(token: String) = transaction {
        UserValidations.findByToken(token)?.let { validation ->
            validation.delete()

            if (!validation.isValid) {
                null
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