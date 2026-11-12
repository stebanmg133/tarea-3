package co.edu.uco.backend.cli;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Nota;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.dto.CrearUsuarioRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.ResumenAdminResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.exception.ApiException;
import co.edu.uco.backend.repo.NotaRepository;
import co.edu.uco.backend.service.AdminService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Consola interna para operar la base de datos sin interfaz gráfica.
 *
 * <p>Se activa con {@code --cli} (ver {@code scripts/db.sh}). Reutiliza los servicios
 * de la aplicación para las operaciones de negocio y permite SQL libre para todo lo demás.</p>
 */
@Component
@ConditionalOnProperty(name = "uco.cli", havingValue = "true")
public class CliRunner implements ApplicationRunner {

    private static final Set<String> COMANDOS_SALIR = Set.of("salir", "exit", "quit", "q");
    private static final int FILAS_MAXIMAS = 50;

    private final AdminService adminService;
    private final NotaRepository notas;
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final ConfigurableApplicationContext contexto;
    private final BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
    /** En modo interactivo se piden confirmaciones; con argumentos o tuberías se asumen. */
    private final boolean interactivo = System.console() != null;

    public CliRunner(AdminService adminService,
                     NotaRepository notas,
                     JdbcTemplate jdbc,
                     DataSource dataSource,
                     ConfigurableApplicationContext contexto) {
        this.adminService = adminService;
        this.notas = notas;
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.contexto = contexto;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> comandos = args.getNonOptionArgs();
        if (!comandos.isEmpty()) {
            comandos.forEach(this::ejecutar);
            terminar();
            return;
        }

        bienvenida();
        while (true) {
            System.out.print(Consola.negrita("uco> "));
            String linea = leer();
            if (linea == null || COMANDOS_SALIR.contains(linea.trim().toLowerCase(Locale.ROOT))) {
                break;
            }
            if (!linea.isBlank()) {
                ejecutar(linea);
            }
        }
        System.out.println(Consola.tenue("Hasta luego."));
        terminar();
    }

    private void ejecutar(String linea) {
        String[] partes = linea.trim().split("\\s+", 2);
        String comando = partes[0].toLowerCase(Locale.ROOT);
        String argumentos = partes.length > 1 ? partes[1].trim() : "";

        try {
            switch (comando) {
                case "ayuda", "help", "?" -> ayuda();
                case "resumen" -> resumen();
                case "usuarios", "users" -> usuarios(argumentos);
                case "pendientes" -> usuarios("PENDIENTE");
                case "notas" -> notas(argumentos);
                case "aprobar" -> cambiarEstado(argumentos, "aprobar");
                case "rechazar" -> cambiarEstado(argumentos, "rechazar");
                case "expulsar" -> cambiarEstado(argumentos, "expulsar");
                case "reactivar" -> cambiarEstado(argumentos, "reactivar");
                case "eliminar", "borrar" -> eliminar(argumentos);
                case "crear-usuario", "insertar-usuario" -> crearUsuario();
                case "tablas" -> tablas();
                case "describir" -> describir(argumentos);
                case "sql" -> sql(argumentos);
                default -> Consola.error("Comando desconocido: " + comando + ". Prueba 'ayuda'.");
            }
        } catch (ApiException ex) {
            Consola.error(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Consola.error(ex.getMessage());
        } catch (RuntimeException ex) {
            Consola.error(ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            if (System.getenv("UCO_CLI_DEBUG") != null) {
                ex.printStackTrace();
            }
        }
    }

    private void ayuda() {
        System.out.println("""
                
                  Consulta y administración
                    resumen                          Conteos de usuarios y notas
                    usuarios [rol] [estado]          Lista usuarios (ej.: usuarios PROFESOR ACTIVO)
                    pendientes                       Usuarios esperando aprobación
                    notas [idEstudiante]             Todas las notas o las de un estudiante

                  Aprobación y expulsión
                    aprobar <idUsuario>              PENDIENTE/RECHAZADO -> ACTIVO
                    rechazar <idUsuario>             PENDIENTE -> RECHAZADO
                    expulsar <idUsuario>             ACTIVO -> EXPULSADO
                    reactivar <idUsuario>            EXPULSADO/RECHAZADO -> ACTIVO
                    eliminar <idUsuario>             Borra el usuario, su perfil y sus notas

                  Insertar datos
                    crear-usuario                    Asistente interactivo (rol, aula, materia...)
                    sql <sentencia>                  SQL libre: SELECT/INSERT/UPDATE/DELETE/DDL

                  Esquema
                    tablas                           Tablas del esquema
                    describir <tabla>                Columnas, tipos y nulabilidad

                  Otros
                    ayuda                            Esta ayuda
                    salir                            Terminar
                
                  Nota: el SQL se ejecuta con el usuario de la aplicación. Si vas a insertar
                  usuarios a mano, usa 'crear-usuario' para que la contraseña quede con BCrypt.
                """);
    }

    private void resumen() {
        ResumenAdminResponse resumen = adminService.resumen();
        Consola.titulo("Resumen");
        Consola.tabla(
                List.of("MÉTRICA", "VALOR"),
                List.of(
                        List.of("Estudiantes", String.valueOf(resumen.estudiantes())),
                        List.of("Profesores", String.valueOf(resumen.profesores())),
                        List.of("Administradores", String.valueOf(resumen.administradores())),
                        List.of("Pendientes", String.valueOf(resumen.pendientes())),
                        List.of("Activos", String.valueOf(resumen.activos())),
                        List.of("Rechazados", String.valueOf(resumen.rechazados())),
                        List.of("Expulsados", String.valueOf(resumen.expulsados())),
                        List.of("Notas", String.valueOf(resumen.notas()))
                ));
    }

    private void usuarios(String filtro) {
        Rol rol = null;
        EstadoUsuario estado = null;
        for (String token : filtro.isBlank() ? List.<String>of() : List.of(filtro.toUpperCase(Locale.ROOT).split("\\s+"))) {
            try {
                rol = Rol.valueOf(token);
                continue;
            } catch (IllegalArgumentException ignorado) {
                // no es un rol
            }
            try {
                estado = EstadoUsuario.valueOf(token);
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Filtro desconocido: " + token + ". Usa un rol o un estado (PROFESOR, ACTIVO...)");
            }
        }

        List<UsuarioResponse> lista = adminService.listar(rol, estado);
        List<List<String>> filas = lista.stream()
                .map(usuario -> List.of(
                        String.valueOf(usuario.id()),
                        usuario.nombreCompleto(),
                        usuario.email(),
                        usuario.rol().name(),
                        usuario.estado().name(),
                        Objects.toString(usuario.aula(), "—"),
                        Objects.toString(usuario.materia(), Objects.toString(usuario.codigoEstudiantil(), "—"))))
                .toList();

        Consola.titulo("Usuarios (" + filas.size() + ")");
        Consola.tabla(List.of("ID", "NOMBRE", "CORREO", "ROL", "ESTADO", "AULA", "MATERIA/CÓDIGO"), filas);
    }

    private void notas(String filtro) {
        List<NotaResponse> lista;
        if (filtro.isBlank()) {
            lista = adminService.listarNotas();
        } else {
            long estudianteId = numero(filtro, "idEstudiante");
            lista = notas.findByEstudianteIdOrderByMateriaAscPeriodoAsc(estudianteId).stream()
                    .map(this::aRespuesta)
                    .toList();
        }

        List<List<String>> filas = lista.stream()
                .map(nota -> List.of(
                        String.valueOf(nota.id()),
                        nota.estudianteNombre(),
                        Objects.toString(nota.estudianteCodigo(), "—"),
                        nota.materia(),
                        nota.aula(),
                        nota.periodo(),
                        nota.valor().toPlainString(),
                        nota.profesorNombre()))
                .toList();

        Consola.titulo("Notas (" + filas.size() + ")");
        Consola.tabla(List.of("ID", "ESTUDIANTE", "CÓDIGO", "MATERIA", "AULA", "PERIODO", "NOTA", "PROFESOR"),
                filas);
    }

    private void cambiarEstado(String argumentos, String accion) {
        long id = numero(argumentos, "idUsuario");
        UsuarioResponse usuario = switch (accion) {
            case "aprobar" -> adminService.aprobar(id);
            case "rechazar" -> adminService.rechazar(id);
            case "expulsar" -> adminService.expulsar(id);
            case "reactivar" -> adminService.reactivar(id);
            default -> throw new IllegalStateException("Acción no soportada: " + accion);
        };
        Consola.ok(usuario.nombreCompleto() + " ahora está " + usuario.estado().name());
    }

    private void eliminar(String argumentos) {
        long id = numero(argumentos, "idUsuario");
        if (!confirmar("Se borrarán el usuario, su perfil y sus notas. ¿Continuar?")) {
            Consola.aviso("Cancelado.");
            return;
        }
        adminService.eliminar(id);
        Consola.ok("Usuario " + id + " eliminado.");
    }

    private void crearUsuario() {
        if (!interactivo) {
            throw new IllegalArgumentException(
                    "crear-usuario es interactivo: ejecuta ./scripts/db.sh sin argumentos, "
                            + "o usa 'sql insert ...' con un hash BCrypt ya generado");
        }
        System.out.println(Consola.tenue("Asistente de creación (Enter para aceptar el valor entre paréntesis)"));
        String email = pedir("Correo", null);
        if (email == null) {
            return;
        }
        String nombre = pedir("Nombre completo", null);
        String rolTexto = pedir("Rol (ADMIN/PROFESOR/ESTUDIANTE)", "ESTUDIANTE");
        Rol rol = Rol.valueOf(rolTexto.toUpperCase(Locale.ROOT));
        String password = pedir("Contraseña", "Demo123!");
        String estadoTexto = pedir("Estado (ACTIVO/PENDIENTE)", "ACTIVO");
        EstadoUsuario estado = EstadoUsuario.valueOf(estadoTexto.toUpperCase(Locale.ROOT));

        String aula = rol == Rol.ADMIN ? null : pedir("Aula", "A-101");
        String materia = rol == Rol.PROFESOR ? pedir("Materia", null) : null;
        String codigo = rol == Rol.ESTUDIANTE ? pedir("Código estudiantil", null) : null;

        UsuarioResponse creado = adminService.crear(new CrearUsuarioRequest(
                nombre, email, password, rol, estado, aula, materia, codigo));

        Consola.ok("Creado " + creado.rol().name() + " con id " + creado.id() + " (" + creado.estado().name() + ")");
    }

    private void tablas() {
        try (Connection conexion = dataSource.getConnection()) {
            String esquema = esquemaDe(conexion);
            List<String> nombres = new ArrayList<>();
            try (ResultSet resultado = conexion.getMetaData()
                    .getTables(conexion.getCatalog(), esquema, "%", new String[]{"TABLE"})) {
                while (resultado.next()) {
                    nombres.add(resultado.getString("TABLE_NAME"));
                }
            }
            nombres.sort(Comparator.naturalOrder());

            Consola.titulo("Tablas del esquema " + esquema);
            Consola.tabla(List.of("TABLA"), nombres.stream().map(List::of).toList());
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudieron leer las tablas: " + ex.getMessage());
        }
    }

    private void describir(String argumentos) {
        if (argumentos.isBlank()) {
            throw new IllegalArgumentException("Uso: describir <tabla>");
        }
        String tabla = argumentos.toUpperCase(Locale.ROOT);
        try (Connection conexion = dataSource.getConnection()) {
            String esquema = esquemaDe(conexion);
            List<List<String>> filas = new ArrayList<>();
            try (ResultSet resultado = conexion.getMetaData()
                    .getColumns(conexion.getCatalog(), esquema, tabla, "%")) {
                while (resultado.next()) {
                    filas.add(List.of(
                            resultado.getString("COLUMN_NAME"),
                            resultado.getString("TYPE_NAME"),
                            String.valueOf(resultado.getInt("COLUMN_SIZE")),
                            "YES".equals(resultado.getString("IS_NULLABLE")) ? "sí" : "no",
                            defectoDe(resultado)));
                }
            }
            if (filas.isEmpty()) {
                Consola.aviso("No existe la tabla " + tabla + " en el esquema " + esquema);
                return;
            }
            Consola.titulo("Tabla " + tabla);
            Consola.tabla(List.of("COLUMNA", "TIPO", "TAMAÑO", "NULO", "DEFECTO"), filas);
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo describir la tabla: " + ex.getMessage());
        }
    }

    private void sql(String sentencia) {
        if (sentencia.isBlank()) {
            throw new IllegalArgumentException("Uso: sql <sentencia SQL>");
        }

        String tipo = sentencia.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        switch (tipo) {
            case "select", "with" -> ejecutarConsulta(sentencia);
            case "insert", "update", "delete", "merge" -> {
                boolean sinFiltro = (tipo.equals("update") || tipo.equals("delete"))
                        && !sentencia.toLowerCase(Locale.ROOT).matches("(?s).*\\bwhere\\b.*");
                if (sinFiltro && !confirmar("La sentencia " + tipo.toUpperCase(Locale.ROOT)
                        + " no tiene WHERE y afecta toda la tabla. ¿Continuar?")) {
                    Consola.aviso("Cancelado.");
                    return;
                }
                int filas = jdbc.update(sentencia);
                Consola.ok(filas + " fila(s) afectadas.");
            }
            default -> {
                jdbc.execute(sentencia);
                Consola.ok("Sentencia ejecutada.");
            }
        }
    }

    private void ejecutarConsulta(String sentencia) {
        List<Map<String, Object>> resultados = jdbc.queryForList(sentencia);
        if (resultados.isEmpty()) {
            Consola.aviso("La consulta no devolvió filas.");
            return;
        }

        List<String> cabeceras = new ArrayList<>(resultados.getFirst().keySet());
        List<List<String>> filas = resultados.stream()
                .limit(FILAS_MAXIMAS)
                .map(fila -> cabeceras.stream()
                        .map(columna -> Objects.toString(fila.get(columna), "—"))
                        .toList())
                .toList();

        Consola.titulo("Resultado (" + resultados.size() + " fila(s))");
        Consola.tabla(cabeceras, filas);
        if (resultados.size() > FILAS_MAXIMAS) {
            Consola.aviso("Mostrando las primeras " + FILAS_MAXIMAS + " filas.");
        }
    }

    private void bienvenida() {
        System.out.println();
        System.out.println(Consola.negrita("UCO · consola interna de la base de datos"));
        try (Connection conexion = dataSource.getConnection()) {
            DatabaseMetaData meta = conexion.getMetaData();
            System.out.println(Consola.tenue("  " + meta.getDatabaseProductName() + " "
                    + meta.getDatabaseProductVersion() + " · " + meta.getURL()));
        } catch (SQLException ex) {
            Consola.aviso("No se pudo leer la conexión: " + ex.getMessage());
        }
        System.out.println(Consola.tenue("  Escribe 'ayuda' para ver los comandos, 'salir' para terminar."));
    }

    private NotaResponse aRespuesta(Nota nota) {
        return new NotaResponse(
                nota.getId(),
                nota.getEstudiante().getId(),
                nota.getEstudiante().getUsuario().getNombreCompleto(),
                nota.getEstudiante().getCodigoEstudiantil(),
                nota.getProfesor().getId(),
                nota.getProfesor().getUsuario().getNombreCompleto(),
                nota.getMateria(),
                nota.getAula(),
                nota.getPeriodo(),
                nota.getValor(),
                nota.getComentario(),
                nota.getActualizadaEn()
        );
    }

    private static String esquemaDe(Connection conexion) throws SQLException {
        String esquema = conexion.getSchema();
        return esquema != null ? esquema.toUpperCase(Locale.ROOT) : "UCO";
    }

    /** Oracle falla al leer COLUMN_DEF de columnas IDENTITY (ORA-17027); se ignora ese caso. */
    private static String defectoDe(ResultSet resultado) {
        try {
            return Objects.toString(resultado.getString("COLUMN_DEF"), "—");
        } catch (SQLException ex) {
            return "—";
        }
    }

    private static long numero(String texto, String campo) {
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El " + campo + " debe ser un número. Recibido: '" + texto + "'");
        }
    }

    private String pedir(String etiqueta, String defecto) {
        System.out.print("  " + etiqueta + (defecto != null ? " (" + defecto + ")" : "") + ": ");
        System.out.flush();
        String valor = leer();
        if (valor == null) {
            return defecto;
        }
        return valor.isBlank() ? defecto : valor.trim();
    }

    private boolean confirmar(String pregunta) {
        if (!interactivo) {
            Consola.aviso("Modo no interactivo: se asume 'sí' para: " + pregunta);
            return true;
        }
        String respuesta = pedir(pregunta + " [si/no]", "no");
        return respuesta != null && Set.of("si", "sí", "s", "yes", "y").contains(respuesta.toLowerCase(Locale.ROOT));
    }

    private String leer() {
        try {
            return entrada.readLine();
        } catch (IOException ex) {
            return null;
        }
    }

    private void terminar() {
        int codigo = SpringApplication.exit(contexto, () -> 0);
        System.exit(codigo);
    }
}
