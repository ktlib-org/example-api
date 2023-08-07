package web.routes

import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiResponse
import org.ktapi.Environment
import org.ktapi.db.Database
import org.ktapi.web.Router
import web.ApiRole

object StatusRouter : Router {
    private const val detailsKey = ""
    private const val tag = "System"

    override fun route() {
        get("/status", this::status, ApiRole.Anyone)
    }

    data class StatusResult(
        val up: Boolean,
        val details: Map<String, Boolean>? = null,
        val version: String? = null
    )

    @OpenApi(
        path = "/status",
        methods = [HttpMethod.GET],
        operationId = "status",
        summary = "Returns the status of the API",
        tags = [tag],
        responses = [
            OpenApiResponse("200", [OpenApiContent(StatusResult::class)]),
            OpenApiResponse("503")
        ]
    )
    private fun status(ctx: Context) {
        val showDetails = detailsKey.isBlank() || ctx.queryParam("details") == detailsKey
        val statuses = doStatusCheck()
        val up = statuses.all { it.value }

        val result = when (showDetails) {
            true -> StatusResult(up, statuses, Environment.version)
            else -> StatusResult(up)
        }

        ctx.status(if (result.up) HttpStatus.OK else HttpStatus.SERVICE_UNAVAILABLE)
        ctx.json(result)
    }

    private fun doStatusCheck() = mapOf(
        "db" to Database.connected
    )
}