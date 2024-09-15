package usecases.organization.user

import domain.entities.organization.OrganizationUser
import domain.entities.organization.OrganizationUsers
import domain.entities.organization.UserRole
import domain.entities.user.User
import domain.entities.user.Users
import io.kotest.matchers.shouldBe
import usecases.UseCaseSpec
import java.util.*

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

    private fun execute(userToUpdate: UUID, role: UserRole) =
        useCase(UpdateRole::class, UpdateRole.Input(userToUpdate, role)).execute()
}