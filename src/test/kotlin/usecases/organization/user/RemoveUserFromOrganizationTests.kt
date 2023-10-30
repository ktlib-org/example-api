package usecases.organization.user

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.UserRole
import entities.user.User
import entities.user.UserLogins
import entities.user.Users
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import usecases.UseCaseContext
import usecases.UseCaseSpec
import java.util.*

class RemoveUserFromOrganizationTests : UseCaseSpec() {
    private lateinit var user: User
    private lateinit var admin: User
    private lateinit var orgUser: OrganizationUser
    private lateinit var orgAdmin: OrganizationUser

    init {
        objectMocks(OrganizationUsers)

        "remove user" {
            execute(orgUser.userId)

            OrganizationUsers.findById(orgUser.id) shouldBe null
        }

        "cannot remove user with higher role" {
            OrganizationUsers.updateRole(orgUser.id, UserRole.Owner)

            val context =
                UseCaseContext(UserLogins.create(admin.id), testOrgId, RemoveUserFromOrganization.Input(orgUser.id))

            useCase(RemoveUserFromOrganization::class, context).execute()

            OrganizationUsers.findById(orgAdmin.id) shouldNotBe null
        }

        "cannot remove last owner" {
            execute(currentUserId)

            OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, testOrgId) shouldNotBe null
        }

        beforeEach {
            user = Users.create("temp@user.com")!!
            admin = Users.create("admin@user.com")!!
            orgUser = OrganizationUsers.create(testOrgId, user.id, UserRole.User)
            orgAdmin = OrganizationUsers.create(testOrgId, admin.id, UserRole.Admin)
        }
    }

    private fun execute(userId: UUID) =
        useCase(RemoveUserFromOrganization::class, RemoveUserFromOrganization.Input(userId)).execute()
}
