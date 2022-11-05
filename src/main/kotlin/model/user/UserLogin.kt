package model.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ktapi.Encryption
import org.ktapi.model.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.schema.boolean
import org.ktorm.schema.long
import org.ktorm.schema.varchar

interface UserLoginData : WithDates {
    val userId: Long
    val parentId: Long?
    val token: String
}

interface UserLogin : EntityWithDates<UserLogin>, UserLoginData {
    companion object : Entity.Factory<UserLogin>()

    @get:JsonIgnore
    var valid: Boolean

    fun invalidate() {
        valid = false
        flushChanges()
    }

    val user: User
        get() = lazyLoad(Users, userId)!!
}

fun List<UserLogin>.preloadUsers() = preload(this, Users, "userId")

object UserLogins : EntityWithDatesTable<UserLogin>("user_login") {
    val token = varchar("token").bindTo { it.token }
    val userId = long("user_id").bindTo { it.userId }
    val parentId = long("parent_id").bindTo { it.parentId }
    val valid = boolean("valid").bindTo { it.valid }

    fun create(userId: Long, parentId: Long? = null): UserLogin {
        val id = insertAndGenerateKey {
            set(UserLogins.userId, userId)
            set(UserLogins.parentId, parentId)
            set(token, Encryption.generateKey(50))
        } as Long
        return findById(id)!!
    }

    fun findByToken(token: String?) = if (token == null) null else findOne { (UserLogins.token eq token) and valid }

    fun findRecent() = findList { valid }
}