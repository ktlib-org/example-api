package usecases.organization.user

import entities.organization.OrganizationUsers
import entities.organization.UserRole
import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.ktapi.test.DbStringSpec

class RemoveUserFromOrganizationTests : DbStringSpec() {
    init {
        "remove user" {
            val newUser = Users.create("test@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            RemoveUserFromOrganization.removeUser(1, newUser.id, 1)

            OrganizationUsers.findById(orgUser.id) shouldBe null
        }

        "cannot remove user with higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Owner)

            RemoveUserFromOrganization.removeUser(1, orgUser.id, newUserOne.id)

            OrganizationUsers.findById(orgUser.id) shouldNotBe null
        }

        "cannot remove last owner" {
            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(1, 1)!!

            RemoveUserFromOrganization.removeUser(1, orgUser.id, 1)

            OrganizationUsers.findById(orgUser.id) shouldNotBe null
        }
    }
}
