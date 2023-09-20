package usecases.user

import entities.organization.Organization
import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.UserLogins
import entities.user.UserValidation
import entities.user.UserValidations
import entities.user.UserValidations.update
import entities.user.Users
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import org.ktlib.entities.ValidationException
import org.ktlib.hoursAgo
import usecases.UseCaseContext
import usecases.UseCaseSpec
import usecases.user.AcceptInvite.Input

class AcceptInviteTests : UseCaseSpec() {
    private lateinit var newOrg: Organization
    private lateinit var validation: UserValidation

    init {
        objectMocks(Users, UserLogins, UserValidations, OrganizationUsers)

        "accept invite" {
            val result = execute(validation.token)

            result shouldBe currentUserLogin
            UserValidations.findById(validation.id) shouldBe null
            OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, testOrgId) shouldNotBe null
        }

        "accept invite updates existing role if one exists" {
            val orgUser = OrganizationUsers.create(newOrg.id, currentUserId, UserRole.User)

            execute(validation.token)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Admin
        }

        "accept invite does not update existing role if invite has lower role" {
            val orgUser = OrganizationUsers.create(newOrg.id, currentUserId, UserRole.Owner)

            execute(validation.token)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Owner
        }

        "accept invite creates new user if one does not exist" {
            every { Users.findByEmail(validation.email) } returns null

            val userLogin =
                useCase(AcceptInvite::class, UseCaseContext(input = Input(validation.token))).execute()

            userLogin.userId shouldNotBe currentUserId
        }

        "accept invite doesn't work with outdated invite" {
            validation.apply { createdAt = (14 * 24 + 1).hoursAgo() }.update()

            shouldThrow<ValidationException> {
                execute(validation.token)
            }
        }

        beforeEach {
            newOrg = Organizations.create("testOrg")
            validation = UserValidations.createForInvite(newOrg.id, UserRole.Admin, "email")
        }
    }

    private fun execute(token: String) = useCase(AcceptInvite::class, Input(token)).execute()
}
