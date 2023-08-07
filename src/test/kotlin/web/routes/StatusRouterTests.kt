package web.routes

import io.kotlintest.shouldBe
import org.ktapi.fromJson
import org.ktapi.test.StringSpec
import web.WebServer

class StatusRouterTests : StringSpec({
    "status" {
        WebServer.test { _, client ->
            val result = client.get("/status").body?.string()?.fromJson<StatusRouter.StatusResult>()

            result?.up shouldBe true
        }
    }
})