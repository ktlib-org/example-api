package model

import org.ktapi.test.DbStringSpec
import io.kotlintest.shouldBe
import model.user.Users

class OrganizationUserTests : DbStringSpec({
    "inserting data" {
        val org = Organizations.create("MyOrg")
        val user = Users.create("my@email.com")!!

        OrganizationUsers.create(org.id, user.id, UserRole.User)
        val result = OrganizationUsers.findByOrganizationId(org.id)

        result.size shouldBe 1
        result.first().userId shouldBe user.id
        result.first().role shouldBe UserRole.User
    }

    "roles are comparable" {
        (UserRole.Owner > UserRole.User) shouldBe true
        (UserRole.Owner >= UserRole.User) shouldBe true
        (UserRole.Admin > UserRole.User) shouldBe true
        (UserRole.Admin >= UserRole.User) shouldBe true
        (UserRole.User >= UserRole.User) shouldBe true
        (UserRole.User > UserRole.User) shouldBe false
        (UserRole.User >= UserRole.Admin) shouldBe false
        (UserRole.User >= UserRole.Owner) shouldBe false
        (UserRole.Admin >= UserRole.Owner) shouldBe false
        (UserRole.Owner <= UserRole.User) shouldBe false
    }
})