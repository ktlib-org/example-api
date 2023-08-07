package entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import org.ktapi.Encryption
import org.ktapi.email.EmailData
import org.ktapi.entities.*
import org.ktapi.entities.Validation.check
import org.ktapi.entities.Validation.field
import org.ktapi.entities.Validation.validEmailDomain
import org.ktapi.entities.Validation.validate
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface UserData : WithDates {
    var firstName: String
    var lastName: String
    var email: String
}

interface User : EntityWithDates<User>, UserData {
    companion object : Entity.Factory<User>()

    @get:JsonIgnore
    var enabled: Boolean

    @get:JsonIgnore
    var locked: Boolean

    @get:JsonIgnore
    var password: String

    @get:JsonIgnore
    var employee: Boolean

    @get:JsonIgnore
    var passwordSet: Boolean

    @get:JsonIgnore
    var passwordFailures: Int

    fun passwordMatches(password: String) = Encryption.passwordMatches(password, this.password)

    fun toEmailData() = EmailData(email, fullName.ifBlank { null })

    fun validate() = validate {
        field("email") {
            check("Email in use") { Users.findByEmail(email)?.id == id }
            validEmailDomain()
        }
    }

    fun passwordFailure() {
        passwordFailures++
        locked = passwordFailures >= 3
        flushChanges()
    }

    fun clearPasswordFailures() {
        passwordFailures = 0
        flushChanges()
    }

    val fullName: String
        get() = "$firstName $lastName".trim()

    val roles: List<OrganizationUser>
        get() = lazyLoad("roles") { OrganizationUsers.findByUserId(id) }
}

fun List<User>.preloadRoles() = preloadOneToMany(
    this, "roles", { OrganizationUsers.findByUserIds(ids()) }, { one, many -> many.filter { it.userId == one.id } }
)

object Users : EntityWithDatesTable<User>("user") {
    val firstName = varchar("first_name").bindTo { it.firstName }
    val lastName = varchar("last_name").bindTo { it.lastName }
    val email = varchar("email").bindTo { it.email }
    val password = varchar("password").bindTo { it.password }
    val enabled = boolean("enabled").bindTo { it.enabled }
    val employee = boolean("employee").bindTo { it.employee }
    val locked = boolean("locked").bindTo { it.locked }
    val passwordFailures = int("password_failures").bindTo { it.passwordFailures }
    val passwordSet = boolean("password_set").bindTo { it.passwordSet }

    fun findByEmail(email: String?) = if (email == null) null else findOne { Users.email eq email.lowercase() }

    fun create(validation: UserValidation) =
        create(email = validation.email, firstName = validation.firstName, lastName = validation.lastName)

    fun create(email: String, password: String? = null, firstName: String = "", lastName: String = ""): User? {
        val lowerEmail = email.lowercase()
        val newPassword = password ?: Encryption.generateKey(20)

        return when (findByEmail(lowerEmail)) {
            null -> {
                insert {
                    set(Users.firstName, firstName)
                    set(Users.lastName, lastName)
                    set(Users.email, lowerEmail)
                    set(Users.password, Encryption.hashPassword(newPassword))
                    set(passwordSet, password != null)
                }
                findByEmail(lowerEmail)
            }

            else -> null
        }
    }

    fun updatePassword(id: Long, password: String) {
        update {
            set(Users.password, Encryption.hashPassword(password))
            set(passwordSet, true)
            set(locked, false)
            set(passwordFailures, 0)
            where { it.id eq id }
        }
    }
}