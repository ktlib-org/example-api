package api.controller

import api.WebServer
import io.kotlintest.shouldBe
import org.ktapi.fromJson
import org.ktapi.test.StringSpec

class SystemControllerTests : StringSpec({
    "status" {
        WebServer.test { _, client ->
            val result = client.get("/status").body?.string()?.fromJson<StatusResult>()

            result?.up shouldBe true
        }
    }
})