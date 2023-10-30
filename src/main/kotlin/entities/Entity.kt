package entities

import entities.organization.Organization
import entities.organization.Organizations
import org.ktlib.entities.Entity
import org.ktlib.entities.EntityStore
import org.ktlib.entities.lazyValue
import org.ktlib.entities.preloadLazyValue
import java.util.*

interface EntityWithOrganization : Entity {
    var organizationId: UUID

    val organization: Organization
        get() = lazyValue(::organization) { Organizations.findById(organizationId)!! }
}

interface EntityWithOrganizationStore<T : EntityWithOrganization> : EntityStore<T> {
    fun findByOrganizationId(organizationId: UUID): List<T>
    fun findIdsByOrganizationId(organizationId: UUID): List<UUID>
    fun findByIdAndOrganizationId(id: UUID?, organizationId: UUID): T?
    fun existsByIdAndOrganizationId(id: UUID?, organizationId: UUID) =
        findByIdAndOrganizationId(id, organizationId) != null
}

fun <T : EntityWithOrganization> List<T>.preloadOrganizations() = preloadLazyValue(
    EntityWithOrganization::organization,
    { Organizations.findByIds(this.map { it.organizationId }.distinct()) },
    { one, many -> many.find { it.id == one.organizationId }!! }
)

open class DataWithDates(entity: Entity) {
    val id = entity.id
    val createdAt = entity.createdAt
    val updatedAt = entity.updatedAt
}
