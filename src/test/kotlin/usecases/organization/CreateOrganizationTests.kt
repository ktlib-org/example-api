package usecases.organization

import entities.organization.OrganizationUsers
import entities.organization.UserRole
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.ktapi.entities.ValidationException
import org.ktapi.test.DbStringSpec
import usecases.organization.CreateOrganization.CreateOrganizationData

class CreateOrganizationTests : DbStringSpec() {
    init {
        "create org" {
            val org = CreateOrganization.create(CreateOrganizationData("MyOrg"), 1)

            val orgUsers = OrganizationUsers.findByOrganizationId(org.id)
            org.name shouldBe "MyOrg"
            orgUsers.size shouldBe 1
            orgUsers.first().userId shouldBe 1
            orgUsers.first().role shouldBe UserRole.Owner
        }

        "create org throws exception if name is blank" {
            shouldThrow<ValidationException> {
                CreateOrganization.create(CreateOrganizationData(""), 1)
            }
        }
    }
}