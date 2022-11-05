package service

import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import model.OrganizationUsers
import model.Organizations
import model.UserRole
import model.user.UserValidations
import model.user.Users
import org.ktapi.model.ValidationException
import org.ktapi.test.DbStringSpec
import service.OrganizationService.OrganizationCreate

class OrganizationServiceTests : DbStringSpec() {
    init {
        "create org" {
            val org = OrganizationService.create(OrganizationCreate("MyOrg"), 1)

            val orgUsers = OrganizationUsers.findByOrganizationId(org.id)
            org.name shouldBe "MyOrg"
            orgUsers.size shouldBe 1
            orgUsers.first().userId shouldBe 1
            orgUsers.first().role shouldBe UserRole.Owner
        }

        "create org throws exception if name is blank" {
            shouldThrow<ValidationException> {
                OrganizationService.create(OrganizationCreate(""), 1)
            }
        }

        "update org" {
            var org = Organizations.create("OriginalName")

            OrganizationService.update(org.id, mapOf("name" to "NewName"))

            org = Organizations.findById(org.id)!!
            org.name shouldBe "NewName"
        }

        "invite" {
            val user = Users.findById(1)!!
            val org = Organizations.findById(1)!!
            val validation = OrganizationService.inviteUser(org.id, UserRole.User, user, "anew@ktapi.org")

            validation.email shouldBe "anew@ktapi.org"
            validation.role shouldBe UserRole.User
            verify {
                EmailService.sendUserInvite(org, user, validation)
            }
        }

        "cannot invite user to role higher than yours" {
            val newUser = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            shouldThrow<Exception> {
                OrganizationService.inviteUser(1, UserRole.Owner, newUser, "anew@ktapi.org")
            }
        }

        "remove invite" {
            val user = Users.findById(1)!!
            val invite = UserValidations.createForInvite(1, UserRole.User, user)

            OrganizationService.removeInvite(1, invite.id)

            UserValidations.findById(invite.id) shouldBe null
        }

        "remove user" {
            val newUser = Users.create("test@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            OrganizationService.removeUser(1, newUser.id, 1)

            OrganizationUsers.findById(orgUser.id) shouldBe null
        }

        "cannot remove user with higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Owner)

            OrganizationService.removeUser(1, orgUser.id, newUserOne.id)

            OrganizationUsers.findById(orgUser.id) shouldNotBe null
        }

        "cannot remove last owner" {
            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(1, 1)!!

            OrganizationService.removeUser(1, orgUser.id, 1)

            OrganizationUsers.findById(orgUser.id) shouldNotBe null
        }

        "update user" {
            val newUser = Users.create("test@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUser.id, UserRole.Admin)

            OrganizationService.updateRole(1, newUser.id, UserRole.User, 1)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.User
        }

        "cannot update user with higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Owner)

            OrganizationService.updateRole(1, orgUser.id, UserRole.User, newUserOne.id)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Owner
        }

        "cannot update user to higher role" {
            val newUserOne = Users.create("test@ktapi.org")!!
            OrganizationUsers.create(1, newUserOne.id, UserRole.Admin)
            val newUserTwo = Users.create("test2@ktapi.org")!!
            val orgUser = OrganizationUsers.create(1, newUserTwo.id, UserRole.Admin)

            OrganizationService.updateRole(1, orgUser.id, UserRole.Owner, newUserOne.id)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Admin
        }

        "cannot update last owner" {
            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(1, 1)!!

            OrganizationService.updateRole(1, orgUser.id, UserRole.Admin, 1)

            OrganizationUsers.findById(orgUser.id)?.role shouldBe UserRole.Owner
        }
    }

    override val objectMocks = listOf(EmailService)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        every {
            EmailService.sendUserInvite(any(), any(), any())
        } just Runs
    }
}