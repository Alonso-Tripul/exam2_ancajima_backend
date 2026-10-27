package pe.upeu.agrosierra.web

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import pe.upeu.agrosierra.config.NoEncontradoException
import pe.upeu.agrosierra.config.SinPrivilegioException
import pe.upeu.agrosierra.dominio.UsuarioAutenticado
import pe.upeu.agrosierra.seguridad.AuthService
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.servicio.AgroService
import pe.upeu.agrosierra.servicio.DatosLabor
import pe.upeu.agrosierra.web.dto.*

fun Application.configurarRutas(authService: AuthService, usuarios: UsuarioRepository, agroService: AgroService) {
    routing {
        get("/") { call.respond(mapOf("servicio" to "AgroAPI", "proyecto" to "exam2_ancajima", "estado" to "operativo")) }
        get("/health") { call.respond(mapOf("status" to "UP")) }
        route("/api/v1") {
            post("/auth/login") {
                val request = call.receive<LoginRequest>()
                val resultado = authService.login(request.usuario, request.password)
                val u = resultado.usuario
                call.respond(LoginResponse(resultado.token, u.usuario, u.nombre, u.rol, u.privilegios, resultado.expiraEn))
            }
            authenticate("auth-jwt") {
                get("/lotes") {
                    call.usuario(usuarios).exigir("LOTE_LEER")
                    val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 0
                    val tamanio = call.request.queryParameters["tamanio"]?.toIntOrNull() ?: 20
                    call.respond(agroService.listarLotes(pagina, tamanio, call.request.queryParameters["valle"], call.request.queryParameters["estado"]).toResponse { it.toResponse() })
                }
                get("/lotes/{id}") {
                    call.usuario(usuarios).exigir("LOTE_LEER")
                    call.respond(agroService.lote(call.id()).toResponse())
                }
                get("/lotes/{id}/labores") {
                    call.usuario(usuarios).exigir("LABOR_LEER")
                    val pagina = call.request.queryParameters["pagina"]?.toIntOrNull() ?: 0
                    val tamanio = call.request.queryParameters["tamanio"]?.toIntOrNull() ?: 20
                    call.respond(agroService.listarLabores(call.id(), pagina, tamanio).toResponse { it.toResponse() })
                }
                post("/labores") {
                    val request = call.receive<LaborRequest>()
                    call.respond(HttpStatusCode.Created, agroService.crearLabor(request.toDatos(), call.usuario(usuarios)).toResponse())
                }
                put("/labores/{id}") {
                    val request = call.receive<LaborRequest>()
                    call.respond(agroService.actualizarLabor(call.id(), request.toDatos(), call.usuario(usuarios)).toResponse())
                }
                delete("/labores/{id}") {
                    agroService.eliminarLabor(call.id(), call.usuario(usuarios))
                    call.respondText("", status = HttpStatusCode.NoContent)
                }
                post("/lotes/{id}/cerrar-campania") {
                    call.respond(agroService.cerrarCampania(call.id(), call.usuario(usuarios)).toResponse())
                }
                get("/usuarios/me") { call.respond(call.usuario(usuarios).toResponse()) }
            }
        }
    }
}

private fun LaborRequest.toDatos() = DatosLabor(loteId, tipo, fecha, cantidad, observacion)
private fun io.ktor.server.application.ApplicationCall.id(): Long =
    parameters["id"]?.toLongOrNull() ?: throw NoEncontradoException("Identificador invalido")
private fun UsuarioAutenticado.exigir(privilegio: String) {
    if (privilegio !in privilegios) throw SinPrivilegioException()
}
private fun io.ktor.server.application.ApplicationCall.usuario(repository: UsuarioRepository): UsuarioAutenticado {
    val jwt = principal<JWTPrincipal>() ?: throw SinPrivilegioException("No existe un usuario autenticado")
    return repository.buscarPorId(jwt.payload.getClaim("uid").asLong()) ?: throw SinPrivilegioException("El usuario ya no esta activo")
}

