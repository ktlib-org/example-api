package usecases.employee

import domain.entities.organization.Organization
import domain.entities.organization.Organizations

class ListOrganizations : EmployeeUseCase<Unit, List<Organization>>() {
    override fun doExecute() = Organizations.all()
}