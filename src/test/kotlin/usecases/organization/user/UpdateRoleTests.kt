package usecases.organization.user

import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.organization.UserRole
import entities.user.User
import entities.user.Users
import io.kotest.matchers.shouldBe
import usecases.UseCaseSpec

class UpdateRoleTests : UseCaseSpec() {
    private lateinit var user: User
    private lateinit var orgUser: OrganizationUser
    private lateinit var currentOrgUser: OrganizationUser

    init {
        objectMocks(OrganizationUsers)

        "update user" {
            val result = execute(orgUser.userId, UserRole.Admin)

            result?.role shouldBe UserRole.Admin
        }

        "cannot update user with higher role" {
            OrganizationUsers.updateRole(currentOrgUser.id, UserRole.Admin)
            OrganizationUsers.updateRole(orgUser.id, UserRole.Owner)

            val result = execute(orgUser.userId, UserRole.User)

            result?.role shouldBe UserRole.Owner
        }

        "cannot update user to higher role" {
            OrganizationUsers.updateRole(currentOrgUser.id, UserRole.Admin)
            OrganizationUsers.updateRole(orgUser.id, UserRole.Admin)

            val result = execute(orgUser.userId, UserRole.Owner)

            result?.role shouldBe UserRole.Admin
        }

        "cannot update last owner" {
            val result = execute(currentUserId, UserRole.Admin)

            result?.role shouldBe UserRole.Owner
        }

        beforeEach {
            user = Users.create("update@test.com")!!
            orgUser = OrganizationUsers.create(testOrgId, user.id, UserRole.User)
            currentOrgUser = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, testOrgId)!!
        }
    }

    private fun execute(userToUpdate: String, role: UserRole) =
        useCase(UpdateRole::class, UpdateRole.Input(userToUpdate, role)).execute()
}