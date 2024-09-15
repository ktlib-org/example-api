package usecases.organization.invite


import domain.entities.organization.OrganizationUsers
import domain.entities.organization.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.ktlib.email.Email
import usecases.UseCaseSpec
import usecases.organization.invite.InviteUser.Input

class InviteUserTests : UseCaseSpec() {
    init {
        objectMocks(Email)

        "invite" {
            val validation = execute(Input(UserRole.User, "anew@ktlib.org"))

            validation.email shouldBe "anew@ktlib.org"
            verify {
                Email.send(any(), any(), any(), any())
            }
        }

        "cannot invite user to role higher than yours" {
            val userRole = OrganizationUsers.findByUserIdAndOrganizationId(currentUserId, testOrgId)!!
            OrganizationUsers.updateRole(userRole.id, UserRole.Admin)

            shouldThrow<Exception> {
                execute(Input(UserRole.Owner, "anew@ktlib.org"))
            }
        }

        beforeEach {
            every { Email.send(any(), any(), any(), any()) } just Runs
        }
    }

    private fun execute(input: Input) = useCase(InviteUser::class, input).execute()
}