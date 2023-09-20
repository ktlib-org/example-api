package usecases.user

import entities.user.User
import entities.user.Users
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.ktlib.email.Email
import usecases.UseCaseSpec

class ForgotPasswordTests : UseCaseSpec() {
    private lateinit var user: User

    init {
        objectMocks(Email)

        "forgot password sends email" {
            val result = execute(user.email)

            val userAfter = Users.findById(user.id)!!
            result?.userId shouldBe userAfter.id
            userAfter.passwordSet shouldBe false
            verify { Email.send(any(), any(), any(), any()) }
        }

        "forgot password does nothing if email not found" {
            execute("fake@email.com")

            verify {
                Email wasNot Called
            }
        }

        beforeEach {
            user = Users.create("MyEmail", "password")!!

            every { Email.send(any(), any(), any(), any()) } just Runs
        }
    }

    private fun execute(email: String) =
        useCase(ForgotPassword::class, ForgotPassword.Input(email)).execute()
}
