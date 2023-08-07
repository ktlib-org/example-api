package usecases.user

import entities.user.UserValidations
import entities.user.Users
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.ktapi.test.DbStringSpec
import services.EmailService
import java.time.LocalDateTime

class VerifyEmailTests : DbStringSpec() {
    init {
        "verifyEmail with valid" {
            val validation = UserValidations.createForEmailValidation("my@email.com", "first", "last")

            val userLogin = VerifyEmail.verifyEmail(validation.token)!!

            val user = Users.findById(userLogin.userId)!!
            UserValidations.findById(validation.id) shouldBe null
            user.email shouldBe validation.email
            user.firstName shouldBe validation.firstName
            user.lastName shouldBe validation.lastName
        }

        "verify email does nothing when invalid" {
            var validation = UserValidations.createForEmailValidation("my@email.com")
            validation = UserValidations.setCreatedAt(validation.id, LocalDateTime.now().minusDays(5))

            val result = VerifyEmail.verifyEmail(validation.token)

            result shouldBe null
        }

        "verify email works with exising email" {
            val user = Users.create("another@email.com")!!
            val validation = UserValidations.createForEmailValidation(user.email)

            val result = VerifyEmail.verifyEmail(validation.token)!!

            result.userId shouldBe user.id
        }
    }

    override val objectMocks = listOf(EmailService)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        every {
            EmailService.sendForgotPassword(any())
            EmailService.sendEmailVerification(any())
            EmailService.sendUserInvite(any(), any(), any())
        } just Runs
    }
}
