package usecases.user

import domain.entities.user.User
import domain.entities.user.Users
import org.ktlib.Encryption
import org.ktlib.entities.Validation.lengthAtLeast
import org.ktlib.entities.Validation.validateField
import usecases.Role
import usecases.UseCase

class UpdatePassword : UseCase<UpdatePassword.Input, Unit>(Role.UserNoOrg) {
    data class Input(val password: String)

    override fun doExecute() {
        validateField(User::password, input.password) { lengthAtLeast(8) }
        Users.updatePassword(currentUserId, Encryption.hashPassword(input.password))
    }
}