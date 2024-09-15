package usecases.organization

import domain.entities.organization.Organization
import domain.entities.organization.Organizations
import usecases.Role
import usecases.UseCase

class ShowOrganization : UseCase<Unit, Organization>(Role.User) {
    override fun doExecute() = Organizations.findById(orgId)!!
}