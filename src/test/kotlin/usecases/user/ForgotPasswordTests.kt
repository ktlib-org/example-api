package usecases.user

import entities.user.Users
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.mockk.*
import org.ktapi.test.DbStringSpec
import services.EmailService

class ForgotPasswordTests : DbStringSpec() {
    init {
        "forgot password sends email" {
            var user = Users.findById(1)!!

            ForgotPassword.forgotPassword(user.email)

            user = Users.findById(1)!!
            user.passwordSet shouldBe false
            verify {
                EmailService.sendForgotPassword(any())
            }
        }

        "forgot password does nothing if email not found" {
            ForgotPassword.forgotPassword("fake@email.coms")

            verify {
                EmailService wasNot Called
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
