package services

import entities.organization.Organization
import entities.user.User
import entities.user.UserValidation
import org.ktapi.Application.Email
import org.ktapi.lazyConfig
import org.ktapi.urlEncode

object EmailService {
    private val emailVerificationTemplate by lazyConfig<String>("email.verificationTemplate")
    private val forgotPasswordTemplate by lazyConfig<String>("email.forgotPasswordTemplate")
    private val userInviteTemplate by lazyConfig<String>("email.userInviteTemplate")
    private val webAppUrl by lazyConfig<String>("web.url")

    fun sendEmailVerification(validation: UserValidation) {
        val url = "$webAppUrl/?action=verifyEmail&token=${validation.token.urlEncode()}"
        Email.send(template = emailVerificationTemplate, to = validation.toEmailData(), data = mapOf("url" to url))
    }

    fun sendForgotPassword(validation: UserValidation) {
        val url = "$webAppUrl/?action=resetPassword&token=${validation.token.urlEncode()}"
        Email.send(template = forgotPasswordTemplate, to = validation.toEmailData(), data = mapOf("url" to url))
    }

    fun sendUserInvite(organization: Organization, invitingUser: User, validation: UserValidation) {
        val url = "$webAppUrl/?action=acceptInvite&token=${validation.token.urlEncode()}"
        Email.send(
            template = userInviteTemplate,
            to = validation.toEmailData(),
            data = mapOf("url" to url, "organization" to organization.name, "invitedBy" to invitingUser.fullName)
        )
    }
}