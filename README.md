# Backend UCO

API REST en Java para una plataforma web de la UCO: profesores y estudiantes se
registran, el administrador aprueba o expulsa cuentas, y los profesores cargan y
modifican las notas de los estudiantes de su aula.

Todo se prueba desde la terminal: **no necesita interfaz gráfica**. Incluye además un
panel web interactivo en `http://localhost:8080/` (HTML/CSS/JS sin dependencias) y una
consola interna por comandos para operar la base de datos.

## Stack

| Pieza | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 (Web MVC, Data JPA, Security, Validation) |
| Base de datos | Oracle (perfil `oracle`) o H2 en modo Oracle (perfil `dev`) |
| Driver Oracle | `ojdbc17` |
| Seguridad | JWT HS256 (`com.auth0:java-jwt`) + BCrypt |
| Build | Maven Wrapper (`./mvnw`) — no hace falta instalar Maven |
| Tests | JUnit 5 + MockMvc (`./mvnw test`) |

## Arranque rápido (sin instalar nada)

Por defecto la aplicación usa H2 con `MODE=Oracle`, así que levanta al instante sin
necesidad de un servidor Oracle:

```bash
cd uco-backend
./mvnw spring-boot:run
```

- API: <http://localhost:8080>
- Panel web: <http://localhost:8080/>
- Consola H2: <http://localhost:8080/h2-console>
  (JDBC URL `jdbc:h2:file:./data/uco`, usuario `sa`, sin contraseña)
- Administrador inicial: `admin@uco.edu.co` / `Admin123!`

El perfil `dev` activa además el perfil `demo`, que carga profesores, estudiantes y
notas de ejemplo (ver [Datos de ejemplo](#datos-de-ejemplo)).

Los datos quedan en `./data/`. Para reiniciar de cero: `rm -rf data`.

## Oracle real con Docker

```bash
# 1. Habilitar Docker (una sola vez)
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"    # después cerrar y abrir sesión

# 2. Levantar Oracle Free 23ai (~1.6 GB la primera vez)
./scripts/oracle-up.sh

# 3. Arrancar la app contra Oracle (con datos de ejemplo)
set -a; . ./.env; set +a     # opcional, copia antes .env.example a .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=oracle,demo
```

Quita `,demo` si quieres la base vacía (solo con el administrador inicial).

Por defecto el contenedor expone:

| Dato | Valor |
|---|---|
| Host / puerto | `localhost:1521` |
| Servicio (PDB) | `FREEPDB1` |
| Usuario de la app | `UCO` |
| Contraseña | `UCO` |
| Usuario SYS | `oracle` |

El esquema (`UCO_USUARIO`, `UCO_ESTUDIANTE`, `UCO_PROFESOR`, `UCO_NOTA`) lo crea
Hibernate con `ddl-auto=update` en el primer arranque.

Para detenerlo: `docker compose down` (los datos quedan en el volumen `uco_oracle-datos`).
Para borrarlo todo: `docker compose down -v`.

## Configuración

Todas las variables se pueden definir por entorno (el archivo `.env.example` sirve de
plantilla; la app lee variables de entorno, no el archivo `.env`).

| Variable | Por defecto (dev) | Uso |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP |
| `UCO_JWT_SECRET` | secreto de desarrollo | Clave HS256 (mínimo 32 caracteres) |
| `UCO_JWT_TTL` | `PT8H` | Vigencia del token (formato ISO-8601) |
| `UCO_ADMIN_EMAIL` | `admin@uco.edu.co` | Correo del admin inicial |
| `UCO_ADMIN_PASSWORD` | `Admin123!` | Contraseña del admin inicial |
| `UCO_ADMIN_NOMBRE` | `Administrador UCO` | Nombre del admin inicial |
| `ORACLE_HOST` / `ORACLE_PORT` | `localhost` / `1521` | Servidor Oracle (perfil `oracle`) |
| `ORACLE_SERVICE` | `FREEPDB1` | Servicio de la PDB |
| `ORACLE_USER` / `ORACLE_PASSWORD` | `UCO` / `UCO` | Credenciales de la app |

> El administrador solo se crea si no existe un usuario con ese correo.

## Datos de ejemplo

El perfil `demo` crea cuentas, perfiles y notas de ejemplo. Es idempotente: si un
correo ya existe, no se duplica.

| Correo | Contraseña | Rol | Estado | Aula / Materia |
|---|---|---|---|---|
| `admin@uco.edu.co` | `Admin123!` | ADMIN | ACTIVO | — |
| `coordinacion@uco.edu.co` | `Demo123!` | ADMIN | ACTIVO | — |
| `laura.gomez@uco.edu.co` | `Demo123!` | PROFESOR | ACTIVO | A-101 / Bases de Datos |
| `andres.ramirez@uco.edu.co` | `Demo123!` | PROFESOR | ACTIVO | A-102 / Programación Web |
| `monica.salazar@uco.edu.co` | `Demo123!` | PROFESOR | ACTIVO | B-201 / Cálculo Diferencial |
| `jorge.cardenas@uco.edu.co` | `Demo123!` | PROFESOR | PENDIENTE | A-103 / Redes de Datos |
| `juan.perez@uco.edu.co` | `Demo123!` | ESTUDIANTE | ACTIVO | A-101 · EST-2026-001 |
| `sofia.torres@uco.edu.co` | `Demo123!` | ESTUDIANTE | ACTIVO | A-101 · EST-2026-002 |
| `camilo.rojas@uco.edu.co` | `Demo123!` | ESTUDIANTE | ACTIVO | A-102 · EST-2026-003 |
| `valentina.diaz@uco.edu.co` | `Demo123!` | ESTUDIANTE | ACTIVO | A-102 · EST-2026-004 |
| `diego.herrera@uco.edu.co` | `Demo123!` | ESTUDIANTE | ACTIVO | B-201 · EST-2026-005 |
| `mariana.lopez@uco.edu.co` | `Demo123!` | ESTUDIANTE | PENDIENTE | A-101 · EST-2026-006 |
| `santiago.munoz@uco.edu.co` | `Demo123!` | ESTUDIANTE | EXPULSADO | A-102 · EST-2026-007 |

Notas ya registradas (periodo `2026-1`): Juan 4.5, Sofía 2.9, Camilo 3.7,
Valentina 4.9 y Diego 3.2.

Así puedes probar de inmediato la aprobación (`jorge.cardenas` y `mariana.lopez` están
pendientes), la expulsión (`santiago.munoz`) y las notas por aula.

Para reiniciar los datos:

```bash
# H2 (dev)
rm -rf data

# Oracle: borra las tablas y reinicia la app
#   DROP TABLE UCO_NOTA, UCO_ESTUDIANTE, UCO_PROFESOR, UCO_USUARIO CASCADE CONSTRAINTS;
```

## Panel web

Abre <http://localhost:8080/>. El panel consume la misma API y no necesita compilación
de front-end (son tres archivos estáticos en `src/main/resources/static/`).

- **Ingreso:** botones con las cuentas de ejemplo que llenan el formulario; también hay
  registro de estudiante y profesor (queda pendiente de aprobación).
- **Administrador:** métricas generales, solicitudes pendientes con aprobación rápida,
  búsqueda y filtros por rol/estado, creación y edición de usuarios, aprobar, rechazar,
  expulsar, reactivar, eliminar e historial completo de notas.
- **Profesor:** resumen del aula, estudiantes con su promedio y registro, edición y
  eliminación de notas mediante formularios.
- **Estudiante:** promedio general, notas por materia y detalle de cada calificación.
- **Consola interna:** una vista con todos los comandos de `./scripts/db.sh` y botón
  para copiarlos.

La sesión se guarda en `localStorage`; si el token expira o la cuenta deja de estar
activa, el panel vuelve al login automáticamente.

## Consola interna de base de datos

`./scripts/db.sh` abre la misma aplicación en modo consola (`--cli`): no levanta el
servidor web, reutiliza los servicios y las reglas del backend, y sirve tanto para
Oracle como para H2. Se puede usar con la aplicación web encendida o apagada.

```bash
# Modo interactivo (escribe "ayuda" y Enter)
./scripts/db.sh

# Un solo comando y sale
./scripts/db.sh resumen
./scripts/db.sh "usuarios PENDIENTE"
./scripts/db.sh "sql select id, email, rol, estado from uco_usuario order by id"
```

| Comando | Qué hace |
|---|---|
| `resumen` | Conteos por rol y estado, y total de notas |
| `usuarios [ROL] [ESTADO]` | Lista y filtra usuarios (`PENDIENTE`, `ACTIVO`, …) |
| `pendientes` | Atajo para `usuarios PENDIENTE` |
| `notas` | Lista las notas registradas |
| `aprobar <id>` | Aprueba una solicitud (mismas reglas que la API) |
| `rechazar <id>` | Rechaza una solicitud pendiente |
| `expulsar <id>` | Expulsa un usuario activo |
| `reactivar <id>` | Reactiva un usuario rechazado o expulsado |
| `eliminar <id>` | Borra usuario, perfil y notas |
| `crear-usuario` | Asistente interactivo de creación |
| `tablas` | Lista las tablas de la base |
| `describir <tabla>` | Columnas, tipos, tamaño y nulabilidad |
| `sql <sentencia>` | Ejecuta SQL libre (`select`, `insert`, `update`, `delete`) |

Por defecto usa Oracle (`PERFIL=oracle`); con `PERFIL=dev` trabaja sobre H2 en archivo:

```bash
PERFIL=dev ./scripts/db.sh resumen
```

> Las operaciones son reales e inmediatas. El comando `crear-usuario` pide las
> contraseñas por teclado y las guarda con BCrypt; para `sql insert` necesitas un hash
> BCrypt ya generado.

## Probar sin interfaz gráfica

### 1. Prueba de humo completa (recomendada)

Con la aplicación corriendo:

```bash
./scripts/smoke-test.sh
```

Hace 27 verificaciones: login admin, registros, bloqueo de usuarios pendientes,
aprobación, login, listado de estudiantes del aula, creación y modificación de nota,
consulta de notas y promedio, control de roles y expulsión/reactivación.

### 2. `curl`

```bash
# Login (guarda el token)
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@uco.edu.co","password":"Admin123!"}' | jq -r .token)

# Usuarios pendientes
curl -s http://localhost:8080/api/admin/usuarios?estado=PENDIENTE \
  -H "Authorization: Bearer $TOKEN" | jq

# Aprobar al usuario 2
curl -s -X PATCH http://localhost:8080/api/admin/usuarios/2/aprobar \
  -H "Authorization: Bearer $TOKEN" | jq
```

### 3. `prueba.http`

Abre `prueba.http` en IntelliJ IDEA y ejecuta las peticiones en orden: las variables
(`tokenAdmin`, `tokenProfesor`, `idEstudiante`, …) se guardan solas entre peticiones.

## Endpoints

### Autenticación (público)

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/auth/registro/estudiante` | Registro de estudiante (queda `PENDIENTE`) |
| `POST` | `/api/auth/registro/profesor` | Registro de profesor con aula y materia (queda `PENDIENTE`) |
| `POST` | `/api/auth/login` | Devuelve el JWT (solo cuentas `ACTIVO`) |
| `GET` | `/api/auth/me` | Datos del usuario autenticado |

### Administrador (`ROLE_ADMIN`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/admin/usuarios?rol=&estado=` | Lista y filtra usuarios |
| `GET` | `/api/admin/usuarios/{id}` | Detalle de un usuario |
| `POST` | `/api/admin/usuarios` | Crea un usuario de cualquier rol |
| `PUT` | `/api/admin/usuarios/{id}` | Edita nombre, aula, materia o código |
| `PATCH` | `/api/admin/usuarios/{id}/aprobar` | `PENDIENTE`/`RECHAZADO` → `ACTIVO` |
| `PATCH` | `/api/admin/usuarios/{id}/rechazar` | `PENDIENTE` → `RECHAZADO` |
| `PATCH` | `/api/admin/usuarios/{id}/expulsar` | `ACTIVO` → `EXPULSADO` |
| `PATCH` | `/api/admin/usuarios/{id}/reactivar` | `EXPULSADO`/`RECHAZADO` → `ACTIVO` |
| `DELETE` | `/api/admin/usuarios/{id}` | Elimina usuario y sus datos (no admins) |
| `GET` | `/api/admin/resumen` | Conteos por rol y estado |
| `GET` | `/api/admin/notas` | Todas las notas |

### Profesor (`ROLE_PROFESOR`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/profesor/perfil` | Perfil con aula y materia |
| `GET` | `/api/profesor/estudiantes` | Estudiantes activos de su aula, con promedio |
| `GET` | `/api/profesor/notas` | Notas que ha registrado |
| `POST` | `/api/profesor/notas` | Registra una nota |
| `PUT` | `/api/profesor/notas/{id}` | Modifica una nota propia |
| `DELETE` | `/api/profesor/notas/{id}` | Elimina una nota propia |

### Estudiante (`ROLE_ESTUDIANTE`)

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/estudiante/perfil` | Perfil con aula y código |
| `GET` | `/api/estudiante/notas` | Sus notas |
| `GET` | `/api/estudiante/promedio` | Promedio y cantidad de notas |

## Reglas de negocio

- Al registrarse, toda cuenta queda `PENDIENTE` y **no puede iniciar sesión** hasta
  que el administrador la apruebe.
- Solo un profesor puede crear notas, y **solo a estudiantes activos de su misma aula**;
  la materia y el aula de la nota salen del perfil del profesor.
- Un profesor solo puede modificar o borrar **sus propias** notas.
- Las transiciones de estado son explícitas (aprobación → rechazo → expulsión → reactivación);
  una transición no permitida devuelve `409`.
- Los administradores no pueden expulsarse ni eliminarse a sí mismos.
- Al expulsar a un usuario, su token deja de servir de inmediato: el filtro JWT valida
  el estado contra la base de datos en cada petición.
- Las contraseñas se guardan con BCrypt y el correo es único (sin distinguir mayúsculas).

## Estructura del proyecto

```
src/main/java/co/edu/uco/backend
├── cli/          Consola interna (--cli) para operar la base por comandos
├── config/       Propiedades tipadas (UcoProperties) y creación del admin inicial
├── domain/       Entidades JPA con comportamiento (Usuario, Estudiante, Profesor, Nota)
├── dto/          Records de entrada/salida con validación
├── exception/    Excepciones de negocio con su código HTTP
├── mapper/       Conversión entidad → DTO
├── repo/         Repositorios Spring Data JPA
├── security/     JWT, filtro de autenticación y configuración de Spring Security
├── service/      Interfaces de casos de uso
│   └── impl/     Implementaciones transaccionales
└── web/          Controladores REST y manejo global de errores (RFC 7807)

src/main/resources/static
├── index.html    Panel web (login, panel por rol y consola interna)
├── styles.css    Diseño responsive
└── app.js        Lógica del panel (sin dependencias)
```

Decisiones destacadas:

- Inyección por constructor en todas las clases (sin `@Autowired` en campos).
- DTOs inmutables (`record`) y entidades que no se exponen directamente.
- Errores en formato `application/problem+json` (`ProblemDetail`).
- `@Transactional` en servicios; lecturas marcadas `readOnly`.
- Consultas por lote en el mapeo de usuarios para evitar el problema N+1.
- Propiedades validadas al arrancar (si el secreto JWT es corto, la app no inicia).

> **Nota sobre identificadores:** el `id` de `/api/admin/usuarios/...` es el del
> **usuario** (el que se usa para aprobar/expulsar). Para calificar, usa el
> `estudianteId` que devuelve `/api/profesor/estudiantes`, que es el del perfil de
> estudiante.

## Tests

```bash
./mvnw test
```

`FlujoUcoIntegrationTest` recorre el flujo completo (registro → aprobación → notas →
expulsión) contra H2 en memoria con el perfil `test`.

## Producción (notas)

- Cambia `UCO_JWT_SECRET` y `UCO_ADMIN_PASSWORD`.
- Desactiva la consola H2 (`spring.h2.console.enabled=false`, ya viene así en `oracle`).
- Considera un usuario de base de datos con permisos mínimos y migraciones (Flyway/Liquibase)
  en lugar de `ddl-auto=update`.
