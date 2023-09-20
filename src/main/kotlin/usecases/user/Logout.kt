package usecases.user

import entities.user.UserLogins.delete
import usecases.Role
import usecases.UseCase

class Logout : UseCase<Unit, Unit>(Role.Anyone) {
    override fun doExecute() {
        currentUserLoginOrNull?.delete()
    }
}