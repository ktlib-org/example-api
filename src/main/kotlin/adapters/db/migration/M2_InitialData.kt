package adapters.db.migration

import adapters.PerformanceRunner
import entities.Jobs
import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.Users
import entities.user.Users.update
import org.ktlib.Encryption
import org.ktlib.Environment
import org.ktlib.db.KotlinMigration
import org.ktlib.toQualifiedName

class M2_InitialData : KotlinMigration() {
    override fun migrate() {
        Jobs.create(
            name = "UpdatePerformanceTotals",
            function = PerformanceRunner::loadRecentAndProcess.toQualifiedName(),
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
