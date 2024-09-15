package usecases.organization.invite

import domain.entities.organization.UserRole
import domain.entities.user.UserValidations
import io.kotest.matchers.shouldBe
import usecases.UseCaseSpec
import java.util.*

class RemoveInviteTests : UseCaseSpec() {
    init {
        "remove invite" {
            val validation = UserValidations.createForInvite(testOrgId, UserRole.User, currentUser)

            execute(validation.id)

            UserValidations.findById(validation.id) shouldBe null
        }
    }

    private fun execute(inviteId: UUID) =
        useCase(RemoveInvite::class, RemoveInvite.Input(inviteId)).execute()
}