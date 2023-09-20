package adapters.db.organization

import entities.organization.Organization
import entities.organization.OrganizationStore
import org.ktlib.db.ktorm.KtormEntity
import org.ktlib.db.ktorm.KtormEntityTable
import org.ktlib.db.ktorm.findAll
import org.ktorm.schema.varchar

interface OrganizationKtorm : KtormEntity<OrganizationKtorm>, Organization

object OrganizationTable : KtormEntityTable<OrganizationKtorm, Organization, OrganizationStore>("organization"),
    OrganizationStore {
    val name = varchar("name").bindTo { it.name }

    override fun create(name: String) = Organization {
        this.name = name
    }.create()

    override fun all() = findAll()
}