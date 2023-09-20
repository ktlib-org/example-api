package usecases.user

import entities.user.UserValidations
import io.kotest.matchers.shouldBe
import usecases.UseCaseSpec

class TokenLoginTests : UseCaseSpec() {
    init {
        "token login" {
            val validation = UserValidations.createForForgotPassword(currentUser)

            val result = execute(validation.token)

            result?.userId shouldBe currentUserId
            UserValidations.findById(validation.id) shouldBe null
        }
    }

    private fun execute(token: String) = useCase(TokenLogin::class, TokenLogin.Input(token)).execute()
}
