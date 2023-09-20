package usecases.user

import entities.user.Users
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.ktlib.Encryption
import org.ktlib.entities.ValidationException
import usecases.UseCaseSpec

class UpdatePasswordTests : UseCaseSpec() {

    init {
        "updatePassword" {
            execute("myNewPasswordHere")

            Encryption.passwordMatches("myNewPasswordHere", Users.findById(currentUserId)!!.password) shouldBe true
        }

        "updatePassword with too short of password throws exception" {
            val exception = shouldThrow<ValidationException> {
                execute("short")
            }

            exception.validationErrors.size shouldBe 1
            exception.validationErrors.errors.first().field shouldBe "password"
        }
    }

    private fun execute(password: String) =
        useCase(UpdatePassword::class, UpdatePassword.Input(password)).execute()
}
