package domain.entities

import domain.entities.organization.Organizations
import domain.entities.user.Users

object TestData {
    val organization = Organizations.all().minBy { it.createdAt }
    val user = Users.all().minBy { it.createdAt }
}