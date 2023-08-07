package usecases.user

import entities.organization.OrganizationUsers
import entities.user.UserLogin
import entities.user.UserLogins
import entities.user.UserValidations
import entities.user.Users
import org.ktapi.db.transaction
import org.ktapi.slack.Slack
import org.ktapi.slack.WebhookMessage

object AcceptInvite {
    fun acceptInvite(userLogin: UserLogin?, token: String) = transaction {
        val validation = UserValidations.findByToken(token)

        validation?.delete()

        if (validation?.isValid != true) {
            null
        } else {
            var userId =
                userLogin?.userId ?: Users.findByEmail(validation.email)?.id

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

            userLogin ?: UserLogins.create(userId)
        }
    }
}