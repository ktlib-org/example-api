package usecases.organization.user

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.UserRole

object UpdateRole {
    fun updateRole(orgId: Long, userId: Long, role: UserRole, currentUserId: Long): OrganizationUser? {
        val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        if (currentUserRole?.canUpdateRole(orgUser, role) == true) {
            OrganizationUsers.updateRole(orgUser!!.id, role)
        }

        return OrganizationUsers.findById(orgUser?.id)
    }
}