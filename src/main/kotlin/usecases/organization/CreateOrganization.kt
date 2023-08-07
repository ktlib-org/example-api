package usecases.organization

import entities.organization.Organization
import entities.organization.OrganizationUsers
import entities.organization.Organizations.create
import entities.organization.UserRole
import entities.user.Users
import org.ktapi.db.transaction
import org.ktapi.entities.populateFrom
import org.ktapi.slack.Slack
import org.ktapi.slack.WebhookMessage

object CreateOrganization {
    data class CreateOrganizationData(val name: String)

    fun create(data: CreateOrganizationData, adminUserId: Long) = transaction {
        val org = Organization {}.populateFrom(data).validate().create()
        val user = Users.findById(adminUserId)!!
        OrganizationUsers.create(org.id, user.id, UserRole.Owner)
        Slack.sendMessage(WebhookMessage("Organization created named ${org.name} for user ${user.email}"))
        org
    }
}