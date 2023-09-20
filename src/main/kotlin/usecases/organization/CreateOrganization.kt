package usecases.organization

import entities.organization.Organization
import entities.organization.OrganizationUsers
import entities.organization.Organizations.create
import entities.organization.UserRole
import entities.user.Users
import org.ktlib.entities.populateFrom
import org.ktlib.entities.transaction
import org.ktlib.slack.Slack
import org.ktlib.slack.WebhookMessage
import usecases.Role
import usecases.UseCase

class CreateOrganization : UseCase<CreateOrganization.Input, Organization>(Role.UserNoOrg) {
    data class Input(val name: String)

    override fun doExecute() = transaction {
        val org = Organization { populateFrom(input) }.validate().create()
        val user = Users.findById(currentUserId)!!
        OrganizationUsers.create(org.id, user.id, UserRole.Owner)
        Slack.sendMessage(WebhookMessage("Organization created named ${org.name} for user ${user.email}"))
        org
    }
}