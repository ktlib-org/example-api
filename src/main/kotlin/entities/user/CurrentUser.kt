package entities.user

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.preloadOrganizations

data class CurrentUserRole(private val organizationUser: OrganizationUser) {
    val organizationId = organizationUser.organizationId
    val organizationName = organizationUser.organization.name
    val role = organizationUser.role
}

data class CurrentUser(private val user: User) {
    val id = user.id
    val createdAt = user.createdAt
    val updatedAt = user.updatedAt
    val firstName = user.firstName
    val lastName = user.lastName
    val email = user.email
    val passwordSet = user.passwordSet
    val roles = OrganizationUsers.findByUserId(user.id).preloadOrganizations().map { CurrentUserRole(it) }
}