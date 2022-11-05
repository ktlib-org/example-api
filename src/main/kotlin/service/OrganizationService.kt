package service

import model.*
import model.Organizations.create
import model.user.User
import model.user.UserValidations
import model.user.Users
import org.ktapi.db.transaction
import org.ktapi.model.Validation.validEmailDomain
import org.ktapi.model.Validation.validateField
import org.ktapi.model.populateFrom
import org.ktapi.slack.Slack
import org.ktapi.slack.WebhookMessage

object OrganizationService {
    data class OrganizationCreate(val name: String)

    fun create(data: OrganizationCreate, adminUserId: Long) = transaction {
        val org = Organization {}.populateFrom(data).validate().create()
        val user = Users.findById(adminUserId)!!
        OrganizationUsers.create(org.id, user.id, UserRole.Owner)
        Slack.sendMessage(WebhookMessage("Organization created named ${org.name} for user ${user.email}"))
        org
    }

    data class OrganizationUpdate(val name: String? = null)

    fun update(orgId: Long, data: Map<String, Any>): Organization {
        val org = Organizations.findById(orgId)!!
        org.populateFrom(data, OrganizationUpdate::class).validate().flushChanges()
        return org
    }

    fun inviteUser(
        orgId: Long,
        role: UserRole,
        invitingUser: User,
        email: String,
        firstName: String = "",
        lastName: String = ""
    ) = transaction {
        validateField("email", email) { validEmailDomain() }
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(invitingUser.id, orgId)

        if (currentUserRole != null && currentUserRole.role >= role) {
            val org = Organizations.findById(orgId)!!
            val validation = UserValidations.createForInvite(org.id, role, email, firstName, lastName)

            EmailService.sendUserInvite(org, invitingUser, validation)

            validation
        } else {
            throw Exception("User does not have permission to assign this role")
        }
    }

    fun removeInvite(orgId: Long, inviteId: Long) {
        UserValidations.findByOrganizationIdAndId(orgId, inviteId)?.delete()
    }

    fun removeUser(orgId: Long, userId: Long, currentUserId: Long) {
        val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        if (canUpdateRole(currentUserRole, orgUser, UserRole.User)) {
            orgUser?.delete()
        }
    }

    fun updateRole(orgId: Long, userId: Long, role: UserRole, currentUserId: Long) {
        val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(userId, orgId)
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, orgId)

        if (canUpdateRole(currentUserRole, orgUser, role)) {
            OrganizationUsers.updateRole(orgUser!!.id, role)
        }
    }

    private fun canUpdateRole(currentUser: OrganizationUser?, orgUser: OrganizationUser?, newRole: UserRole): Boolean {
        if (orgUser == null || currentUser == null) return false

        if (orgUser.role == UserRole.Owner && OrganizationUsers.hasOneOwner(orgUser.organizationId)) {
            return false
        }

        return currentUser.role >= orgUser.role && currentUser.role >= newRole
    }
}
