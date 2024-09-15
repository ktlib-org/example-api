package usecases.organization.user

import domain.entities.organization.OrganizationUserWithUser
import domain.entities.organization.OrganizationUsers
import domain.entities.organization.preloadUsers
import usecases.Role
import usecases.UseCase

class ShowAllUsers : UseCase<Unit, List<OrganizationUserWithUser>>(Role.Admin) {
    override fun doExecute() = OrganizationUsers.findByOrganizationId(orgId)
        .preloadUsers()
        .map { it.toOrganizationUserWithUser() }
}