package usecases.organization.invite


import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.Users
import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.ktapi.test.DbStringSpec
import services.EmailService

class InviteUserTests : DbStringSpec() {
    init {
        "invite" {
            val user = Users.findById(1)!!
            val org = Organizations.findById(1)!!
            val validation = InviteUser.inviteUser(org.id, UserRole.User, user, "anew@ktapi.org")

            validation.email shouldBe "anew@ktapi.org"
            validation.role shouldBe UserRole.User
            verify {
                EmailService.sendUserInvite(org, user, validation)
            }
        }

        "cannot invite user to role higher than yours" {
            val newUser = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            shouldThrow<Exception> {
                InviteUser.inviteUser(1, UserRole.Owner, newUser, "anew@ktapi.org")
            }
        }
    }

    override val objectMocks = listOf(EmailService)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        every {
            EmailService.sendUserInvite(any(), any(), any())
        } just Runs
    }
}