package usecases.organization.invite

import domain.entities.user.UserValidation
import domain.entities.user.UserValidations
import usecases.Role
import usecases.UseCase

class ShowInvites : UseCase<Unit, List<UserValidation>>(Role.Admin) {
    override fun doExecute() = UserValidations.findByOrganization(orgId)
}

