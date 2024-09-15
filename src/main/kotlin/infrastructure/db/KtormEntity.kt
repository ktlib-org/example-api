package infrastructure.db

import domain.entities.EntityWithOrganization
import domain.entities.EntityWithOrganizationRepo
import org.ktlib.db.Database
import org.ktlib.db.ktorm.EntityKtorm
import org.ktlib.db.ktorm.Table
import org.ktlib.db.ktorm.findList
import org.ktlib.db.ktorm.findOne
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.uuid
import java.util.*

interface EntityWithOrganizationKtorm<E : org.ktorm.entity.Entity<E>> : EntityKtorm<E>,
    EntityWithOrganization

abstract class EntityWithOrganizationTable<E : EntityWithOrganizationKtorm<E>, T : EntityWithOrganization>(
    tableName: String,
    alias: String? = null,
) : Table<E, T>(tableName, alias), EntityWithOrganizationRepo<T> {
    val organizationId = uuid("organization_id").bindTo { it.organizationId }

    @Suppress("UNCHECKED_CAST")
    override fun findByOrganizationId(organizationId: UUID) =
        findList { it.organizationId eq organizationId } as List<T>

    override fun findIdsByOrganizationId(organizationId: UUID) =
        Database.queryIds("select id from $tableName where organization_id = ?", Database.param(organizationId))

    @Suppress("UNCHECKED_CAST")
    override fun findByIdAndOrganizationId(id: UUID?, organizationId: UUID) =
        if (id == null) null else findOne { (it.organizationId eq organizationId) and (it.id eq id) } as T?
}
