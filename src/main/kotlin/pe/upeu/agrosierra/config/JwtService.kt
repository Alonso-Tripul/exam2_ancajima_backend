package pe.upeu.agrosierra.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import pe.upeu.agrosierra.dominio.UsuarioAutenticado
import java.time.Instant
import java.util.Date

class JwtService(
    private val secret: String = System.getenv("AGRO_JWT_SECRET")
        ?: "exam2-ancajima-clave-local-cambiar-en-produccion-2026",
) {
    val issuer = "exam2_ancajima"
    val audience = "agrocampo-android"
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).withIssuer(issuer).withAudience(audience).build()

    fun crearToken(usuario: UsuarioAutenticado): Pair<String, Long> {
        val expira = Instant.now().plusSeconds(3600)
        val token = JWT.create()
            .withIssuer(issuer).withAudience(audience).withSubject(usuario.id.toString())
            .withClaim("uid", usuario.id).withClaim("usuario", usuario.usuario)
            .withClaim("nombre", usuario.nombre).withClaim("rol", usuario.rol)
            .withArrayClaim("privilegios", usuario.privilegios.sorted().toTypedArray())
            .withIssuedAt(Date.from(Instant.now())).withExpiresAt(Date.from(expira)).sign(algorithm)
        return token to expira.epochSecond
    }
}

