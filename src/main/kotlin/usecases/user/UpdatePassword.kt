package usecases.user

import entities.user.Users
import org.ktapi.entities.Validation.lengthAtLeast
import org.ktapi.entities.Validation.validateField

object UpdatePassword {
    fun updatePassword(userId: Long, password: String) {
        validateField("password", password) { lengthAtLeast(8) }
        Users.updatePassword(userId, password)
    }
}