package usecases.user

import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.ktapi.Encryption
import org.ktapi.entities.ValidationException
import org.ktapi.test.DbStringSpec

class UpdatePasswordTests : DbStringSpec() {
    init {
        "updatePassword" {
            UpdatePassword.updatePassword(1, "myNewPasswordHere")

            val user = Users.findById(1)!!
            Encryption.passwordMatches("myNewPasswordHere", user.password) shouldBe true
        }

        "updatePassword with too short of password throws exception" {
            val exception = shouldThrow<ValidationException> {
                UpdatePassword.updatePassword(1, "short")
            }

            exception.validationErrors.size shouldBe 1
            exception.validationErrors.errors.first().field shouldBe "password"
            exception.validationErrors.errors.first().message shouldBe "password must be at least 8 characters in length"
        }
    }
}
