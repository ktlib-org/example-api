package entities.organization

import org.ktapi.entities.EntityWithDates
import org.ktapi.entities.EntityWithDatesTable
import org.ktapi.entities.Validation.field
import org.ktapi.entities.Validation.notBlank
import org.ktapi.entities.Validation.validate
import org.ktapi.entities.WithDates
import org.ktorm.entity.Entity
import org.ktorm.schema.varchar

interface OrganizationData : WithDates {
    var name: String
}

interface Organization : EntityWithDates<Organization>, OrganizationData {
    companion object : Entity.Factory<Organization>()

    fun validate() = validate {
        field(::name) { notBlank() }
    }
}

object Organizations : EntityWithDatesTable<Organization>("organization") {
    val name = varchar("name").bindTo { it.name }

    fun create(name: String) = Organization {
        this.name = name
    }.create()
}