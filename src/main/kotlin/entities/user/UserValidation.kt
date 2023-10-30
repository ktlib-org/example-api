package entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.UserRole
import org.ktlib.email.EmailData
import org.ktlib.entities.Entity
import org.ktlib.entities.EntityStore
import org.ktlib.entities.Factory
import org.ktlib.hoursAgo
import org.ktlib.lookup
import java.util.*

interface UserValidation : Entity {
    companion object : Factory<UserValidation>()

    var firstName: String
    var lastName: String
    var email: String
    var organizationId: UUID?
    var role: UserRole?

    @get:JsonIgnore
    val userId: UUID?

    @get:JsonIgnore
    var token: String

    fun toEmailData() = EmailData(email, fullName.ifBlank { null })

    val fullName: String
        get() = "$firstName $lastName".trim()

    val isEmailValidation: Boolean
        get() = !isInvite && !isForgotPassword

    val isForgotPassword: Boolean
        get() = !isInvite && userId != null

    val isInvite: Boolean
        get() = organizationId != null && role != null

    val isValid: Boolean
        get() {
            val cutoff = when {
                isInvite -> (7 * 24).hoursAgo()
                else -> 24.hoursAgo()
            }
            return createdAt.isAfter(cutoff)
        }
}

object UserValidations : UserValidationStore by lookup()

interface UserValidationStore : EntityStore<UserValidation> {
    fun findByToken(token: String): UserValidation?
    fun findByOrganization(organizationId: UUID): List<UserValidation>
    fun findByOrganizationIdAndId(organizationId: UUID, id: UUID): UserValidation?
    fun createForEmailValidation(email: String, firstName: String = "", lastName: String = ""): UserValidation
    fun createForForgotPassword(user: User): UserValidation
    fun createForInvite(organizationId: UUID, role: UserRole, user: User): UserValidation
    fun createForInvite(
        organizationId: UUID,
        role: UserRole,
        email: String,
        firstName: String = "",
        lastName: String = ""
    ): UserValidation
}
