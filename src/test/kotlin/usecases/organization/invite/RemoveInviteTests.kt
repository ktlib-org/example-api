package usecases.organization.invite

import entities.organization.UserRole
import entities.user.UserValidations
import entities.user.Users
import io.kotlintest.shouldBe
import org.ktapi.test.DbStringSpec

class RemoveInviteTests : DbStringSpec() {
    init {
        "remove invite" {
            val user = Users.findById(1)!!
            val invite = UserValidations.createForInvite(1, UserRole.User, user)

            RemoveInvite.removeInvite(1, invite.id)

            UserValidations.findById(invite.id) shouldBe null
        }
    }
}