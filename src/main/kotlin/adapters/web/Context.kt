package adapters.web

import io.javalin.http.Context

val Context.userToken: String? get() = header("Authorization")?.substringAfter("Bearer ")

val Context.organizationId: String? get() = header("Organization")
