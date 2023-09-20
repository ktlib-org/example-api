package usecases.organization.invite

import entities.user.UserValidations
import entities.user.UserValidations.delete
import usecases.Role
import usecases.UseCase

class RemoveInvite : UseCase<RemoveInvite.Input, Unit>(Role.Admin) {
    data class Input(val inviteId: String)

    override fun doExecute() {
        UserValidations.findByOrganizationIdAndId(orgId, input.inviteId)?.delete()
    }
}