package service

import model.OrganizationUsers
import model.UserRole
import model.user.*
import org.ktapi.Encryption
import org.ktapi.db.transaction
import org.ktapi.model.Validation.check
import org.ktapi.model.Validation.field
import org.ktapi.model.Validation.lengthAtLeast
import org.ktapi.model.Validation.validEmailDomain
import org.ktapi.model.Validation.validate
import org.ktapi.model.Validation.validateField
import org.ktapi.model.populateFrom
import org.ktapi.slack.Slack
import org.ktapi.slack.WebhookMessage

object UserService {
    data class UserUpdate(val firstName: String? = null, val lastName: String? = null, val email: String?)

    fun update(user: User, data: Map<String, Any>) {
        user.populateFrom(data, UserUpdate::class)
        validate { validateEmail(user.id, user.email) }
        user.flushChanges()
    }

    fun updatePassword(userId: Long, password: String) {
        validateField("password", password) { lengthAtLeast(8) }
        Users.updatePassword(userId, password)
    }

    fun hasPermission(orgId: Long?, userId: Long?, roles: List<UserRole>): Boolean {
        if (userId == null || orgId == null) return false

        val userOrg = OrganizationUsers.findByUserIdAndOrganizationId(userId, orgId)

        return userOrg != null && roles.any { userOrg.role >= it }
    }

    fun verifyEmail(token: String) = transaction {
        UserValidations.findByToken(token)?.let { validation ->
            validation.delete()

            if (!validation.isValid) {
                null
            } else {
                val user =
                    Users.create(
                        email = validation.email,
                        firstName = validation.firstName,
                        lastName = validation.lastName
                    ) ?: Users.findByEmail(validation.email)!!

                UserLogins.create(user.id)
            }
        }
    }

    fun forgotPassword(email: String?) = transaction {
        Users.findByEmail(email)?.let { user ->
            user.passwordSet = false
            user.flushChanges()

            val validation = UserValidations.createForForgotPassword(user)
            EmailService.sendForgotPassword(validation)

            validation
        }

    }

    fun tokenLogin(token: String): UserLogin? {
        val validation = UserValidations.findByToken(token) ?: return null

        validation.delete()

        if (!validation.isValid || validation.userId == null) return null

        return UserLogins.create(validation.userId!!)
    }

    fun signup(email: String, firstName: String = "", lastName: String = "") = transaction {
        val user = Users.findByEmail(email)

        if (user == null) {
            validate { validateEmail(null, email) }
            val validation = UserValidations.createForEmailValidation(email, firstName, lastName)

            EmailService.sendEmailVerification(validation)

            validation
        } else {
            forgotPassword(email)
        }
    }

    private fun validateEmail(userId: Long?, email: String) = field("email", email) {
        check("Email in use") { Users.findByEmail(email)?.id == userId }
        validEmailDomain()
    }

    fun login(email: String, password: String): Pair<User?, UserLogin?> {
        val user = Users.findByEmail(email) ?: return Pair(null, null)

        return when {
            user.locked -> Pair(user, null)
            Encryption.passwordMatches(password, user.password) -> {
                user.clearPasswordFailures()
                Pair(user, UserLogins.create(user.id))
            }

            else -> {
                user.passwordFailure()
                Pair(user, null)
            }
        }
    }

    fun acceptInvite(userLogin: UserLogin?, token: String) = transaction {
        val validation = UserValidations.findByToken(token)

        validation?.delete()

        if (validation?.isValid != true) {
            null
        } else {
            var userId =
                userLogin?.userId ?: Users.findByEmail(validation.email)?.id

            if (userId == null) {
                userId = Users.create(validation)!!.id
                Slack.sendMessage(WebhookMessage("User account created for email: ${validation.email}"))
            }

            val currentRole = OrganizationUsers.findByUserIdAndOrganizationId(userId, validation.organizationId!!)

            if (currentRole == null) {
                OrganizationUsers.create(validation.organizationId!!, userId, validation.role!!)
            } else if (currentRole.role < validation.role!!) {
                OrganizationUsers.updateRole(currentRole.id, validation.role!!)
            }

            userLogin ?: UserLogins.create(userId)
        }
    }
}