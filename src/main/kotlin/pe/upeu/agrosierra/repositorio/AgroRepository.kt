package pe.upeu.agrosierra.repositorio

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import pe.upeu.agrosierra.dominio.Labor
import pe.upeu.agrosierra.dominio.Lote
import pe.upeu.agrosierra.dominio.TipoLabor

data class Pagina<T>(
    val contenido: List<T>, val pagina: Int, val tamanio: Int, val totalElementos: Long,
) {
    val totalPaginas: Int = if (totalElementos == 0L) 0 else ((totalElementos + tamanio - 1) / tamanio).toInt()
    val ultima: Boolean = pagina + 1 >= totalPaginas
}

class AgroRepository {
    fun listarLotes(pagina: Int, tamanio: Int, valle: String?, estado: String?): Pagina<Lote> = transaction {
        val condicionValle = valle?.takeIf { it.isNotBlank() }?.let { LotesTable.valle eq it }
        val condicionEstado = when (estado?.uppercase()) {
            "ABIERTA" -> LotesTable.campaniaCerrada eq false
            "CERRADA" -> LotesTable.campaniaCerrada eq true
            else -> null
        }
        val query = LotesTable.selectAll().apply {
            if (condicionValle != null && condicionEstado != null) andWhere { condicionValle and condicionEstado }
            else if (condicionValle != null) andWhere { condicionValle }
            else if (condicionEstado != null) andWhere { condicionEstado }
        }
        val total = query.count()
        val contenido = query.orderBy(LotesTable.codigo).limit(tamanio).offset((pagina * tamanio).toLong()).map { it.toLote() }
        Pagina(contenido, pagina, tamanio, total)
    }

    fun lotePorId(id: Long): Lote? = transaction {
        LotesTable.selectAll().where { LotesTable.id eq id }.singleOrNull()?.toLote()
    }

    fun listarLabores(loteId: Long, pagina: Int, tamanio: Int): Pagina<Labor> = transaction {
        val query = LaboresTable.join(UsuariosTable, JoinType.INNER, LaboresTable.responsableId, UsuariosTable.id)
            .selectAll().where { LaboresTable.loteId eq loteId }
        val total = query.count()
        val contenido = query.orderBy(LaboresTable.fecha to SortOrder.DESC, LaboresTable.id to SortOrder.DESC)
            .limit(tamanio).offset((pagina * tamanio).toLong()).map { it.toLabor() }
        Pagina(contenido, pagina, tamanio, total)
    }

    fun laborPorId(id: Long): Labor? = transaction {
        LaboresTable.join(UsuariosTable, JoinType.INNER, LaboresTable.responsableId, UsuariosTable.id)
            .selectAll().where { LaboresTable.id eq id }.singleOrNull()?.toLabor()
    }

    fun crearLabor(loteId: Long, tipo: TipoLabor, fecha: String, responsableId: Long, cantidad: Double, observacion: String): Labor = transaction {
        val id = LaboresTable.insertAndGetId {
            it[LaboresTable.loteId] = loteId; it[LaboresTable.tipo] = tipo.name; it[LaboresTable.fecha] = fecha
            it[LaboresTable.responsableId] = responsableId; it[LaboresTable.cantidad] = cantidad
            it[LaboresTable.observacion] = observacion; it[creadoEn] = System.currentTimeMillis()
        }.value
        laborPorId(id)!!
    }

    fun actualizarLabor(id: Long, tipo: TipoLabor, fecha: String, cantidad: Double, observacion: String): Labor = transaction {
        LaboresTable.update({ LaboresTable.id eq id }) {
            it[LaboresTable.tipo] = tipo.name; it[LaboresTable.fecha] = fecha
            it[LaboresTable.cantidad] = cantidad; it[LaboresTable.observacion] = observacion
        }
        laborPorId(id)!!
    }

    fun eliminarLabor(id: Long) = transaction { LaboresTable.deleteWhere { LaboresTable.id eq id } }

    fun cerrarCampania(loteId: Long): Lote = transaction {
        LotesTable.update({ LotesTable.id eq loteId }) { it[campaniaCerrada] = true }
        lotePorId(loteId)!!
    }

    fun tieneCosecha(loteId: Long): Boolean = transaction {
        LaboresTable.selectAll().where { (LaboresTable.loteId eq loteId) and (LaboresTable.tipo eq TipoLabor.COSECHA.name) }.count() > 0
    }

    private fun ResultRow.toLote() = Lote(
        id = this[LotesTable.id].value, codigo = this[LotesTable.codigo], nombre = this[LotesTable.nombre],
        hectareas = this[LotesTable.hectareas], cultivo = this[LotesTable.cultivo], valle = this[LotesTable.valle],
        campaniaCerrada = this[LotesTable.campaniaCerrada], dosisMaximaPorHectarea = this[LotesTable.dosisMaximaPorHectarea],
    )

    private fun ResultRow.toLabor() = Labor(
        id = this[LaboresTable.id].value, loteId = this[LaboresTable.loteId].value,
        tipo = TipoLabor.valueOf(this[LaboresTable.tipo]), fecha = this[LaboresTable.fecha],
        responsable = this[UsuariosTable.nombre], responsableId = this[LaboresTable.responsableId].value,
        cantidad = this[LaboresTable.cantidad], observacion = this[LaboresTable.observacion], creadoEn = this[LaboresTable.creadoEn],
    )
}

