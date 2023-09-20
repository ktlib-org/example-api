package usecases.user

import entities.user.CurrentUser
import entities.user.Users.update
import org.ktlib.entities.populateFrom
import usecases.DataMap
import usecases.Role
import usecases.UseCase

class UpdateUser : UseCase<DataMap, CurrentUser>(Role.UserNoOrg) {
    data class UpdateUserData(val firstName: String? = null, val lastName: String? = null, val email: String?)

    override fun doExecute(): CurrentUser {
        currentUser.populateFrom(input, UpdateUserData::class).validate()
        return CurrentUser(currentUser.update())
    }
}