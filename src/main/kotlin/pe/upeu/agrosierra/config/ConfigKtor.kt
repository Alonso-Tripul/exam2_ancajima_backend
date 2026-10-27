package pe.upeu.agrosierra.config

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import pe.upeu.agrosierra.web.dto.ErrorResponse
import java.time.Instant

fun Application.configurarHttp(jwtService: JwtService) {
    val appLog = environment.log
    install(CallLogging)
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; explicitNulls = false; prettyPrint = true }) }
    install(CORS) {
        anyHost(); allowHeader(HttpHeaders.Authorization); allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Put); allowMethod(HttpMethod.Delete)
    }
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "exam2_ancajima"
            verifier(jwtService.verifier)
            validate { credential ->
                val uid = credential.payload.getClaim("uid").asLong()
                val privilegios = credential.payload.getClaim("privilegios").asList(String::class.java)
                if (uid != null && privilegios != null) JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(Instant.now().toString(), 401, "Unauthorized", "Token ausente, alterado o vencido", call.request.path()),
                )
            }
        }
    }
    install(StatusPages) {
        exception<ApiException> { call, cause ->
            call.respond(
                cause.status,
                ErrorResponse(
                    Instant.now().toString(), cause.status.value, cause.status.description,
                    cause.message, call.request.path(), cause.validationErrors,
                ),
            )
        }
        exception<Throwable> { call, cause ->
            appLog.error("Error no controlado", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(Instant.now().toString(), 500, "Internal Server Error", "Ocurrio un error interno", call.request.path()),
            )
        }
    }
}

