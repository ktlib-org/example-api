package usecases.organization

import entities.organization.Organization
import entities.organization.Organizations
import usecases.Role
import usecases.UseCase

class ShowOrganization : UseCase<Unit, Organization>(Role.User) {
    override fun doExecute() = Organizations.findById(orgId)!!
}