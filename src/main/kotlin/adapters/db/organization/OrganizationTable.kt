package adapters.db.organization

import entities.organization.Organization
import entities.organization.OrganizationStore
import org.ktlib.db.ktorm.EntityKtorm
import org.ktlib.db.ktorm.EntityTable
import org.ktlib.db.ktorm.findAll
import org.ktorm.schema.varchar

interface OrganizationKtorm : EntityKtorm<OrganizationKtorm>, Organization

object OrganizationTable : EntityTable<OrganizationKtorm, Organization>("organization"),
    OrganizationStore {
    val name = varchar("name").bindTo { it.name }

    override fun create(name: String) = Organization {
        this.name = name
    }.create()

    override fun all() = findAll()
}