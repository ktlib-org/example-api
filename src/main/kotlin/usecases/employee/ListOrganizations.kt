package usecases.employee

import entities.organization.Organization
import entities.organization.Organizations

class ListOrganizations : EmployeeUseCase<Unit, List<Organization>>() {
    override fun doExecute() = Organizations.all()
}