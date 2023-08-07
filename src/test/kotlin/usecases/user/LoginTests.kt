package usecases.user

import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.ktapi.test.DbStringSpec

class LoginTests : DbStringSpec() {
    init {
        "login" {
            var testUser = Users.findById(1)!!
            testUser.passwordFailure()

            val result = Login.login(testUser.email, "test")

            testUser = Users.findById(testUser.id)!!
            result.first shouldNotBe null
            result.second shouldNotBe null
            testUser.passwordFailures shouldBe 0
        }

        "login failed" {
            val testUser = Users.findById(1)!!

            val result = Login.login(testUser.email, "bad")

            result.first shouldNotBe null
            result.first?.locked shouldBe false
            result.second shouldBe null
        }

        "login causes locked account" {
            val testUser = Users.findById(1)!!

            Login.login(testUser.email, "bad")
            Login.login(testUser.email, "bad")
            val result = Login.login(testUser.email, "bad")

            result.first?.locked shouldBe true
        }
    }
}
