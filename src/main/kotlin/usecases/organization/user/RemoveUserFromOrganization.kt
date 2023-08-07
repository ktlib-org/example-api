package usecases.organization.user

import entities.organization.OrganizationUsers
import entities.organization.UserRole

object RemoveUserFromOrganization {
    fun removeUser(orgId: Long, userId: Long, currentUserId: Long) {
        val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        if (currentUserRole?.canUpdateRole(orgUser, UserRole.User) == true) {
            orgUser?.delete()
        }
    }
}