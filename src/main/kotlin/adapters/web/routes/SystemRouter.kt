package adapters.web.routes

import adapters.web.userToken
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import org.ktlib.Environment
import org.ktlib.config
import org.ktlib.db.Database
import org.ktlib.web.Router
import usecases.createContext

object SystemRouter : Router {
    private val baseEncodeJson = config<Boolean>("web.baseEncodeJson")
    private const val detailsKey = ""

    override fun route() {
        get("/status", this::status)
        get("/q", this::isEmployee)
        get("/e", this::isBaseEncodeJson)
    }

    data class StatusResult(
        val up: Boolean,
        val details: Map<String, Boolean>? = null,
        val version: String? = null
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

    private fun isEmployee(ctx: Context) {
        ctx.json(if (createContext(ctx.userToken, null, Unit).userLogin?.user?.employee == true) 1 else 0)
    }

    private fun isBaseEncodeJson(ctx: Context) {
        ctx.json(if (baseEncodeJson) 1 else 0)
    }
}