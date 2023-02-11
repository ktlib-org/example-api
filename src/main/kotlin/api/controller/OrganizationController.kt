package api.controller

import api.ApiRole.*
import api.bodyFromJson
import api.organizationId
import api.user
import api.userId
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.*
import model.*
import model.user.UserValidationData
import model.user.UserValidations
import org.ktapi.model.ValidationError
import org.ktapi.web.Router
import org.ktapi.web.idPathParam
import service.OrganizationService
import service.OrganizationService.OrganizationCreate
import service.OrganizationService.OrganizationUpdate

object OrganizationController : Router {
    override fun route() {
        path("/organization") {
            get(this::show, User)
            post(this::create, UserNoOrg)
            patch(this::update, Owner)
            post("/invites", this::invite, Admin)
            path("/invites") {
                get(this::invites, Admin)
                delete("/{inviteId}", this::removeInvite, Admin)
            }
            path("/users") {
                get(this::users, User)
                path("/{userId}") {
                    delete(this::removeUser, Admin)
                    patch(this::updateUserRole, Admin)
                }
            }
        }
    }

    private const val tag = "Organization"

    @OpenApi(
        path = "/organization",
        methods = [HttpMethod.POST],
        operationId = "createOrganization",
        summary = "Creates a new organization for the logged in user",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(OrganizationCreate::class)]),
        responses = [OpenApiResponse("200", [OpenApiContent(OrganizationData::class)])]
    )
    private fun create(ctx: Context) {
        ctx.json(OrganizationService.create(ctx.bodyFromJson(), ctx.userId))
    }

    @OpenApi(
        path = "/organization",
        methods = [HttpMethod.PATCH],
        operationId = "updateOrganization",
        summary = "Updates an organization",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(OrganizationUpdate::class)]),
        responses = [OpenApiResponse("200", [OpenApiContent(OrganizationData::class)])]
    )
    private fun update(ctx: Context) {
        ctx.json(OrganizationService.update(ctx.organizationId, ctx.bodyFromJson()))
    }

    @OpenApi(
        path = "/organization",
        methods = [HttpMethod.GET],
        operationId = "showOrganization",
        summary = "Returns an organization",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(OrganizationData::class)])]
    )
    private fun show(ctx: Context) {
        ctx.json(Organizations.findById(ctx.organizationId)!!)
    }

    data class InviteCreate(val role: UserRole, val email: String, val firstName: String, val lastName: String)

    @OpenApi(
        path = "/organization/invites",
        methods = [HttpMethod.POST],
        operationId = "invite",
        summary = "Sends an email inviting a user to an organization",
        tags = [tag],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(InviteCreate::class)]),
        responses = [
            OpenApiResponse("200", [OpenApiContent(UserValidationData::class)]),
            OpenApiResponse("400", [OpenApiContent(Array<ValidationError>::class)])
        ]
    )
    private fun invite(ctx: Context) {
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

    @OpenApi(
        path = "/organization/invites",
        methods = [HttpMethod.GET],
        operationId = "invites",
        summary = "Returns invites for an organization",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Array<UserValidationData>::class)])]
    )
    private fun invites(ctx: Context) {
        ctx.json(UserValidations.findByOrganization(ctx.organizationId))
    }

    @OpenApi(
        path = "/organization/invites/{inviteId}",
        methods = [HttpMethod.DELETE],
        operationId = "removeInvite",
        summary = "Removes an invite to a user",
        tags = [tag],
        pathParams = [OpenApiParam(name = "inviteId", type = Long::class, required = true)],
        responses = [OpenApiResponse("200")]
    )
    private fun removeInvite(ctx: Context) {
        OrganizationService.removeInvite(ctx.organizationId, ctx.idPathParam("inviteId"))
        ctx.status(HttpStatus.OK)
    }

    data class OrganizationUserData(private val orgUser: OrganizationUser) {
        val id = orgUser.id
        val userId = orgUser.userId
        val firstName = orgUser.user.firstName
        val lastName = orgUser.user.lastName
        val since = orgUser.createdAt
        val email = orgUser.user.email
        val role = orgUser.role
    }

    @OpenApi(
        path = "/organization/users",
        methods = [HttpMethod.GET],
        operationId = "organizationUsers",
        summary = "Returns users for an organization",
        tags = [tag],
        responses = [OpenApiResponse("200", [OpenApiContent(Array<OrganizationUserData>::class)])]
    )
    private fun users(ctx: Context) {
        val users = OrganizationUsers.findByOrganizationId(ctx.organizationId)
            .preloadUsers()
            .map { OrganizationUserData(it) }

        ctx.json(users)
    }

    @OpenApi(
        path = "/organization/users/{userId}",
        methods = [HttpMethod.DELETE],
        operationId = "removeUser",
        summary = "Removes a user from an organization",
        tags = [tag],
        pathParams = [OpenApiParam(name = "userId", type = Long::class, required = true)],
        responses = [OpenApiResponse("200")]
    )
    private fun removeUser(ctx: Context) {
        OrganizationService.removeUser(
            orgId = ctx.organizationId,
            userId = ctx.idPathParam("userId"),
            currentUserId = ctx.userId
        )

        ctx.status(HttpStatus.OK)
    }

    data class UserRoleUpdate(val role: UserRole)

    @OpenApi(
        path = "/organization/users/{userId}",
        methods = [HttpMethod.PATCH],
        operationId = "updateUserRole",
        summary = "Changes role of a user",
        tags = [tag],
        pathParams = [OpenApiParam(name = "userId", type = Long::class, required = true)],
        requestBody = OpenApiRequestBody(content = [OpenApiContent(UserRoleUpdate::class)]),
        responses = [OpenApiResponse("200", [OpenApiContent(OrganizationUserData::class)])]
    )
    private fun updateUserRole(ctx: Context) {
        val (role) = ctx.bodyFromJson<UserRoleUpdate>()

        val orgUser = OrganizationService.updateRole(
            orgId = ctx.organizationId,
            userId = ctx.idPathParam("userId"),
            role = role,
            currentUserId = ctx.userId
        )

        if (orgUser == null) {
            ctx.status(HttpStatus.OK)
        } else {
            ctx.json(OrganizationUserData(orgUser))
        }
    }
}