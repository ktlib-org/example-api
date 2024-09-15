package infrastructure

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import io.kotest.core.spec.style.StringSpec

class ArchitectureTests : StringSpec({
    "architecture layers have correct dependencies" {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val entities = Layer("Entities", "domain.entities..")
                val services = Layer("Services", "domain.services..")
                val useCases = Layer("UseCases", "usecases..")
                val web = Layer("Web", "infrastructure.web..")
                val db = Layer("Database", "infrastructure.db..")

                entities.dependsOnNothing()
                services.dependsOn(entities)
                useCases.dependsOn(services, entities)
                web.dependsOn(useCases)
                db.dependsOn(entities)
            }
    }
})