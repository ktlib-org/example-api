package usecases.organization

import entities.organization.Organizations
import io.kotest.matchers.shouldBe
import usecases.UseCaseSpec

class UpdateOrganizationTests : UseCaseSpec() {
    init {
        "update org" {
            execute(mapOf("name" to "NewName"))

            val org = Organizations.findById(testOrgId)!!
            org.name shouldBe "NewName"
        }
    }

    private fun execute(data: Map<String, Any?>) = useCase(UpdateOrganization::class, data).execute()
}