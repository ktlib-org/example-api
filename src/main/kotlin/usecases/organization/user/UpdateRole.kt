package usecases.organization.user

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.UserRole
import usecases.Role
import usecases.UseCase

class UpdateRole : UseCase<UpdateRole.Input, OrganizationUser?>(Role.Admin) {
    data class Input(val userId: String, val role: UserRole)

    override fun doExecute(): OrganizationUser? {
        val currentRole = OrganizationUsers.findByUserIdAndOrganizationId(input.userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        return if (currentUserRole?.canUpdateRole(currentRole, input.role) == true) {
            OrganizationUsers.updateRole(currentRole!!.id, input.role)
            OrganizationUsers.findById(currentRole.id)
        } else {
            currentRole
        }
    }
}