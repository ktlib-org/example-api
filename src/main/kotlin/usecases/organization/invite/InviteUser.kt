package usecases.organization.invite

import domain.entities.organization.Organization
import domain.entities.organization.OrganizationUsers
import domain.entities.organization.Organizations
import domain.entities.organization.UserRole
import domain.entities.user.User
import domain.entities.user.UserValidation
import domain.entities.user.UserValidations
import org.ktlib.email.Email
import org.ktlib.entities.transaction
import org.ktlib.urlEncode
import usecases.Role
import usecases.UseCase
import usecases.UseCaseConfig
import usecases.UseCaseConfig.webAppUrl

class InviteUser : UseCase<InviteUser.Input, UserValidation>(Role.Admin) {
    data class Input(
        val role: UserRole,
        val email: String,
        val firstName: String = "",
        val lastName: String = ""
    )

    override fun doExecute() = transaction {
        val (role, email, firstName, lastName) = input
        val currentUserRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUser.id, orgId)

        if (currentUserRole != null && currentUserRole.role >= role) {
            val org = Organizations.findById(orgId)!!
            val validation = UserValidations.createForInvite(org.id, role, email, firstName, lastName)

            sendUserInvite(org, currentUser, validation)

            validation
        } else {
            throw Exception("User does not have permission to assign this role")
        }
    }

    private fun sendUserInvite(organization: Organization, invitingUser: User, validation: UserValidation) {
        val url = "$webAppUrl/?action=acceptInvite&token=${validation.token.urlEncode()}"
        Email.send(
            template = UseCaseConfig.userInviteTemplate,
            to = validation.toEmailData(),
            data = mapOf("url" to url, "organization" to organization.name, "invitedBy" to invitingUser.fullName)
        )
    }
}