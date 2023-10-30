package adapters.web

import io.javalin.http.Context
import org.ktlib.toUUID
import java.util.*

val Context.userToken: String? get() = header("Authorization")?.substringAfter("Bearer ")

val Context.organizationId: UUID? get() = header("Organization")?.toUUID()
