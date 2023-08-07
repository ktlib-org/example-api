package usecases.user

import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.ktapi.entities.ValidationException
import org.ktapi.test.DbStringSpec

class UpdateUserTests : DbStringSpec() {
    init {
        "update user" {
            var user = Users.findById(1)!!

            UpdateUser.update(user, mapOf("firstName" to "newFirst", "lastName" to "newLast"))

            user = Users.findById(1)!!
            user.firstName shouldBe "newFirst"
            user.lastName shouldBe "newLast"
        }

        "update user doesn't work to invalid domain" {
            val user = Users.findById(1)!!

            shouldThrow<ValidationException> {
                UpdateUser.update(user, mapOf("email" to "something@else.com"))
            }
        }

        "update user works to valid domain" {
            val user = Users.findById(1)!!

            shouldThrow<ValidationException> {
                UpdateUser.update(user, mapOf("email" to "something@ktapi.org"))
            }
        }
    }
}
