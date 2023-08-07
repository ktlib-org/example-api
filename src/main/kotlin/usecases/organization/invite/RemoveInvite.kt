package usecases.organization.invite

import entities.user.UserValidations

object RemoveInvite {
    fun removeInvite(orgId: Long, inviteId: Long) {
        UserValidations.findByOrganizationIdAndId(orgId, inviteId)?.delete()
    }
}