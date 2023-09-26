package adapters.db.user

import entities.user.UserLogin
import entities.user.UserLoginStore
import org.ktlib.db.IdGenerator
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.boolean
import org.ktorm.schema.varchar

interface UserLoginKtorm : EntityKtorm<UserLoginKtorm>, UserLogin

object UserLoginTable : EntityTable<UserLoginKtorm, UserLogin>("user_login"),
    UserLoginStore {
    val token = varchar("token").bindTo { it.token }
    val userId = varchar("user_id").bindTo { it.userId }
    val parentId = varchar("parent_id").bindTo { it.parentId }
    val valid = boolean("valid").bindTo { it.valid }

    override fun create(userId: String, parentId: String?): UserLogin {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.userId, userId)
            set(it.parentId, parentId)
            set(token, IdGenerator.generate(50))
        }
        return findById(id)!!
    }

    override fun findByToken(token: String?) =
        if (token == null) null else findOne { (it.token eq token) and valid }

    override fun findRecent() = findList { valid }
}