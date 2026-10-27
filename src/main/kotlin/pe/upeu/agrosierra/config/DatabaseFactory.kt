package pe.upeu.agrosierra.config

import at.favre.lib.crypto.bcrypt.BCrypt
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import pe.upeu.agrosierra.dominio.Privilegio
import pe.upeu.agrosierra.dominio.Rol
import pe.upeu.agrosierra.repositorio.*
import java.io.File
import java.time.Instant

object DatabaseFactory {
    fun init(jdbcUrl: String = System.getenv("AGRO_DB_URL") ?: "jdbc:h2:file:./data/agrosierra") {
        File("data").mkdirs()
        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            driverClassName = "org.h2.Driver"
            username = "sa"
            password = ""
            maximumPoolSize = 6
            isAutoCommit = false
            isReadOnly = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        Database.connect(HikariDataSource(config))
        transaction {
            SchemaUtils.create(RolesTable, PrivilegiosTable, RolPrivilegiosTable, UsuariosTable, LotesTable, LaboresTable)
            if (RolesTable.selectAll().empty()) seed()
        }
    }

    private fun seed() {
        val privilegios = Privilegio.entries.associateWith { p ->
            PrivilegiosTable.insertAndGetId { it[nombre] = p.name }
        }
        val roles = Rol.entries.associateWith { r ->
            RolesTable.insertAndGetId { it[nombre] = r.name }
        }
        val matriz = mapOf(
            Rol.TECNICO to setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR),
            Rol.JEFE_CAMPO to setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.LABOR_CREAR, Privilegio.LABOR_EDITAR, Privilegio.LABOR_ELIMINAR, Privilegio.CAMPANIA_CERRAR),
            Rol.ADMINISTRADOR to setOf(Privilegio.LOTE_LEER, Privilegio.LABOR_LEER, Privilegio.USUARIO_GESTIONAR),
        )
        matriz.forEach { (rol, permisos) ->
            permisos.forEach { permiso ->
                RolPrivilegiosTable.insert {
                    it[rolId] = roles.getValue(rol)
                    it[privilegioId] = privilegios.getValue(permiso)
                }
            }
        }

        val hash = BCrypt.withDefaults().hashToString(12, "Agro2026*".toCharArray())
        val tecnicoId = UsuariosTable.insertAndGetId {
            it[usuario] = "tecnico1"; it[nombre] = "Elena Torres"; it[passwordHash] = hash; it[rolId] = roles.getValue(Rol.TECNICO)
        }
        val jefeId = UsuariosTable.insertAndGetId {
            it[usuario] = "jefe1"; it[nombre] = "Carlos Mendoza"; it[passwordHash] = hash; it[rolId] = roles.getValue(Rol.JEFE_CAMPO)
        }
        UsuariosTable.insertAndGetId {
            it[usuario] = "admin"; it[nombre] = "Lucia Ramos"; it[passwordHash] = hash; it[rolId] = roles.getValue(Rol.ADMINISTRADOR)
        }

        val lotes = listOf(
            listOf("L-001", "Pampa Dorada", "12.5", "Papa Canchan", "Mantaro", "false", "2.5"),
            listOf("L-002", "Andenes del Sol", "8.0", "Quinua", "Mantaro", "false", "1.8"),
            listOf("L-003", "Vista Hermosa", "15.2", "Maiz amilaceo", "Mantaro", "true", "2.0"),
            listOf("L-004", "Santa Rosa", "6.7", "Cebada", "Urubamba", "false", "1.6"),
            listOf("L-005", "El Molino", "10.1", "Haba", "Urubamba", "false", "1.4"),
            listOf("L-006", "Quebrada Verde", "9.3", "Papa Yungay", "Urubamba", "true", "2.2"),
            listOf("L-007", "Alto Cielo", "13.0", "Quinua roja", "Colca", "false", "1.7"),
            listOf("L-008", "La Esperanza", "7.4", "Tarwi", "Colca", "false", "1.5"),
        ).map { d ->
            LotesTable.insertAndGetId {
                it[codigo] = d[0]; it[nombre] = d[1]; it[hectareas] = d[2].toDouble(); it[cultivo] = d[3]
                it[valle] = d[4]; it[campaniaCerrada] = d[5].toBoolean(); it[dosisMaximaPorHectarea] = d[6].toDouble()
            }
        }

        val tipos = listOf(
            "RIEGO", "FUMIGACION", "COSECHA", "RIEGO", "FUMIGACION",
            "RIEGO", "FUMIGACION", "RIEGO", "COSECHA", "FUMIGACION",
            "RIEGO", "COSECHA", "RIEGO", "FUMIGACION", "RIEGO",
            "FUMIGACION", "COSECHA", "RIEGO", "FUMIGACION", "RIEGO",
        )
        repeat(20) { index ->
            val loteIndex = index % lotes.size
            LaboresTable.insert {
                it[loteId] = lotes[loteIndex]
                it[tipo] = tipos[index]
                it[fecha] = "2026-10-${(7 + index).coerceAtMost(26).toString().padStart(2, '0')}"
                it[responsableId] = if (index % 5 == 0) jefeId else tecnicoId
                it[cantidad] = if (tipos[index] == "FUMIGACION") 4.5 + loteIndex else 18.0 + index
                it[observacion] = when (tipos[index]) {
                    "COSECHA" -> "Cosecha selectiva y pesaje en campo"
                    "FUMIGACION" -> "Aplicacion controlada con EPP completo"
                    else -> "Riego por turnos, humedad verificada"
                }
                it[creadoEn] = Instant.now().minusSeconds((20 - index) * 3600L).toEpochMilli()
            }
        }
    }
}

