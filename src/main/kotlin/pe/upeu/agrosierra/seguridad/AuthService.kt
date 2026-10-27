package pe.upeu.agrosierra.seguridad

import at.favre.lib.crypto.bcrypt.BCrypt
import pe.upeu.agrosierra.config.CredencialesInvalidasException
import pe.upeu.agrosierra.config.JwtService
import pe.upeu.agrosierra.dominio.UsuarioAutenticado

data class ResultadoLogin(val usuario: UsuarioAutenticado, val token: String, val expiraEn: Long)

class AuthService(private val repository: UsuarioRepository, private val jwtService: JwtService) {
    fun login(usuario: String, password: String): ResultadoLogin {
        val encontrado = repository.buscarPorUsuario(usuario.trim()) ?: throw CredencialesInvalidasException()
        val valido = BCrypt.verifyer().verify(password.toCharArray(), encontrado.passwordHash).verified
        if (!valido) throw CredencialesInvalidasException()
        val (token, expiraEn) = jwtService.crearToken(encontrado.usuario)
        return ResultadoLogin(encontrado.usuario, token, expiraEn)
    }
}
