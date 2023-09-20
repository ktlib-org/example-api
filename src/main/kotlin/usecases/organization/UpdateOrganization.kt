package usecases.organization

import entities.organization.Organization
import entities.organization.Organizations
import entities.organization.Organizations.update
import org.ktlib.entities.populateFrom
import usecases.DataMap
import usecases.Role
import usecases.UseCase

class UpdateOrganization : UseCase<DataMap, Organization>(Role.Admin) {
    data class UpdateOrganizationData(val name: String? = null)

    override fun doExecute(): Organization {
        val org = Organizations.findById(orgId)!!
        org.populateFrom(input, UpdateOrganizationData::class).validate().update()
        return org
    }
}