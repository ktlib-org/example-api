package usecases.organization.invite

import entities.user.UserValidations
import entities.user.UserValidations.delete
import usecases.Role
import usecases.UseCase
import java.util.*

class RemoveInvite : UseCase<RemoveInvite.Input, Unit>(Role.Admin) {
    data class Input(val inviteId: UUID)

    override fun doExecute() {
        UserValidations.findByOrganizationIdAndId(orgId, input.inviteId)?.delete()
    }
}