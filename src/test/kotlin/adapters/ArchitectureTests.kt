package adapters

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import io.kotest.core.spec.style.StringSpec

class ArchitectureTests : StringSpec({
    "architecture layers have correct dependencies" {
        Konsist
            .scopeFromProduction()
            .assertArchitecture {
                val entities = Layer("Entities", "entities..")
                val useCases = Layer("UseCases", "usecases..")
                val services = Layer("Services", "services..")
                val web = Layer("Web", "adapters.web..")
                val db = Layer("Database", "adapters.db..")

                entities.dependsOnNothing()
                services.dependsOn(entities)
                useCases.dependsOn(services, entities)
                web.dependsOn(useCases)
                db.dependsOn(entities)
            }
    }
})