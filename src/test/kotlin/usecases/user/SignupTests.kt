package usecases.user

import entities.user.Users
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.ktapi.test.DbStringSpec
import services.EmailService

class SignupTests : DbStringSpec() {
    init {
        "signup" {
            val validation = Signup.signup("anew@ktapi.org")

            validation.email shouldBe "anew@ktapi.org"
            verify {
                EmailService.sendEmailVerification(validation)
            }
        }

        "signup with exising email sends forgot password" {
            val testUser = Users.findById(1)!!

            val validation = Signup.signup(testUser.email)

            validation.userId shouldBe testUser.id
            verify {
                EmailService.sendForgotPassword(validation)
            }
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
