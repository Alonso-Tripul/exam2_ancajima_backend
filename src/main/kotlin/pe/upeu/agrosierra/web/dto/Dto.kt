package pe.upeu.agrosierra.web.dto

import kotlinx.serialization.Serializable
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.UsuarioAutenticado
import pe.upeu.agrosierra.repositorio.Pagina

@Serializable
data class LoginRequest(val usuario: String = "", val password: String = "")

@Serializable
data class LoginResponse(
    val token: String, val usuario: String, val nombre: String, val rol: String,
    val privilegios: Set<String>, val expiraEn: Long,
)

@Serializable
data class UsuarioResponse(
    val id: Long, val usuario: String, val nombre: String, val rol: String, val privilegios: Set<String>,
)

@Serializable
data class LoteResponse(
    val id: Long, val codigo: String, val nombre: String, val hectareas: Double,
    val cultivo: String, val valle: String, val campaniaCerrada: Boolean, val dosisMaximaPorHectarea: Double,
)

@Serializable
data class LaborResponse(
    val id: Long, val loteId: Long, val tipo: String, val fecha: String,
    val responsable: String, val cantidad: Double, val observacion: String,
)

@Serializable
data class LaborRequest(
    val loteId: Long = 0, val tipo: String = "", val fecha: String = "",
    val cantidad: Double = 0.0, val observacion: String = "",
)

@Serializable
data class PaginaResponse<T>(
    val contenido: List<T>, val pagina: Int, val tamanio: Int, val totalElementos: Long,
    val totalPaginas: Int, val ultima: Boolean,
)

@Serializable
data class ErrorResponse(
    val timestamp: String, val status: Int, val error: String, val message: String,
    val path: String, val validationErrors: Map<String, String> = emptyMap(),
)

fun UsuarioAutenticado.toResponse() = UsuarioResponse(id, usuario, nombre, rol, privilegios)
fun Lote.toResponse() = LoteResponse(id, codigo, nombre, hectareas, cultivo, valle, campaniaCerrada, dosisMaximaPorHectarea)
fun Labor.toResponse() = LaborResponse(id, loteId, tipo.name, fecha, responsable, cantidad, observacion)
fun <T, R> Pagina<T>.toResponse(mapper: (T) -> R) = PaginaResponse(contenido.map(mapper), pagina, tamanio, totalElementos, totalPaginas, ultima)

