package adapters.db

import org.ktlib.Init
import org.ktlib.db.ktorm.EntityTable
import org.ktlib.db.ktorm.Ktorm
import org.ktlib.entities.EntityInitializer
import org.ktlib.instancesFromFilesRelativeToClass

object EntityInitializer : Init(), EntityInitializer {
    init {
        Ktorm.registerEntityTables(instancesFromFilesRelativeToClass<JobTable, EntityTable<*, *>>())
    }
}