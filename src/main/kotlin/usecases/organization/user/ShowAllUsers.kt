package usecases.organization.user

import entities.organization.OrganizationUserWithUser
import entities.organization.OrganizationUsers
import entities.organization.preloadUsers
import usecases.Role
import usecases.UseCase

class ShowAllUsers : UseCase<Unit, List<OrganizationUserWithUser>>(Role.Admin) {
    override fun doExecute() = OrganizationUsers.findByOrganizationId(orgId)
        .preloadUsers()
        .map { it.toOrganizationUserWithUser() }
}