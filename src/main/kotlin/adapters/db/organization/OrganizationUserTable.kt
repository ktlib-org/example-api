package adapters.db.organization

import adapters.db.EntityKtormWithOrganization
import adapters.db.EntityWithOrganizationTable
import entities.organization.OrganizationUser
import entities.organization.OrganizationUserStore
import entities.organization.UserRole
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.schema.enum
import org.ktorm.schema.varchar

interface OrganizationUserKtorm : EntityKtormWithOrganization<OrganizationUserKtorm>, OrganizationUser

object OrganizationUserTable :
    EntityWithOrganizationTable<OrganizationUserKtorm, OrganizationUser>("organization_user"),
    OrganizationUserStore {
    val userId = varchar("user_id").bindTo { it.userId }
    val role = enum<UserRole>("role").bindTo { it.role }

    override fun create(organizationId: String, userId: String, role: UserRole): OrganizationUser {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.userId, userId)
            set(it.organizationId, organizationId)
            set(it.role, role)
        }
        return findById(id)!!
    }

    override fun updateRole(id: String, role: UserRole) = update {
        set(it.role, role)
        where { it.id eq id }
    }

    override fun hasOneOwner(organizationId: String) =
        count { (it.organizationId eq organizationId) and (role eq UserRole.Owner) } < 2

    override fun findByUserId(userId: String) = findList { it.userId eq userId }

    override fun findByUserIds(userIds: List<String>) = findList { userId inList userIds }

    override fun findByUserIdAndOrganizationId(userId: String, organizationId: String) =
        findOne { (it.userId eq userId) and (it.organizationId eq organizationId) }

    override fun userBelongsToOrganization(userId: String, organizationId: String) =
        count { (it.userId eq userId) and (it.organizationId eq organizationId) } > 0
}