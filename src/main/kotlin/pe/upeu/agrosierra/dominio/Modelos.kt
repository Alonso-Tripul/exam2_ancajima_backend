package pe.upeu.agrosierra.dominio

enum class TipoLabor { RIEGO, FUMIGACION, COSECHA }

enum class Privilegio {
    LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR,
    LABOR_ELIMINAR, CAMPANIA_CERRAR, USUARIO_GESTIONAR,
}

enum class Rol { TECNICO, JEFE_CAMPO, ADMINISTRADOR }

data class UsuarioAutenticado(
    val id: Long,
    val usuario: String,
    val nombre: String,
    val rol: String,
    val privilegios: Set<String>,
)

data class Lote(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val hectareas: Double,
    val cultivo: String,
    val valle: String,
    val campaniaCerrada: Boolean,
    val dosisMaximaPorHectarea: Double,
)

data class Labor(
    val id: Long,
    val loteId: Long,
    val tipo: TipoLabor,
    val fecha: String,
    val responsable: String,
    val responsableId: Long,
    val cantidad: Double,
    val observacion: String,
    val creadoEn: Long,
)

