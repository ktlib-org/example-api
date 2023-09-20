package entities

import entities.organization.Organization
import entities.organization.Organizations
import org.ktlib.entities.Entity
import org.ktlib.entities.EntityStore
import org.ktlib.entities.lazyAssociation
import org.ktlib.entities.preloadLazyAssociation

interface EntityWithOrganization : Entity {
    var organizationId: String

    val organization: Organization
        get() = lazyAssociation(::organization) { Organizations.findById(organizationId)!! }
}

interface EntityWithOrganizationStore<T : EntityWithOrganization> : EntityStore<T> {
    fun findByOrganizationId(organizationId: String): List<T>
    fun findIdsByOrganizationId(organizationId: String): List<String>
    fun findByIdAndOrganizationId(id: String?, organizationId: String): T?
    fun existsByIdAndOrganizationId(id: String?, organizationId: String) =
        findByIdAndOrganizationId(id, organizationId) != null
}

fun <T : EntityWithOrganization> List<T>.preloadOrganizations() = preloadLazyAssociation(
    EntityWithOrganization::organization,
    { Organizations.findByIds(this.map { it.organizationId }.distinct()) },
    { one, many -> many.find { it.id == one.organizationId }!! }
)

open class DataWithDates(entity: Entity) {
    val id = entity.id
    val createdAt = entity.createdAt
    val updatedAt = entity.updatedAt
}
