package pe.upeu.agrosierra.servicio

import pe.upeu.agrosierra.config.ConflictoException
import pe.upeu.agrosierra.config.NoEncontradoException
import pe.upeu.agrosierra.config.SinPrivilegioException
import pe.upeu.agrosierra.config.ValidacionException
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.AgroRepository
import pe.upeu.agrosierra.repositorio.Pagina
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class DatosLabor(val loteId: Long, val tipo: String, val fecha: String, val cantidad: Double, val observacion: String)

class AgroService(private val repository: AgroRepository) {
    fun listarLotes(pagina: Int, tamanio: Int, valle: String?, estado: String?): Pagina<Lote> =
        repository.listarLotes(pagina.coerceAtLeast(0), tamanio.coerceIn(1, 100), valle, estado)

    fun lote(id: Long): Lote = repository.lotePorId(id) ?: throw NoEncontradoException("Lote no encontrado")

    fun listarLabores(loteId: Long, pagina: Int, tamanio: Int): Pagina<Labor> {
        lote(loteId)
        return repository.listarLabores(loteId, pagina.coerceAtLeast(0), tamanio.coerceIn(1, 100))
    }

    fun crearLabor(datos: DatosLabor, usuario: UsuarioAutenticado): Labor {
        exigir(usuario, Privilegio.LABOR_CREAR)
        val lote = lote(datos.loteId)
        if (lote.campaniaCerrada) throw ConflictoException("La campania del lote esta cerrada")
        val tipo = validar(datos, lote)
        return repository.crearLabor(datos.loteId, tipo, datos.fecha, usuario.id, datos.cantidad, datos.observacion.trim())
    }

    fun actualizarLabor(id: Long, datos: DatosLabor, usuario: UsuarioAutenticado): Labor {
        exigir(usuario, Privilegio.LABOR_EDITAR)
        val existente = repository.laborPorId(id) ?: throw NoEncontradoException("Labor no encontrada")
        val lote = lote(existente.loteId)
        if (lote.campaniaCerrada) throw ConflictoException("La campania del lote esta cerrada")
        if (usuario.rol == "TECNICO") {
            if (existente.responsableId != usuario.id) throw SinPrivilegioException("Un tecnico solo puede corregir sus propias labores")
            if (System.currentTimeMillis() - existente.creadoEn > 24 * 60 * 60 * 1000L) {
                throw SinPrivilegioException("El plazo de 24 horas para corregir esta labor vencio")
            }
        }
        val tipo = validar(datos.copy(loteId = existente.loteId), lote)
        return repository.actualizarLabor(id, tipo, datos.fecha, datos.cantidad, datos.observacion.trim())
    }

    fun eliminarLabor(id: Long, usuario: UsuarioAutenticado) {
        exigir(usuario, Privilegio.LABOR_ELIMINAR)
        repository.laborPorId(id) ?: throw NoEncontradoException("Labor no encontrada")
        repository.eliminarLabor(id)
    }

    fun cerrarCampania(loteId: Long, usuario: UsuarioAutenticado): Lote {
        exigir(usuario, Privilegio.CAMPANIA_CERRAR)
        val lote = lote(loteId)
        if (lote.campaniaCerrada) throw ConflictoException("La campania ya esta cerrada")
        if (!repository.tieneCosecha(loteId)) throw ConflictoException("Se requiere al menos una labor de tipo COSECHA")
        return repository.cerrarCampania(loteId)
    }

    private fun validar(datos: DatosLabor, lote: Lote): TipoLabor {
        val errores = linkedMapOf<String, String>()
        val tipo = runCatching { TipoLabor.valueOf(datos.tipo.uppercase()) }.getOrNull()
        if (tipo == null) errores["tipo"] = "Use RIEGO, FUMIGACION o COSECHA"
        try { LocalDate.parse(datos.fecha) } catch (_: DateTimeParseException) { errores["fecha"] = "Use una fecha ISO-8601 valida" }
        if (datos.cantidad <= 0.0) errores["cantidad"] = "La cantidad debe ser mayor que cero"
        if (datos.observacion.isBlank()) errores["observacion"] = "La observacion es obligatoria"
        if (datos.observacion.length > 500) errores["observacion"] = "Maximo 500 caracteres"
        if (tipo == TipoLabor.FUMIGACION && datos.cantidad > lote.dosisMaximaPorHectarea * lote.hectareas) {
            errores["cantidad"] = "Supera la dosis maxima permitida para ${lote.hectareas} ha"
        }
        if (errores.isNotEmpty()) throw ValidacionException(errores)
        return tipo!!
    }

    private fun exigir(usuario: UsuarioAutenticado, privilegio: Privilegio) {
        if (privilegio.name !in usuario.privilegios) throw SinPrivilegioException()
    }
}
