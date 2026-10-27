package pe.upeu.agrosierra.repositorio

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object RolesTable : LongIdTable("roles") { val nombre = varchar("nombre", 40).uniqueIndex() }
object PrivilegiosTable : LongIdTable("privilegios") { val nombre = varchar("nombre", 50).uniqueIndex() }
object RolPrivilegiosTable : Table("rol_privilegios") {
    val rolId = reference("rol_id", RolesTable)
    val privilegioId = reference("privilegio_id", PrivilegiosTable)
    override val primaryKey = PrimaryKey(rolId, privilegioId)
}
object UsuariosTable : LongIdTable("usuarios") {
    val usuario = varchar("usuario", 40).uniqueIndex()
    val nombre = varchar("nombre", 120)
    val passwordHash = varchar("password_hash", 100)
    val rolId = reference("rol_id", RolesTable)
    val activo = bool("activo").default(true)
}
object LotesTable : LongIdTable("lotes") {
    val codigo = varchar("codigo", 20).uniqueIndex()
    val nombre = varchar("nombre", 120)
    val hectareas = double("hectareas")
    val cultivo = varchar("cultivo", 80)
    val valle = varchar("valle", 60)
    val campaniaCerrada = bool("campania_cerrada").default(false)
    val dosisMaximaPorHectarea = double("dosis_maxima_por_hectarea")
}
object LaboresTable : LongIdTable("labores") {
    val loteId = reference("lote_id", LotesTable)
    val tipo = varchar("tipo", 20)
    val fecha = varchar("fecha", 10)
    val responsableId = reference("responsable_id", UsuariosTable)
    val cantidad = double("cantidad")
    val observacion = varchar("observacion", 500)
    val creadoEn = long("creado_en")
}
