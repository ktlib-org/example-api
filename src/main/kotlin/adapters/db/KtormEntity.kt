package adapters.db

import entities.EntityWithOrganization
import entities.EntityWithOrganizationStore
import org.ktlib.db.Database
import org.ktlib.db.ktorm.EntityKtorm
import org.ktlib.db.ktorm.EntityTable
import org.ktlib.db.ktorm.findList
import org.ktlib.db.ktorm.findOne
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.varchar

interface EntityKtormWithOrganization<E : org.ktorm.entity.Entity<E>> : EntityKtorm<E>,
    EntityWithOrganization

abstract class EntityWithOrganizationTable<E : EntityKtormWithOrganization<E>, T : EntityWithOrganization>(
    tableName: String,
    alias: String? = null,
) : EntityTable<E, T>(tableName, alias), EntityWithOrganizationStore<T> {
    val organizationId = varchar("organization_id").bindTo { it.organizationId }

    @Suppress("UNCHECKED_CAST")
    override fun findByOrganizationId(organizationId: String) =
        findList { it.organizationId eq organizationId } as List<T>

    override fun findIdsByOrganizationId(organizationId: String) =
        Database.queryIds("select id from $tableName where organization_id = ?", Database.param(organizationId))

    @Suppress("UNCHECKED_CAST")
    override fun findByIdAndOrganizationId(id: String?, organizationId: String) =
        if (id == null) null else findOne { (it.organizationId eq organizationId) and (it.id eq id) } as T?
}
