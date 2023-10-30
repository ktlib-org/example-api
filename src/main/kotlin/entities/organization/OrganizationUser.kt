package entities.organization

import entities.DataWithDates
import entities.EntityWithOrganization
import entities.EntityWithOrganizationStore
import entities.user.User
import entities.user.Users
import org.ktlib.entities.Factory
import org.ktlib.entities.lazyValue
import org.ktlib.entities.preloadLazyValue
import org.ktlib.lookup
import java.util.*

enum class UserRole {
    User, Admin, Owner;
}

data class OrganizationUserWithUser(private val orgUser: OrganizationUser) : DataWithDates(orgUser) {
    val role = orgUser.role
    val user = orgUser.user
}

interface OrganizationUser : EntityWithOrganization {
    companion object : Factory<OrganizationUser>()

    val userId: UUID
    val role: UserRole

    val user: User get() = lazyValue(::user) { Users.findById(userId)!! }

    fun toOrganizationUserWithUser() = OrganizationUserWithUser(this)

    fun canUpdateRole(orgUser: OrganizationUser?, newRole: UserRole) =
        orgUser != null
                && (orgUser.role != UserRole.Owner || !OrganizationUsers.hasOneOwner(orgUser.organizationId))
                && role >= orgUser.role && role >= newRole

    fun hasPermission(acceptableRoles: List<UserRole>) = acceptableRoles.any { role >= it }

    val hasAdminPrivileges: Boolean get() = role >= UserRole.Admin
}

fun List<OrganizationUser>.preloadUsers() = preloadLazyValue(
    OrganizationUser::user,
    { Users.findByIds(map { it.userId }) },
    { one, many -> many.find { one.userId == it.id }!! }
)

object OrganizationUsers : OrganizationUserStore by lookup()

interface OrganizationUserStore : EntityWithOrganizationStore<OrganizationUser> {
    fun create(organizationId: UUID, userId: UUID, role: UserRole): OrganizationUser
    fun updateRole(id: UUID, role: UserRole): Int
    fun findByUserId(userId: UUID): List<OrganizationUser>
    fun findByUserIds(userIds: List<UUID>): List<OrganizationUser>
    fun findByUserIdAndOrganizationId(userId: UUID, organizationId: UUID): OrganizationUser?
    fun userBelongsToOrganization(userId: UUID, organizationId: UUID): Boolean
    fun hasOneOwner(organizationId: UUID): Boolean
}
