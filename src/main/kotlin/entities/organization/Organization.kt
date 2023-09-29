package entities.organization

import org.ktlib.entities.Entity
import org.ktlib.entities.EntityStore
import org.ktlib.entities.Factory
import org.ktlib.entities.Validation.field
import org.ktlib.entities.Validation.notBlank
import org.ktlib.entities.Validation.validate
import org.ktlib.lookup

interface Organization : Entity {
    companion object : Factory<Organization>()

    var name: String

    fun validate() = validate {
        field(::name) { notBlank() }
    }
}

object Organizations : OrganizationStore by lookup()

interface OrganizationStore : EntityStore<Organization> {
    fun create(name: String): Organization
}