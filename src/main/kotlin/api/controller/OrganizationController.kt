package api.controller

import api.ApiRole.*
import api.bodyFromJson
import api.organizationId
import api.user
import api.userId
import com.fasterxml.jackson.annotation.JsonIgnore
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.HttpCode
import model.*
import model.user.UserValidationData
import model.user.UserValidations
import org.ktapi.web.Router
import org.ktapi.web.documentedHandler
import org.ktapi.web.idPathParam
import service.OrganizationService
import service.OrganizationService.OrganizationCreate
import service.OrganizationService.OrganizationUpdate

object OrganizationController : Router {
    override fun route() {
        path("/organization") {
            get(show, User)
            post(create, UserNoOrg)
            patch(update, Owner)
            post("/invite", invite, Admin)
            path("/invites") {
                get(invites, Admin)
                delete("/{inviteId}", removeInvite, Admin)
            }
            get("/users", users, User)
            path("/users/{userId}") {
                delete(removeUser, Admin)
                patch(updateUserRole, Admin)
            }
        }
    }

    private const val tag = "Organization"

    private val create = documentedHandler {
        doc("create", "Creates a new organization for the logged in user", tag) {
            body<OrganizationCreate>()
            json<OrganizationData>("200")
        }
        handler { ctx ->
            ctx.json(OrganizationService.create(ctx.bodyFromJson(), ctx.userId))
        }
    }

    private val update = documentedHandler {
        doc("update", "Updates an organization", tag) {
            body<OrganizationUpdate>()
            json<OrganizationData>("200")
        }
        handler { ctx ->
            ctx.json(OrganizationService.update(ctx.organizationId, ctx.bodyFromJson()))
        }
    }

    private val show = documentedHandler {
        doc("show", "Returns an organization", tag) {
            json<OrganizationData>("200")
        }
        handler { ctx ->
            ctx.json(Organizations.findById(ctx.organizationId)!!)
        }
    }

    data class InviteCreate(val role: UserRole, val email: String, val firstName: String, val lastName: String)

    private val invite = documentedHandler {
        doc("invite", "Sends an email inviting a user to an organization", tag) {
            body<InviteCreate>()
            json<UserValidationData>("200")
        }
        handler { ctx ->
            val invite = ctx.bodyFromJson<InviteCreate>()
            val validation = OrganizationService.inviteUser(
                orgId = ctx.organizationId,
                invitingUser = ctx.user,
                role = invite.role,
                email = invite.email,
                firstName = invite.firstName,
                lastName = invite.lastName
            )
            ctx.json(validation)
        }
    }

    private val invites = documentedHandler {
        doc("invites", "Returns invites for an organization", tag) {
            jsonArray<UserValidationData>("200")
        }
        handler { ctx ->
            ctx.json(UserValidations.findByOrganization(ctx.organizationId))
        }
    }

    private val removeInvite = documentedHandler {
        doc("removeInvite", "Removes an invite to a user", tag) {
            idPathParam("inviteId")
            json<Unit>("200")
        }
        handler { ctx ->
            OrganizationService.removeInvite(ctx.organizationId, ctx.idPathParam("inviteId"))
            ctx.status(HttpCode.OK)
        }
    }

    data class OrganizationUserData(@JsonIgnore val orgUser: OrganizationUser) {
        val id = orgUser.id
        val userId = orgUser.userId
        val firstName = orgUser.user.firstName
        val lastName = orgUser.user.lastName
        val since = orgUser.createdAt
        val email = orgUser.user.email
        val role = orgUser.role
    }

    private val users = documentedHandler {
        doc("users", "Returns users for an organization", tag) {
            jsonArray<OrganizationUserData>("200")
        }
        handler { ctx ->
            val users = OrganizationUsers.findByOrganizationId(ctx.organizationId)
                .preloadUsers()
                .map { OrganizationUserData(it) }
            ctx.json(users)
        }
    }

    private val removeUser = documentedHandler {
        doc("removeUser", "Removes a user from an organization", tag) {
            idPathParam("userId")
            json<Unit>("200")
        }
        handler { ctx ->
            OrganizationService.removeUser(
                orgId = ctx.organizationId,
                userId = ctx.idPathParam("userId"),
                currentUserId = ctx.userId
            )
            ctx.status(HttpCode.OK)
        }
    }

    data class UserRoleUpdate(val role: UserRole)

    private val updateUserRole = documentedHandler {
        doc("updateUserRole", "Changes role of a user", tag) {
            idPathParam("userId")
            body<UserRoleUpdate>()
            json<Boolean>("200")
        }
        handler { ctx ->
            val update = ctx.bodyFromJson<UserRoleUpdate>()
            OrganizationService.updateRole(
                orgId = ctx.organizationId,
                userId = ctx.idPathParam("userId"),
                role = update.role,
                currentUserId = ctx.userId
            )
            ctx.status(HttpCode.OK)
        }
    }
}