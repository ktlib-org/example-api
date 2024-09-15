package infrastructure

import infrastructure.db.JobTable
import org.ktlib.Bootstrap
import org.ktlib.db.ktorm.Table
import org.ktlib.db.ktorm.Ktorm
import org.ktlib.email.Email
import org.ktlib.email.SendGrid
import org.ktlib.instancesFromFilesRelativeToClass
import org.ktlib.registerInstanceFactory

object Bootstrap : Bootstrap {
    override fun init() {
        Ktorm.registerEntityTables(instancesFromFilesRelativeToClass<JobTable, Table<*, *>>())

        registerInstanceFactory<Email> { SendGrid }
    }
}