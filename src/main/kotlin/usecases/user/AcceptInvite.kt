package usecases.user

import domain.entities.organization.OrganizationUsers
import domain.entities.user.UserLogin
import domain.entities.user.UserLogins
import domain.entities.user.UserValidations
import domain.entities.user.UserValidations.delete
import domain.entities.user.Users
import org.ktlib.entities.ValidationError
import org.ktlib.entities.ValidationErrors
import org.ktlib.entities.ValidationException
import org.ktlib.entities.transaction
import org.ktlib.slack.Slack
import org.ktlib.slack.WebhookMessage
import usecases.Role
import usecases.UseCase

class AcceptInvite : UseCase<AcceptInvite.Input, UserLogin>(Role.Anyone) {
    data class Input(val token: String)

    override fun doExecute() = transaction {
        val validation = UserValidations.findByToken(input.token)

        validation?.delete()

        if (validation?.isValid != true) {
            throw ValidationException(ValidationErrors(mutableListOf(ValidationError("token", "Invalid token."))))
        } else {
            var userId = currentUserIdOrNull ?: Users.findByEmail(validation.email)?.id

            if (userId == null) {
                userId = Users.create(validation)!!.id
                Slack.sendMessage(WebhookMessage("User account created for email: ${validation.email}"))
            }

            val currentRole = OrganizationUsers.findByUserIdAndOrganizationId(userId, validation.organizationId!!)

            if (currentRole == null) {
                OrganizationUsers.create(validation.organizationId!!, userId, validation.role!!)
            } else if (currentRole.role < validation.role!!) {
                OrganizationUsers.updateRole(currentRole.id, validation.role!!)
            }

            currentUserLoginOrNull ?: UserLogins.create(userId)
        }
    }
}