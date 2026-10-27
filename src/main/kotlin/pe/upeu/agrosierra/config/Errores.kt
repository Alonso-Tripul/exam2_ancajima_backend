package pe.upeu.agrosierra.config

import io.ktor.http.HttpStatusCode

open class ApiException(
    val status: HttpStatusCode,
    override val message: String,
    val validationErrors: Map<String, String> = emptyMap(),
) : RuntimeException(message)

class CredencialesInvalidasException : ApiException(HttpStatusCode.Unauthorized, "Usuario o contrasena incorrectos")
class SinPrivilegioException(message: String = "No cuenta con el privilegio requerido") : ApiException(HttpStatusCode.Forbidden, message)
class NoEncontradoException(message: String) : ApiException(HttpStatusCode.NotFound, message)
class ConflictoException(message: String) : ApiException(HttpStatusCode.Conflict, message)
class ValidacionException(errors: Map<String, String>) : ApiException(HttpStatusCode.BadRequest, "Datos invalidos", errors)

