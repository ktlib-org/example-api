package model

import org.ktapi.test.DbStringSpec
import io.kotlintest.shouldBe

class OrganizationTests : DbStringSpec({
    "inserting data" {
        val org = Organizations.create("MyOrg")

        val result = Organizations.findById(org.id)

        result?.name shouldBe org.name
    }
})