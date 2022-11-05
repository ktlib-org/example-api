package api.controller

import api.ApiRole.Anyone
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.HttpCode
import org.ktapi.Environment
import org.ktapi.db.Database
import org.ktapi.web.Router
import org.ktapi.web.documentedHandler

data class StatusResult(
    val up: Boolean,
    val details: Map<String, Boolean>? = null,
    val version: String? = null
)

object StatusController : Router {
    override fun route() {
        get("/status", status, Anyone)
    }

    private const val detailsKey = ""
    private const val tag = "System"

    private val status = documentedHandler {
        doc("status", "Returns status of API", tag) {
            json<StatusResult>("200")
        }
        handler { ctx ->
            val showDetails = detailsKey.isBlank() || ctx.queryParam("details") == detailsKey
            val statuses = doStatusCheck()
            val up = statuses.all { it.value }

            val result = when (showDetails) {
                true -> StatusResult(up, statuses, Environment.version)
                else -> StatusResult(up)
            }

            ctx.status(if (result.up) HttpCode.OK else HttpCode.SERVICE_UNAVAILABLE)
            ctx.json(result)
        }
    }

    private fun doStatusCheck() = mapOf(
        "db" to Database.connected
    )
}