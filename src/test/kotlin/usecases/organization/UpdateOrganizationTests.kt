package usecases.organization

import entities.organization.Organizations
import io.kotlintest.shouldBe
import org.ktapi.test.DbStringSpec

class UpdateOrganizationTests : DbStringSpec() {
    init {
        "update org" {
            var org = Organizations.create("OriginalName")

            UpdateOrganization.update(org.id, mapOf("name" to "NewName"))

            org = Organizations.findById(org.id)!!
            org.name shouldBe "NewName"
        }
    }
}