package infrastructure.web.routes

import infrastructure.web.WebServer
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.ktlib.fromJson

class StatusRouterTests : StringSpec({
    "status" {
        WebServer.test { _, client ->
            val result = client.get("/status").body?.string()?.fromJson<SystemRouter.StatusResult>()

            result?.up shouldBe true
        }
    }
})