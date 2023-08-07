package usecases.user

import entities.user.UserValidations
import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.ktapi.test.DbStringSpec

class TokenLoginTests : DbStringSpec() {
    init {
        "token login" {
            val validation = UserValidations.createForForgotPassword(Users.findById(1)!!)

            val result = TokenLogin.tokenLogin(validation.token)

            result shouldNotBe null
            UserValidations.findById(validation.id) shouldBe null
        }
    }
}
