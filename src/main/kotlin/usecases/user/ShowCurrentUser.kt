package usecases.user

import domain.entities.user.CurrentUser
import usecases.Role
import usecases.UseCase

class ShowCurrentUser : UseCase<Unit, CurrentUser?>(Role.UserNoOrg) {
    override fun doExecute() = CurrentUser(currentUser)
}