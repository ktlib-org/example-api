package usecases

import org.ktlib.lazyConfig

object UseCaseConfig {
    val webAppUrl by lazyConfig<String>("web.appUrl")

    val emailVerificationTemplate by lazyConfig<String>("email.verificationTemplate")
    val forgotPasswordTemplate by lazyConfig<String>("email.forgotPasswordTemplate")
    val userInviteTemplate by lazyConfig<String>("email.userInviteTemplate")
}