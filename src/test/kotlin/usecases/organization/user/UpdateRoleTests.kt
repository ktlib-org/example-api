package usecases.organization.user

import entities.organization.OrganizationUsers
import entities.organization.UserRole
import entities.user.Users
import io.kotlintest.shouldBe
import org.ktapi.test.DbStringSpec

class UpdateRoleTests : DbStringSpec() {
    init {
        "update user" {
            val newUser = Users.create("test@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            UpdateRole.updateRole(1, newUser.id, UserRole.User, 1)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.User
        }

        "cannot update user with higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Owner)

            UpdateRole.updateRole(1, orgUser.id, UserRole.User, newUserOne.id)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Owner
        }

        "cannot update user to higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Admin)

            UpdateRole.updateRole(1, orgUser.id, UserRole.Owner, newUserOne.id)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Admin
        }

        "cannot update last owner" {
            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(1, 1)!!

            UpdateRole.updateRole(1, orgUser.id, UserRole.Admin, 1)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Owner
        }
    }
}