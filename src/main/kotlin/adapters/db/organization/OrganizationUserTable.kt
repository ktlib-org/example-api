package adapters.db.organization

import adapters.db.EntityWithOrganizationKtorm
import adapters.db.EntityWithOrganizationTable
import entities.organization.OrganizationUser
import entities.organization.OrganizationUserRepo
import entities.organization.UserRole
import org.ktlib.db.ktorm.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.inList
import org.ktorm.schema.enum
import org.ktorm.schema.uuid
import java.util.*

interface OrganizationUserKtorm : EntityWithOrganizationKtorm<OrganizationUserKtorm>, OrganizationUser

object OrganizationUserTable :
    EntityWithOrganizationTable<OrganizationUserKtorm, OrganizationUser>("organization_user"),
    OrganizationUserRepo {
    val userId = uuid("user_id").bindTo { it.userId }
    val role = enum<UserRole>("role").bindTo { it.role }

    override fun create(organizationId: UUID, userId: UUID, role: UserRole): OrganizationUser {
        val id = generateId()
        insert {
            set(it.id, id)
            set(it.userId, userId)
            set(it.organizationId, organizationId)
            set(it.role, role)
        }
        return findById(id)!!
    }

    override fun updateRole(id: UUID, role: UserRole) = update {
        set(it.role, role)
        where { it.id eq id }
    }

    override fun hasOneOwner(organizationId: UUID) =
        count { (it.organizationId eq organizationId) and (role eq UserRole.Owner) } < 2

    override fun findByUserId(userId: UUID) = findList { it.userId eq userId }

    override fun findByUserIds(userIds: List<UUID>) = findList { userId inList userIds }

    override fun findByUserIdAndOrganizationId(userId: UUID, organizationId: UUID) =
        findOne { (it.userId eq userId) and (it.organizationId eq organizationId) }

    override fun userBelongsToOrganization(userId: UUID, organizationId: UUID) =
        count { (it.userId eq userId) and (it.organizationId eq organizationId) } > 0
}