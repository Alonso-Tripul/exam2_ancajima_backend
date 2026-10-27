# exam2_ancajima - AgroAPI

Backend nuevo para AgroSierra. Expone un API REST versionado, protege rutas con JWT, autoriza por privilegio y persiste en una base H2 relacional.

## Ejecutar con un solo comando

```powershell
.\gradlew.bat run
```

El servicio queda en `http://localhost:8080`. La base se crea en `data/agrosierra.mv.db` con 3 usuarios, 8 lotes y 20 labores.

Usuarios (contraseña: `Agro2026*`): `tecnico1`, `jefe1`, `admin`.

## Base de datos en IntelliJ IDEA

- Driver: H2
- URL: `jdbc:h2:file:<RUTA_BACKEND>/data/agrosierra`
- Usuario: `sa`
- Contraseña: vacía

Detén el backend antes de abrir la conexión de archivo. La edición Community de IntelliJ no incluye la ventana Database. En Ultimate: Database > New > Data Source > H2, pegar la URL y probar la conexión.

## Paquetes

- `config/`: JWT, H2, CORS, 401 y formato común de errores.
- `seguridad/`: autenticación, usuarios y carga de privilegios.
- `dominio/`: modelos sin dependencias web.
- `repositorio/`: tablas y persistencia transaccional.
- `servicio/`: RN-01 a RN-04.
- `web/`: rutas `/api/v1` y DTO.

## Contrato

| Verbo | Ruta | Privilegio |
|---|---|---|
| POST | `/api/v1/auth/login` | Público |
| GET | `/api/v1/lotes` | LOTE_LEER |
| GET | `/api/v1/lotes/{id}` | LOTE_LEER |
| GET | `/api/v1/lotes/{id}/labores` | LABOR_LEER |
| POST | `/api/v1/labores` | LABOR_CREAR |
| PUT | `/api/v1/labores/{id}` | LABOR_EDITAR |
| DELETE | `/api/v1/labores/{id}` | LABOR_ELIMINAR |
| POST | `/api/v1/lotes/{id}/cerrar-campania` | CAMPANIA_CERRAR |
| GET | `/api/v1/usuarios/me` | Autenticado |

Los listados usan `contenido/pagina/tamanio/totalElementos/totalPaginas/ultima`. Los errores usan `timestamp/status/error/message/path/validationErrors`.

## Pruebas

```powershell
.\gradlew.bat test
```

