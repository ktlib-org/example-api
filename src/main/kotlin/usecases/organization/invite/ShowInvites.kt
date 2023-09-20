package usecases.organization.invite

import entities.user.UserValidation
import entities.user.UserValidations
import usecases.Role
import usecases.UseCase

class ShowInvites : UseCase<Unit, List<UserValidation>>(Role.Admin) {
    override fun doExecute() = UserValidations.findByOrganization(orgId)
}

