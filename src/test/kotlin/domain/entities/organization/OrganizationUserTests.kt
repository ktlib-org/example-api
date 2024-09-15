package domain.entities.organization

import domain.entities.organization.UserRole
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class OrganizationUserTests : StringSpec({
    "roles are comparable" {
        (UserRole.Owner > UserRole.User) shouldBe true
        (UserRole.Owner >= UserRole.User) shouldBe true
        (UserRole.Admin > UserRole.User) shouldBe true
        (UserRole.Admin >= UserRole.User) shouldBe true
        (UserRole.User >= UserRole.User) shouldBe true
        (UserRole.User > UserRole.User) shouldBe false
        (UserRole.User >= UserRole.Admin) shouldBe false
        (UserRole.User >= UserRole.Owner) shouldBe false
        (UserRole.Admin >= UserRole.Owner) shouldBe false
        (UserRole.Owner <= UserRole.User) shouldBe false
    }
})