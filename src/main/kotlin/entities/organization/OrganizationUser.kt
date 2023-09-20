package entities.organization

import entities.DataWithDates
import entities.EntityWithOrganization
import entities.EntityWithOrganizationStore
import entities.user.User
import entities.user.Users
import org.ktlib.entities.Factory
import org.ktlib.entities.lazyAssociation
import org.ktlib.entities.preloadLazyAssociation
import org.ktlib.lookup

enum class UserRole {
    User, Admin, Owner;
}

data class OrganizationUserWithUser(private val orgUser: OrganizationUser) : DataWithDates(orgUser) {
    val role = orgUser.role
    val user = orgUser.user
}

interface OrganizationUser : EntityWithOrganization {
    companion object : Factory<OrganizationUser>()

    val userId: String
    val role: UserRole

    val user: User get() = lazyAssociation(::user) { Users.findById(userId)!! }

    fun toOrganizationUserWithUser() = OrganizationUserWithUser(this)

    fun canUpdateRole(orgUser: OrganizationUser?, newRole: UserRole) =
        orgUser != null
                && (orgUser.role != UserRole.Owner || !OrganizationUsers.hasOneOwner(orgUser.organizationId))
                && role >= orgUser.role && role >= newRole

    fun hasPermission(acceptableRoles: List<UserRole>) = acceptableRoles.any { role >= it }

    val hasAdminPrivileges: Boolean get() = role >= UserRole.Admin
}

fun List<OrganizationUser>.preloadUsers() = preloadLazyAssociation(
    OrganizationUser::user,
    { Users.findByIds(map { it.userId }) },
    { one, many -> many.find { one.userId == it.id }!! }
)

object OrganizationUsers : OrganizationUserStore by lookup()

interface OrganizationUserStore : EntityWithOrganizationStore<OrganizationUser> {
    fun create(organizationId: String, userId: String, role: UserRole): OrganizationUser
    fun updateRole(id: String, role: UserRole): Int
    fun findByUserId(userId: String): List<OrganizationUser>
    fun findByUserIds(userIds: List<String>): List<OrganizationUser>
    fun findByUserIdAndOrganizationId(userId: String, organizationId: String): OrganizationUser?
    fun userBelongsToOrganization(userId: String, organizationId: String): Boolean
    fun hasOneOwner(organizationId: String): Boolean
}
