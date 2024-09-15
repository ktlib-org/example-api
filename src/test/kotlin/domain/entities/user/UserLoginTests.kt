package domain.entities.user

import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.UserLogins.update
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.ktlib.test.EntitySpec

class UserLoginTests : EntitySpec({
    objectMocks(UserLogins)
    
    "invalidate" {
        every { any<UserLogin>().update() } returns UserLogin {}

        val userLogin = UserLogin {
            valid = true
        }

        userLogin.invalidate()

        userLogin.valid shouldBe false
        verify {
            userLogin.update()
        }
    }
})