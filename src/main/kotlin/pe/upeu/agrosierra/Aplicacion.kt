package pe.upeu.agrosierra

import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import pe.upeu.agrosierra.config.DatabaseFactory
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.config.configurarHttp
import pe.upeu.agrosierra.repositorio.AgroRepository
import pe.upeu.agrosierra.seguridad.AuthService
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import pe.upeu.agrosierra.servicio.AgroService
import pe.upeu.agrosierra.web.configurarRutas

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toIntOrNull() ?: 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()
    val jwtService = JwtService()
    val usuarios = UsuarioRepository()
    configurarHttp(jwtService)
    configurarRutas(AuthService(usuarios, jwtService), usuarios, AgroService(AgroRepository()))
}

