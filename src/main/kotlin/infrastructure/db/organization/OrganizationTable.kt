package infrastructure.db.organization

import domain.entities.organization.Organization
import domain.entities.organization.OrganizationRepo
import org.ktlib.db.ktorm.EntityKtorm
import org.ktlib.db.ktorm.Table
import org.ktlib.db.ktorm.findAll
import org.ktorm.schema.varchar

interface OrganizationKtorm : EntityKtorm<OrganizationKtorm>, Organization

object OrganizationTable : Table<OrganizationKtorm, Organization>("organization"),
    OrganizationRepo {
    val name = varchar("name").bindTo { it.name }

    override fun create(name: String) = Organization {
        this.name = name
    }.create()

    override fun all() = findAll()
}