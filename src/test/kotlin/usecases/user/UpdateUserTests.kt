package usecases.user

import entities.user.Users
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.ktlib.entities.ValidationException
import usecases.UseCaseSpec

class UpdateUserTests : UseCaseSpec() {
    init {
        "update user" {
            execute(mapOf("firstName" to "newFirst", "lastName" to "newLast"))

            val user = Users.findById(currentUserId)!!
            user.firstName shouldBe "newFirst"
            user.lastName shouldBe "newLast"
        }

        "update user doesn't work to invalid domain" {
            shouldThrow<ValidationException> {
                execute(mapOf("email" to "something@else.com"))
            }
        }

        "update user works to valid domain" {
            execute(mapOf("email" to "something@test.com"))
        }
    }

    private fun execute(data: Map<String, Any?>) = useCase(UpdateUser::class, data).execute()
}
