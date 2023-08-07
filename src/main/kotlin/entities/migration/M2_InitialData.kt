package entities.migration

import entities.Jobs
import entities.organization.OrganizationUsers
import entities.organization.Organizations
import entities.organization.UserRole
import entities.user.Users
import org.ktapi.db.KotlinMigration
import org.ktapi.toQualifiedName
import services.PerformanceService

class M2_InitialData : KotlinMigration() {
    override fun migrate() {
        Jobs.create(
            name = "UpdatePerformanceTotals",
            function = PerformanceService::loadRecentAndProcess.toQualifiedName(),
            cron = "*/5 * * * *",
            enabled = false
        )

        val user = Users.create("admin@ktapi.org", "test", "Katy", "Api")
        val org = Organizations.create("KtApi")
        OrganizationUsers.create(org.id, user!!.id, UserRole.Owner)
    }

    override fun getChecksum() = 661826309
}
