package services

import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.UserValidations
import entities.user.Users
import io.kotlintest.TestCase
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.ktapi.Application.Email
import org.ktapi.config
import org.ktapi.test.DbStringSpec
import org.ktapi.urlEncode

class EmailServiceTests : DbStringSpec() {
    private val url = config<String>("web.url")

    init {
        "sendEmailVerification" {
            val validation = UserValidations.createForEmailValidation("my@email.com")

            EmailService.sendEmailVerification(validation)

            verify {
                Email.send(
                    template = "fakeVerificationTemplate",
                    to = validation.toEmailData(),
                    data = mapOf("url" to "$url/?action=verifyEmail&token=${validation.token.urlEncode()}"),
                    from = any()
                )
            }
        }

        "sendForgotPassword" {
            val validation = UserValidations.createForForgotPassword(Users.findById(1)!!)

            EmailService.sendForgotPassword(validation)

            verify {
                Email.send(
                    template = "fakeForgotPasswordTemplate",
                    to = validation.toEmailData(),
                    data = mapOf("url" to "$url/?action=resetPassword&token=${validation.token.urlEncode()}"),
                    from = any()
                )
            }
        }

        "sendUserInvite" {
            val user = Users.findById(1)!!
            val validation = UserValidations.createForInvite(1, UserRole.User, user)
            val org = Organizations.findById(1)!!

            EmailService.sendUserInvite(org, user, validation)

            verify {
                Email.send(
                    template = "fakeUserInviteTemplate",
                    to = validation.toEmailData(),
                    data = mapOf(
                        "url" to "$url/?action=acceptInvite&token=${validation.token.urlEncode()}",
                        "organization" to org.name,
                        "invitedBy" to user.fullName
                    ),
                    from = any()
                )
            }
        }
    }

    override val objectMocks = listOf(Email)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        every {
            Email.send(any(), any(), any(), any(), any(), any())
        } just Runs
    }
}