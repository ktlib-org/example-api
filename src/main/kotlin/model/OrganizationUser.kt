package model

import model.user.User
import model.user.Users
import org.ktapi.model.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.entity.Entity
import org.ktorm.schema.enum
import org.ktorm.schema.long

enum class UserRole {
    User, Admin, Owner;
}

interface OrganizationUserData : WithDates {
    val userId: Long
    val organizationId: Long
    val role: UserRole
}

interface OrganizationUser : EntityWithDates<OrganizationUser>, OrganizationUserData {
    companion object : Entity.Factory<OrganizationUser>()

    val user: User
        get() = lazyLoad("user") { Users.findById(userId) }

    val organization: Organization
        get() = lazyLoad("organization") { Organizations.findById(organizationId) }
}

fun List<OrganizationUser>.preloadOrganizations() = preloadOneToOne(
    this,
    "organization",
    { Organizations.findByIds(map { it.organizationId }) },
    { one, many -> many.find { one.organizationId == it.id }!! }
)

fun List<OrganizationUser>.preloadUsers() = preloadOneToOne(
    this,
    "user",
    { Users.findByIds(map { it.userId }) },
    { one, many -> many.find { one.userId == it.id }!! })

object OrganizationUsers : EntityWithDatesTable<OrganizationUser>("organization_user") {
    val userId = long("user_id").bindTo { it.userId }
    val organizationId = long("organization_id").bindTo { it.organizationId }
    val role = enum<UserRole>("role").bindTo { it.role }

    fun create(orgId: Long, userId: Long, role: UserRole): OrganizationUser {
        val id = insertAndGenerateKey {
            set(OrganizationUsers.userId, userId)
            set(organizationId, orgId)
            set(OrganizationUsers.role, role)
        }
        return findById(id as Long)!!
    }

    fun updateRole(id: Long, role: UserRole) = update {
        set(OrganizationUsers.role, role)
        where { it.id eq id }
    }

    fun hasOneOwner(orgId: Long) = count { (organizationId eq orgId) and (role eq UserRole.Owner) } < 2

    fun findByUserId(userId: Long) = findList { OrganizationUsers.userId eq userId }

    fun findByOrganizationId(organizationId: Long) = findList { OrganizationUsers.organizationId eq organizationId }

    fun findByUserIds(userIds: List<Long>) = findList { userId inList userIds }

    fun findByIdAndOrganizationId(id: Long, organizationId: Long) =
        findOne { (it.id eq id) and (OrganizationUsers.organizationId eq organizationId) }

    fun findByUserIdAndOrganizationId(userId: Long, organizationId: Long) =
        findOne { (OrganizationUsers.userId eq userId) and (OrganizationUsers.organizationId eq organizationId) }
}