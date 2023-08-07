package usecases.organization

import entities.organization.Organization
import entities.organization.Organizations
import org.ktapi.entities.populateFrom

object UpdateOrganization {
    data class UpdateOrganizationData(val name: String? = null)

    fun update(orgId: Long, data: Map<String, Any>): Organization {
        val org = Organizations.findById(orgId)!!
        org.populateFrom(data, UpdateOrganizationData::class).validate().flushChanges()
        return org
    }
}