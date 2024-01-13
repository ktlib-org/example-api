package entities

import entities.organization.Organizations
import entities.user.Users

object TestData {
    val organization = Organizations.all().minBy { it.createdAt }
    val user = Users.all().minBy { it.createdAt }
}