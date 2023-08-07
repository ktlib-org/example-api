package usecases.organization.invite

import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.User
import entities.user.UserValidations
import org.ktapi.db.transaction
import org.ktapi.entities.Validation
import services.EmailService

object InviteUser {
    fun inviteUser(
        orgId: Long,
        role: UserRole,
        invitingUser: User,
        email: String,
        firstName: String = "",
        lastName: String = ""
    ) = transaction {
        Validation.validateField("email", email) { Validation.validEmailDomain() }
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
}