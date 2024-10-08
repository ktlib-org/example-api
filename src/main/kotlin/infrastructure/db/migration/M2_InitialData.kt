package infrastructure.db.migration

import domain.entities.Jobs
import domain.entities.organization.OrganizationUsers
import domain.entities.organization.Organizations
import domain.entities.organization.UserRole
import domain.entities.user.Users
import domain.entities.user.Users.update
import org.ktlib.Encryption
import org.ktlib.Environment
import org.ktlib.db.KotlinMigration

class M2_InitialData : KotlinMigration() {
    override fun migrate() {
        Jobs.create(
            name = "UpdatePerformanceTotals",
            function = "services.PerformanceRunner.loadRecentAndProcess",
            cron = "*/5 * * * *",
            enabled = false
        )

        createOrg("BaseKotlin", "admin@test.com", "Base", "Kotlin")

        if (Environment.isLocal) {
            createOrg("Local Organization", "local@test.com", "Local", "User")
        }
    }

    private fun createOrg(name: String, userEmail: String, firstName: String, lastName: String) {
        val user = Users.create(userEmail, Encryption.hashPassword("test"), firstName, lastName)!!
        user.employee = true
        user.update()
        val org = Organizations.create(name)
        OrganizationUsers.create(org.id, user.id, UserRole.Owner)
    }

    override fun getChecksum() = 661826309
}
