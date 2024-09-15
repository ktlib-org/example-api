package infrastructure.db.user

import domain.entities.organization.UserRole
import domain.entities.user.User
import domain.entities.user.UserValidation
import domain.entities.user.UserValidationRepo
import org.ktlib.db.ktorm.*
import org.ktlib.sha512
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.enum
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*

interface UserValidationKtorm : EntityKtorm<UserValidationKtorm>, UserValidation

object UserValidationTable :
    Table<UserValidationKtorm, UserValidation>("user_validation"),
    UserValidationRepo {
    val token = varchar("token").bindTo { it.token }
    val firstName = varchar("first_name").bindTo { it.firstName }
    val lastName = varchar("last_name").bindTo { it.lastName }
    val email = varchar("email").bindTo { it.email }
    val userId = uuid("user_id").bindTo { it.userId }
    val organizationId = uuid("organization_id").bindTo { it.organizationId }
    val role = enum<UserRole>("role").bindTo { it.role }

    override fun createForEmailValidation(email: String, firstName: String, lastName: String): UserValidation {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.firstName, firstName)
            set(it.lastName, lastName)
            set(it.email, email.lowercase())
            set(token, createToken(email))
        }
        return findById(id)!!
    }

    private fun createToken(email: String) = "$email-${System.currentTimeMillis()}-${Math.random()}".sha512()

    override fun createForForgotPassword(user: User): UserValidation {
        val id = generateId()
        insert {
            set(it.id, id)
            set(firstName, user.firstName)
            set(lastName, user.lastName)
            set(email, user.email)
            set(userId, user.id)
            set(token, createToken(user.email))
        }
        return findById(id)!!
    }

    override fun createForInvite(organizationId: UUID, role: UserRole, user: User): UserValidation {
        val id = generateId()
        insert {
            set(it.id, id)
            set(firstName, user.firstName)
            set(lastName, user.lastName)
            set(email, user.email)
            set(userId, user.id)
            set(token, createToken(user.email))
            set(it.organizationId, organizationId)
            set(it.role, role)
        }
        return findById(id)!!
    }

    override fun createForInvite(
        organizationId: UUID,
        role: UserRole,
        email: String,
        firstName: String,
        lastName: String
    ): UserValidation {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.firstName, firstName)
            set(it.lastName, lastName)
            set(it.email, email.lowercase())
            set(token, createToken(email))
            set(it.organizationId, organizationId)
            set(it.role, role)
        }
        return findById(id)!!
    }

    override fun findByToken(token: String) = findOne { it.token eq token }

    override fun findByOrganization(organizationId: UUID) = findList { it.organizationId eq organizationId }

    override fun findByOrganizationIdAndId(organizationId: UUID, id: UUID) =
        findOne { (it.organizationId eq organizationId) and (it.id eq id) }
}