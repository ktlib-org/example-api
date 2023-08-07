package entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.UserRole
import org.ktapi.email.EmailData
import org.ktapi.entities.*
import org.ktapi.hoursAgo
import org.ktapi.sha512
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.schema.enum
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface UserValidationData : WithDates {
    val firstName: String
    val lastName: String
    val email: String
    val organizationId: Long?
    val role: UserRole?
}

interface UserValidation : EntityWithDates<UserValidation>, UserValidationData {
    companion object : Entity.Factory<UserValidation>()

    @get:JsonIgnore
    val userId: Long?

    @get:JsonIgnore
    val token: String

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

object UserValidations : EntityWithDatesTable<UserValidation>("user_validation") {
    val token = varchar("token").bindTo { it.token }
    val firstName = varchar("first_name").bindTo { it.firstName }
    val lastName = varchar("last_name").bindTo { it.lastName }
    val email = varchar("email").bindTo { it.email }
    val userId = long("user_id").bindTo { it.userId }
    val organizationId = long("organization_id").bindTo { it.organizationId }
    val role = enum<UserRole>("role").bindTo { it.role }

    fun createForEmailValidation(email: String, firstName: String = "", lastName: String = ""): UserValidation {
        val id = insertAndGenerateKey {
            set(UserValidations.firstName, firstName)
            set(UserValidations.lastName, lastName)
            set(UserValidations.email, email.lowercase())
            set(token, createToken(email))
        }
        return findById(id as Long)!!
    }

    private fun createToken(email: String) = "$email-${System.currentTimeMillis()}-${Math.random()}".sha512()

    fun createForForgotPassword(user: User): UserValidation {
        val id = insertAndGenerateKey {
            set(firstName, user.firstName)
            set(lastName, user.lastName)
            set(email, user.email)
            set(userId, user.id)
            set(token, createToken(user.email))
        }
        return findById(id as Long)!!
    }

    fun createForInvite(organizationId: Long, role: UserRole, user: User): UserValidation {
        val id = insertAndGenerateKey {
            set(firstName, user.firstName)
            set(lastName, user.lastName)
            set(email, user.email)
            set(userId, user.id)
            set(token, createToken(user.email))
            set(UserValidations.organizationId, organizationId)
            set(UserValidations.role, role)
        }
        return findById(id as Long)!!
    }

    fun createForInvite(
        organizationId: Long,
        role: UserRole,
        email: String,
        firstName: String = "",
        lastName: String = ""
    ): UserValidation {
        val id = insertAndGenerateKey {
            set(UserValidations.firstName, firstName)
            set(UserValidations.lastName, lastName)
            set(UserValidations.email, email.lowercase())
            set(token, createToken(email))
            set(UserValidations.organizationId, organizationId)
            set(UserValidations.role, role)
        }
        return findById(id as Long)!!
    }

    fun findByToken(token: String) = findOne { UserValidations.token eq token }

    fun findByOrganization(orgId: Long) = findList { organizationId eq orgId }

    fun findByOrganizationIdAndId(orgId: Long, id: Long) = findOne { (organizationId eq orgId) and (it.id eq id) }
}