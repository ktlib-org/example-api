package usecases.user

import entities.user.UserValidation
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.ktlib.email.Email
import usecases.UseCase
import usecases.UseCaseConfig
import usecases.UseCaseSpec

class SignupTests : UseCaseSpec() {
    private val forgotPassword = mockk<ForgotPassword>()

    init {
        objectMocks(Email, UseCase)

        "signup" {
            val validation = execute("anew@test.com")

            validation shouldNotBe null
            verify { Email.send(UseCaseConfig.emailVerificationTemplate, any(), any(), any(), any(), any()) }
        }

        "signup with exising email calls forgot password" {
            execute(currentUser.email)

            verify { forgotPassword.execute() }
        }

        beforeEach {
            every { UseCase.create(ForgotPassword::class, any()) } returns forgotPassword
            every { Email.send(any(), any(), any(), any(), any(), any()) } just Runs
            every { forgotPassword.execute() } returns UserValidation {}
        }
    }

    private fun execute(email: String) = useCase(Signup::class, Signup.Input(email)).execute()
}
