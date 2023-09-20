package usecases.user

import entities.user.UserLogin
import entities.user.UserValidation
import entities.user.UserValidations
import entities.user.UserValidations.update
import entities.user.Users
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.ktlib.entities.ValidationException
import org.ktlib.now
import usecases.UseCaseSpec

class VerifyEmailTests : UseCaseSpec() {
    private lateinit var validation: UserValidation
    private lateinit var newUserLogin: UserLogin

    init {
        "verifyEmail with valid" {
            val newUser = Users.create("anew@email.com")!!
            val validation = UserValidations.createForForgotPassword(newUser)

            val userLogin = execute(validation.token)

            userLogin.userId shouldBe newUser.id
            UserValidations.findById(validation.id) shouldBe null
        }

        "verify email throws exception when invalid" {
            val validation =
                UserValidations.createForForgotPassword(currentUser).apply { createdAt = now().minusDays(5) }.update()

            shouldThrow<ValidationException> {
                execute(validation.token)
            }
        }

        "verify email works with exising email" {
            val validation = UserValidations.createForForgotPassword(currentUser)

            val userLogin = execute(validation.token)

            userLogin.userId shouldBe currentUserId
            UserValidations.findById(validation.id) shouldBe null
        }
    }

    private fun execute(token: String) = useCase(VerifyEmail::class, VerifyEmail.Input(token)).execute()
}
