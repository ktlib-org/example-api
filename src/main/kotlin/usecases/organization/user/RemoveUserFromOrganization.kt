package usecases.organization.user

import entities.organization.OrganizationUsers
import entities.organization.OrganizationUsers.delete
import entities.organization.UserRole
import usecases.Role
import usecases.UseCase
import java.util.*

class RemoveUserFromOrganization : UseCase<RemoveUserFromOrganization.Input, Unit>(Role.Admin) {
    data class Input(val userId: UUID)

    override fun doExecute() {
        val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(input.userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        if (currentUserRole?.canUpdateRole(orgUser, UserRole.User) == true) {
            orgUser?.delete()
        }
    }
}