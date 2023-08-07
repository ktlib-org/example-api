package entities.user

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.ktapi.test.DbStringSpec

class UserLoginTests : DbStringSpec({
    "create" {
        val user = Users.findById(1)!!

        val userLogin = UserLogins.create(user.id)

        userLogin.token shouldNotBe null
        userLogin.userId shouldBe 1
        userLogin.valid shouldBe true
    }

    "invalidate" {
        val user = Users.findById(1)!!
        val userLogin = UserLogins.create(user.id)

        userLogin.invalidate()

        UserLogins.findById(userLogin.id)!!.valid shouldBe false
    }
})