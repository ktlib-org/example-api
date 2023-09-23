package entities.user

import com.fasterxml.jackson.annotation.JsonIgnore
import entities.user.UserLogins.update
import org.ktlib.entities.*
import org.ktlib.lookup


interface UserLogin : Entity {
    companion object : Factory<UserLogin>()

    val userId: String
    val parentId: String?
    val token: String

    @get:JsonIgnore
    var valid: Boolean

    fun invalidate() {
        valid = false
        update()
    }

    val user: User get() = lazyValue(::user) { Users.findById(userId)!! }
}

fun List<UserLogin>.preloadUsers() = preloadLazyValue(
    UserLogin::user,
    { Users.findByIds(map { it.userId }) },
    { one, many -> many.find { one.userId == it.id }!! }
)

object UserLogins : UserLoginStore by lookup()

interface UserLoginStore : EntityStore<UserLogin> {
    fun create(userId: String, parentId: String? = null): UserLogin
    fun findByToken(token: String?): UserLogin?
    fun findRecent(): List<UserLogin>
}
