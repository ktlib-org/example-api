package entities

import entities.organization.Organizations
import entities.user.Users
import org.ktlib.entities.EntityInitializer

object TestData {
    init {
        EntityInitializer.init()
    }

    val organization = Organizations.all().minBy { it.createdAt }
    val user = Users.all().minBy { it.createdAt }
}