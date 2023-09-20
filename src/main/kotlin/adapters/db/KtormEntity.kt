package adapters.db

import entities.EntityWithOrganization
import entities.EntityWithOrganizationStore
import org.ktlib.db.Database
import org.ktlib.db.ktorm.KtormEntity
import org.ktlib.db.ktorm.KtormEntityTable
import org.ktlib.db.ktorm.findList
import org.ktlib.db.ktorm.findOne
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.schema.varchar
import kotlin.reflect.KClass

interface KtormEntityWithOrganization<E : org.ktorm.entity.Entity<E>> : KtormEntity<E>,
    EntityWithOrganization

abstract class EntityWithOrganizationTable<E : KtormEntityWithOrganization<E>, T : EntityWithOrganization, S : EntityWithOrganizationStore<T>>(
    tableName: String,
    alias: String? = null,
    entityClass: KClass<E>? = null
) : KtormEntityTable<E, T, S>(tableName, alias, entityClass), EntityWithOrganizationStore<T> {
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
