package service

import io.kotlintest.TestCase
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import io.mockk.*
import model.Organizations
import model.OrganizationUsers
import model.UserRole
import model.user.UserLogins
import model.user.Users
import model.user.UserValidations
import org.ktapi.Encryption
import org.ktapi.daysAgo
import org.ktapi.model.ValidationException
import org.ktapi.test.DbStringSpec
import java.time.LocalDateTime

class UserServiceTests : DbStringSpec() {
    init {
        "update user" {
            var user = Users.findById(1)!!

            UserService.update(user, mapOf("firstName" to "newFirst", "lastName" to "newLast"))

            user = Users.findById(1)!!
            user.firstName shouldBe "newFirst"
            user.lastName shouldBe "newLast"
        }

        "update user doesn't work to invalid domain" {
            val user = Users.findById(1)!!

            shouldThrow<ValidationException> {
                UserService.update(user, mapOf("email" to "something@else.com"))
            }
        }

        "update user works to valid domain" {
            val user = Users.findById(1)!!

            shouldThrow<ValidationException> {
                UserService.update(user, mapOf("email" to "something@ktapi.org"))
            }
        }

        "updatePassword" {
            UserService.updatePassword(1, "myNewPasswordHere")

            val user = Users.findById(1)!!
            Encryption.passwordMatches("myNewPasswordHere", user.password) shouldBe true
        }

        "updatePassword with too short of password throws exception" {
            val exception = shouldThrow<ValidationException> {
                UserService.updatePassword(1, "short")
            }

            exception.validationErrors.size shouldBe 1
            exception.validationErrors.errors.first().field shouldBe "password"
            exception.validationErrors.errors.first().message shouldBe "password must be at least 8 characters in length"
        }

        "hasPermission" {
            UserService.hasPermission(1, 1, listOf(UserRole.User)) shouldBe true
            UserService.hasPermission(1, 1, listOf(UserRole.Admin)) shouldBe true
            UserService.hasPermission(1, 1, listOf(UserRole.Owner)) shouldBe true
            UserService.hasPermission(2, 1, listOf(UserRole.User)) shouldBe false
            UserService.hasPermission(null, 1, listOf(UserRole.User)) shouldBe false
            UserService.hasPermission(1, null, listOf(UserRole.User)) shouldBe false
            UserService.hasPermission(1, 1, listOf()) shouldBe false
        }

        "verifyEmail with valid" {
            val validation = UserValidations.createForEmailValidation("my@email.com", "first", "last")

            val userLogin = UserService.verifyEmail(validation.token)!!

            val user = Users.findById(userLogin.userId)!!
            UserValidations.findById(validation.id) shouldBe null
            user.email shouldBe validation.email
            user.firstName shouldBe validation.firstName
            user.lastName shouldBe validation.lastName
        }

        "verify email does nothing when invalid" {
            var validation = UserValidations.createForEmailValidation("my@email.com")
            validation = UserValidations.setCreatedAt(validation.id, LocalDateTime.now().minusDays(5))

            val result = UserService.verifyEmail(validation.token)

            result shouldBe null
        }

        "verify email works with exising email" {
            val user = Users.create("another@email.com")!!
            val validation = UserValidations.createForEmailValidation(user.email)

            val result = UserService.verifyEmail(validation.token)!!

            result.userId shouldBe user.id
        }

        "forgot password sends email" {
            var user = Users.findById(1)!!

            UserService.forgotPassword(user.email)

            user = Users.findById(1)!!
            user.passwordSet shouldBe false
            verify {
                EmailService.sendForgotPassword(any())
            }
        }

        "forgot password does nothing if email not found" {
            UserService.forgotPassword("fake@email.coms")

            verify {
                EmailService wasNot Called
            }
        }

        "token login" {
            val validation = UserValidations.createForForgotPassword(Users.findById(1)!!)

            val result = UserService.tokenLogin(validation.token)

            result shouldNotBe null
            UserValidations.findById(validation.id) shouldBe null
        }

        "signup" {
            val validation = UserService.signup("anew@ktapi.org")!!

            validation.email shouldBe "anew@ktapi.org"
            verify {
                EmailService.sendEmailVerification(validation)
            }
        }

        "signup with exising email sends forgot password" {
            val testUser = Users.findById(1)!!

            val validation = UserService.signup(testUser.email)!!

            validation.userId shouldBe testUser.id
            verify {
                EmailService.sendForgotPassword(validation)
            }
        }

        "login" {
            var testUser = Users.findById(1)!!
            testUser.passwordFailure()

            val result = UserService.login(testUser.email, "test")

            testUser = Users.findById(testUser.id)!!
            result.first shouldNotBe null
            result.second shouldNotBe null
            testUser.passwordFailures shouldBe 0
        }

        "login failed" {
            val testUser = Users.findById(1)!!

            val result = UserService.login(testUser.email, "bad")

            result.first shouldNotBe null
            result.first?.locked shouldBe false
            result.second shouldBe null
        }

        "login causes locked account" {
            val testUser = Users.findById(1)!!

            UserService.login(testUser.email, "bad")
            UserService.login(testUser.email, "bad")
            val result = UserService.login(testUser.email, "bad")

            result.first?.locked shouldBe true
        }

        "accept invite" {
            val org = Organizations.create("NewOrg")
            val user = Users.findById(1)!!
            val validation = UserValidations.createForInvite(org.id, UserRole.Admin, user)
            val userLogin = UserLogins.create(1)

            val result = UserService.acceptInvite(userLogin, validation.token)

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

            val result = UserService.acceptInvite(null, validation.token)

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

            val result = UserService.acceptInvite(null, validation.token)

            val orgUser = OrganizationUsers.findByUserIdAndOrganizationId(user.id, org.id)
            UserValidations.findById(validation.id) shouldBe null
            orgUser shouldNotBe null
            orgUser?.role shouldBe UserRole.Admin
            result shouldNotBe null
        }

        "accept invite uses existing user if one exists" {
            val user = Users.create("mynew@email.com")!!
            val validation = UserValidations.createForInvite(1, UserRole.User, user.email)

            val result = UserService.acceptInvite(null, validation.token)

            result?.userId shouldBe user.id
        }

        "accept invite creates new user if one does not exist" {
            val validation = UserValidations.createForInvite(1, UserRole.User, "yetanother@email.com")

            val result = UserService.acceptInvite(null, validation.token)

            result shouldNotBe null
            Users.findById(result?.userId)!!.email shouldBe "yetanother@email.com"
        }

        "accept invite doesn't work with outdated invite" {
            val validation = UserValidations.createForInvite(1, UserRole.Admin, "my@email.com")
            UserValidations.setCreatedAt(validation.id, 14.daysAgo())

            val result = UserService.acceptInvite(null, validation.token)

            result shouldBe null
            UserValidations.findById(validation.id) shouldBe null
        }
    }

    override val objectMocks = listOf(EmailService)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        every {
            EmailService.sendForgotPassword(any())
            EmailService.sendEmailVerification(any())
            EmailService.sendUserInvite(any(), any(), any())
        } just Runs
    }
}
