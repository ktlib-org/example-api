package usecases.employee

import com.fasterxml.jackson.annotation.JsonIgnore
import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.preloadUsers

class ListUserLogins : EmployeeUseCase<Unit, List<ListUserLogins.UserLoginData>>() {
    data class UserLoginData(@JsonIgnore private val userLogin: UserLogin) {
        val id = userLogin.id
        val userId = userLogin.userId
        val parentId = userLogin.parentId
        val valid = userLogin.valid
        val firstName = userLogin.user.firstName
        val lastName = userLogin.user.lastName
        val email = userLogin.user.email
        val employee = userLogin.user.employee
    }

    override fun doExecute() = UserLogins.findRecent().preloadUsers().map { UserLoginData(it) }
}