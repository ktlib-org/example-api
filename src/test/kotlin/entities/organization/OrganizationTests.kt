package entities.organization

import io.kotlintest.shouldBe
import org.ktapi.test.DbStringSpec

class OrganizationTests : DbStringSpec({
    "inserting data" {
        val org = Organizations.create("MyOrg")

        val result = Organizations.findById(org.id)

        result?.name shouldBe org.name
    }
})