package pe.upeu.agrosierra.seguridad

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import pe.upeu.agrosierra.dominio.UsuarioAutenticado
import pe.upeu.agrosierra.repositorio.*

data class UsuarioConHash(val usuario: UsuarioAutenticado, val passwordHash: String)

class UsuarioRepository {
    fun buscarPorUsuario(nombreUsuario: String): UsuarioConHash? = transaction {
        val row = UsuariosTable
            .join(RolesTable, JoinType.INNER, UsuariosTable.rolId, RolesTable.id)
            .selectAll()
            .where { (UsuariosTable.usuario eq nombreUsuario) and (UsuariosTable.activo eq true) }
            .singleOrNull() ?: return@transaction null
        UsuarioConHash(row.toUsuario(privilegiosDe(row[UsuariosTable.id].value)), row[UsuariosTable.passwordHash])
    }

    fun buscarPorId(id: Long): UsuarioAutenticado? = transaction {
        val row = UsuariosTable
            .join(RolesTable, JoinType.INNER, UsuariosTable.rolId, RolesTable.id)
            .selectAll()
            .where { (UsuariosTable.id eq id) and (UsuariosTable.activo eq true) }
            .singleOrNull() ?: return@transaction null
        row.toUsuario(privilegiosDe(id))
    }

    private fun privilegiosDe(usuarioId: Long): Set<String> {
        val rol = UsuariosTable.selectAll().where { UsuariosTable.id eq usuarioId }.single()[UsuariosTable.rolId]
        return RolPrivilegiosTable
            .join(PrivilegiosTable, JoinType.INNER, RolPrivilegiosTable.privilegioId, PrivilegiosTable.id)
            .selectAll().where { RolPrivilegiosTable.rolId eq rol }
            .map { it[PrivilegiosTable.nombre] }.toSet()
    }

    private fun ResultRow.toUsuario(privilegios: Set<String>) = UsuarioAutenticado(
        id = this[UsuariosTable.id].value,
        usuario = this[UsuariosTable.usuario],
        nombre = this[UsuariosTable.nombre],
        rol = this[RolesTable.nombre],
        privilegios = privilegios,
    )
}

