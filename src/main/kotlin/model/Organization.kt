package model

import org.ktapi.model.EntityWithDates
import org.ktapi.model.EntityWithDatesTable
import org.ktapi.model.Validation.field
import org.ktapi.model.Validation.notBlank
import org.ktapi.model.Validation.validate
import org.ktapi.model.WithDates
import org.ktorm.entity.Entity
import org.ktorm.schema.varchar

interface OrganizationData : WithDates {
    var name: String
}

interface Organization : EntityWithDates<Organization>, OrganizationData {
    companion object : Entity.Factory<Organization>()

    fun validate() = validate {
        field("name") { notBlank() }
    }
}

object Organizations : EntityWithDatesTable<Organization>("organization") {
    val name = varchar("name").bindTo { it.name }

    fun create(name: String) = Organization {
        this.name = name
    }.create()
}