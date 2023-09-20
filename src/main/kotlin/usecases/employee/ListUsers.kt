package usecases.employee

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.OrganizationUser
import entities.user.User
import entities.user.Users
import entities.user.preloadRoles

class ListUsers : EmployeeUseCase<Unit, List<ListUsers.UserDataAll>>() {
    data class UserRoleData(@JsonIgnore private val orgUser: OrganizationUser) {
        val organizationId = orgUser.organizationId
        val userId = orgUser.userId
        val role = orgUser.role
        val createdAt = orgUser.createdAt
        val updatedAt = orgUser.updatedAt
    }

    data class UserDataAll(@JsonIgnore private val user: User) {
        val id = user.id
        val firstName = user.firstName
        val lastName = user.lastName
        val email = user.email
        val employee = user.employee
        val passwordSet = user.passwordSet
        val enabled = user.enabled
        val locked = user.locked
        val passwordFailures = user.passwordFailures
        val roles = user.roles.map { UserRoleData(it) }
    }

    override fun doExecute() = Users.all().preloadRoles().map { UserDataAll(it) }
}