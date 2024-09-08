package entities.organization

import org.ktlib.entities.Entity
import org.ktlib.entities.Repository
import org.ktlib.entities.Factory
import org.ktlib.entities.Validation.field
import org.ktlib.entities.Validation.notBlank
import org.ktlib.entities.Validation.validate
import org.ktlib.lookupInstance

interface Organization : Entity {
    companion object : Factory<Organization>()

    var name: String

    fun validate() = validate {
        field(::name) { notBlank() }
    }
}

object Organizations : OrganizationRepo by lookupInstance()

interface OrganizationRepo : Repository<Organization> {
    fun create(name: String): Organization
}