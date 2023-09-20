package usecases.organization

import entities.organization.OrganizationUsers
import entities.organization.UserRole
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.ktlib.entities.ValidationException
import org.ktlib.slack.Slack
import usecases.UseCaseSpec

class CreateOrganizationTests : UseCaseSpec() {
    init {
        objectMocks(Slack)

        "create org" {
            val org = execute("MyOrg")

            val orgUser = OrganizationUsers.findByOrganizationId(org.id)
            org.name shouldBe "MyOrg"
            orgUser.size shouldBe 1
            orgUser.first().userId shouldBe currentUserId
            orgUser.first().role shouldBe UserRole.Owner
            verify { Slack.sendMessage(any()) }
        }

        "create org throws exception if name is blank" {
            shouldThrow<ValidationException> {
                execute("")
            }
        }
    }

    private fun execute(name: String) =
        useCase(CreateOrganization::class, CreateOrganization.Input(name)).execute()
}
