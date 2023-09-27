package adapters.web.routes

import adapters.web.organizationId
import adapters.web.userToken
import io.github.oshai.kotlinlogging.KotlinLogging
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.http.Context
import org.ktlib.*
import org.ktlib.web.Router
import usecases.UseCase
import usecases.UseCaseConfig
import usecases.UseCaseContext
import usecases.createContext
import kotlin.reflect.KClass

object UseCaseRouter : Router {
    private val logger = KotlinLogging.logger {}
    private val useCasePaths = mutableSetOf<String>()
    private val baseEncodeJson = config<Boolean>("web.baseEncodeJson", false)

    fun execute(ctx: Context, useCaseType: KClass<out UseCase<Any, Any?>>, inputType: KClass<Any>) {
        val context = if (inputType == Unit::class) {
            createContext(ctx.userToken, ctx.organizationId)
        } else {
            val body = if (baseEncodeJson) ctx.bodyAsBytes().base64Decode() else ctx.bodyAsBytes()
            createContext(ctx.userToken, ctx.organizationId, body.fromJson(inputType))
        }

        @Suppress("UNCHECKED_CAST")
        val result = UseCase.create(useCaseType, context as UseCaseContext<Any>).execute()

        if (result != Unit && result != null) {
            if (baseEncodeJson) {
                ctx.result(result.toJsonBytes().base64EncodeAsBytes())
            } else {
                ctx.json(result)
            }
        }
    }

    override fun route() {
        instancesFromFilesRelativeToClass<UseCaseConfig, UseCase<Any, Any?>>()
            .forEach { useCase ->
                val simpleName = useCase::class.simpleName!!
                val qualifiedName = useCase::class.qualifiedName!!
                val isEmployee = qualifiedName.contains(".employee.")

                val handler = { ctx: Context -> execute(ctx, useCase::class, useCase.inputType) }

                val addPath = { name: String ->
                    val path = name.createPath(isEmployee)

                    if (path in useCasePaths) throw Exception("Duplicate UseCase path $path for use case $qualifiedName}")

                    logger.info { "UseCase $qualifiedName at path $path" }
                    useCasePaths.add(path)

                    post(path, handler)
                }

                addPath(simpleName)
                useCase.aliases.forEach { addPath(it) }
            }
    }

    private fun String.createPath(isEmployee: Boolean) =
        "/use-cases/${if (isEmployee) "employee/" else ""}" + this
}