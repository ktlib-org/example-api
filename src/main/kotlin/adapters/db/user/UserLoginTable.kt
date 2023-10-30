package adapters.db.user

import entities.user.UserLogin
import entities.user.UserLoginStore
import org.ktlib.db.ktorm.*
import org.ktlib.toHexString
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*

interface UserLoginKtorm : EntityKtorm<UserLoginKtorm>, UserLogin

object UserLoginTable : EntityTable<UserLoginKtorm, UserLogin>("user_login"),
    UserLoginStore {
    val token = varchar("token").bindTo { it.token }
    val userId = uuid("user_id").bindTo { it.userId }
    val parentId = uuid("parent_id").bindTo { it.parentId }
    val valid = boolean("valid").bindTo { it.valid }

    override fun create(userId: UUID, parentId: UUID?): UserLogin {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.userId, userId)
            set(it.parentId, parentId)
            set(token, generateId().toHexString())
        }
        return findById(id)!!
    }

    override fun findByToken(token: String?) =
        if (token == null) null else findOne { (it.token eq token) and valid }

    override fun findRecent() = findList { valid }
}