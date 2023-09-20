package usecases.user

import entities.user.User
import entities.user.Users
import entities.user.Users.update
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.verify
import org.ktlib.Encryption
import usecases.UseCaseContext
import usecases.UseCaseSpec

class LoginTests : UseCaseSpec() {
    private lateinit var user: User

    init {
        objectMocks(Encryption)

        "login" {
            every { Encryption.passwordMatches("test", user.password) } returns true

            val result = execute(user.email, "test")

            result.userLocked shouldBe false
            result.loginFailed shouldBe false
            result.userLogin shouldNotBe null
            Users.findById(user.id)?.passwordFailures shouldBe 0
        }

        "login failed" {
            every { Encryption.passwordMatches(any(), any()) } returns false

            val result = execute(user.email, "bad")

            result.loginFailed shouldBe true
            result.userLocked shouldBe false
            result.userLogin shouldBe null
        }

        "login causes locked account" {
            every { Encryption.passwordMatches(any(), any()) } returns false
            user.apply { passwordFailures = 2 }.update()

            val result = execute(user.email, "bad")

            result.loginFailed shouldBe true
            result.userLocked shouldBe true
            result.userLogin shouldBe null
        }

        "should not attempt login if user already locked" {
            every { Encryption.passwordMatches(any(), any()) } returns false
            user.apply { locked = true }.update()

            val result = execute(user.email, "bad")

            result.loginFailed shouldBe true
            result.userLocked shouldBe true
            result.userLogin shouldBe null
            verify(exactly = 0) { Encryption.passwordMatches(any(), any()) }
        }

        beforeEach {
            user = Users.create("myuser@email.com", "testPassword")!!
        }
    }

    private fun execute(email: String, password: String) =
        useCase(Login::class, UseCaseContext(input = Login.Input(email, password))).execute()
}
