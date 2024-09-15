package usecases.organization.user

import domain.entities.organization.OrganizationUser
import domain.entities.organization.OrganizationUsers
import domain.entities.organization.UserRole
import usecases.Role
import usecases.UseCase
import java.util.*

class UpdateRole : UseCase<UpdateRole.Input, OrganizationUser?>(Role.Admin) {
    data class Input(val userId: UUID, val role: UserRole)

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