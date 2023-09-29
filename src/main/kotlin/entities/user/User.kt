package entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.organization.OrganizationUser
import entities.organization.OrganizationUsers
import entities.user.Users.update
import org.ktlib.Encryption
import org.ktlib.email.EmailData
import org.ktlib.entities.*
import org.ktlib.entities.Validation.check
import org.ktlib.entities.Validation.field
import org.ktlib.entities.Validation.validEmailDomain
import org.ktlib.entities.Validation.validate
import org.ktlib.lookup

interface User : Entity {
    companion object : Factory<User>()

    var firstName: String
    var lastName: String
    var email: String

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
        field(::email) {
            check("Email in use") {
                val user = Users.findByEmail(email)
                user == null || user.id == id
            }
            validEmailDomain()
        }
    }

    fun passwordFailure() {
        passwordFailures++
        locked = passwordFailures >= 3
        update()
    }

    fun clearPasswordFailures() {
        passwordFailures = 0
        update()
    }

    val fullName: String get() = "$firstName $lastName".trim()

    val roles: List<OrganizationUser> get() = lazyValue(::roles) { OrganizationUsers.findByUserId(id) }
}

fun List<User>.preloadRoles() = preloadLazyList(
    User::roles,
    { OrganizationUsers.findByUserIds(ids()) },
    { one, many -> many.filter { it.userId == one.id } }
)

object Users : UserStore by lookup()

interface UserStore : EntityStore<User> {
    fun findByEmail(email: String?): User?

    fun create(validation: UserValidation) =
        create(email = validation.email, firstName = validation.firstName, lastName = validation.lastName)

    fun create(email: String, password: String? = null, firstName: String = "", lastName: String = ""): User?
    fun updatePassword(id: String, password: String)
}
