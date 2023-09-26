package adapters.db.user

import entities.user.User
import entities.user.UserStore
import entities.user.UserValidation
import org.ktlib.Encryption
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.eq
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface UserKtorm : EntityKtorm<UserKtorm>, User

object UserTable : EntityTable<UserKtorm, User>("user"), UserStore {
    val firstName = varchar("first_name").bindTo { it.firstName }
    val lastName = varchar("last_name").bindTo { it.lastName }
    val email = varchar("email").bindTo { it.email }
    val password = varchar("password").bindTo { it.password }
    val enabled = boolean("enabled").bindTo { it.enabled }
    val employee = boolean("employee").bindTo { it.employee }
    val locked = boolean("locked").bindTo { it.locked }
    val passwordFailures = int("password_failures").bindTo { it.passwordFailures }
    val passwordSet = boolean("password_set").bindTo { it.passwordSet }

    override fun all() = findAll()

    override fun findByEmail(email: String?) = if (email == null) null else findOne { it.email eq email.lowercase() }

    override fun create(validation: UserValidation) =
        create(email = validation.email, firstName = validation.firstName, lastName = validation.lastName)

    override fun create(email: String, password: String?, firstName: String, lastName: String): User? {
        val lowerEmail = email.lowercase()

        return when (findByEmail(lowerEmail)) {
            null -> {
                insert {
                    set(it.id, generateId())
                    set(it.firstName, firstName)
                    set(it.lastName, lastName)
                    set(it.email, lowerEmail)
                    set(it.password, password ?: Encryption.generateKey(20))
                    set(passwordSet, password != null)
                }
                findByEmail(lowerEmail)
            }

            else -> null
        }
    }

    override fun updatePassword(id: String, password: String) {
        update {
            set(it.password, password)
            set(passwordSet, true)
            set(locked, false)
            set(passwordFailures, 0)
            where { it.id eq id }
        }
    }
}
