package usecases.user

import entities.user.User
import org.ktapi.entities.populateFrom

object UpdateUser {
    data class UpdateUserData(val firstName: String? = null, val lastName: String? = null, val email: String?)

    fun update(user: User, data: Map<String, Any>) {
        user.populateFrom(data, UpdateUserData::class).validate()
        user.flushChanges()
    }
}