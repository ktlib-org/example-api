package usecases.user

import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.UserLogins
import entities.user.UserValidations
import entities.user.Users
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import org.ktapi.hoursAgo
import org.ktapi.test.DbStringSpec

class AcceptInviteTests : DbStringSpec() {
    init {
        "accept invite" {
            val org = Organizations.create("NewOrg")
            val user = Users.findById(1)!!
            val validation = UserValidations.createForInvite(org.id, UserRole.Admin, user)
            val userLogin = UserLogins.create(1)

            val result = AcceptInvite.acceptInvite(userLogin, validation.token)

            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(user.id, org.id)
            UserValidations.findById(validation.id) shouldBe null
            orgUser shouldNotBe null
            orgUser?.role shouldBe UserRole.Admin
            result shouldBe userLogin
        }

        "accept invite updates existing role if one exists" {
            val org = Organizations.create("NewOrg")
            val user = Users.findById(1)!!
            OrganizationUsers.create(org.id, user.id, UserRole.User)
            val validation = UserValidations.createForInvite(org.id, UserRole.Admin, user)

            val result = AcceptInvite.acceptInvite(null, validation.token)

            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(user.id, org.id)
            UserValidations.findById(validation.id) shouldBe null
            orgUser shouldNotBe null
            orgUser?.role shouldBe UserRole.Admin
            result shouldNotBe null
        }

        "accept invite does not update existing role if invite has lower role" {
            val org = Organizations.create("NewOrg")
            val user = Users.findById(1)!!
            OrganizationUsers.create(org.id, user.id, UserRole.Admin)
            val validation = UserValidations.createForInvite(org.id, UserRole.User, user)

            val result = AcceptInvite.acceptInvite(null, validation.token)

            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(user.id, org.id)
            UserValidations.findById(validation.id) shouldBe null
            orgUser shouldNotBe null
            orgUser?.role shouldBe UserRole.Admin
            result shouldNotBe null
        }

        "accept invite uses existing user if one exists" {
            val user = Users.create("mynew@email.com")!!
            val validation = UserValidations.createForInvite(1, UserRole.User, user.email)

            val result = AcceptInvite.acceptInvite(null, validation.token)

            result?.userId shouldBe user.id
        }

        "accept invite creates new user if one does not exist" {
            val validation = UserValidations.createForInvite(1, UserRole.User, "yetanother@email.com")

            val result = AcceptInvite.acceptInvite(null, validation.token)

            result shouldNotBe null
            Users.findById(result?.userId)!!.email shouldBe "yetanother@email.com"
        }

        "accept invite doesn't work with outdated invite" {
            val validation = UserValidations.createForInvite(1, UserRole.Admin, "my@email.com")
            UserValidations.setCreatedAt(validation.id, (14 * 24).hoursAgo())

            val result = AcceptInvite.acceptInvite(null, validation.token)

            result shouldBe null
            UserValidations.findById(validation.id) shouldBe null
        }
    }
}
