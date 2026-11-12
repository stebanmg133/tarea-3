package co.edu.uco.backend.config;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Nota;
import co.edu.uco.backend.domain.Profesor;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.NotaRepository;
import co.edu.uco.backend.repo.ProfesorRepository;
import co.edu.uco.backend.repo.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Datos de ejemplo para probar la plataforma.
 *
 * <p>Se cargan solo con el perfil {@code demo} (en {@code dev} viene activado por
 * defecto). Es idempotente: si un correo de ejemplo ya existe, no se duplica.</p>
 */
@Component
@Profile("demo")
public class DatosDemoRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemoRunner.class);

    /** Contraseña compartida por todas las cuentas de ejemplo. */
    public static final String PASSWORD_DEMO = "Demo123!";

    private final UsuarioRepository usuarios;
    private final EstudianteRepository estudiantes;
    private final ProfesorRepository profesores;
    private final NotaRepository notas;
    private final PasswordEncoder passwordEncoder;

    public DatosDemoRunner(UsuarioRepository usuarios,
                           EstudianteRepository estudiantes,
                           ProfesorRepository profesores,
                           NotaRepository notas,
                           PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.estudiantes = estudiantes;
        this.profesores = profesores;
        this.notas = notas;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        usuario("coordinacion@uco.edu.co", "Coordinación Académica", Rol.ADMIN, EstadoUsuario.ACTIVO);

        Profesor laura = profesor("laura.gomez@uco.edu.co", "Laura Gómez",
                "A-101", "Bases de Datos", EstadoUsuario.ACTIVO);
        Profesor andres = profesor("andres.ramirez@uco.edu.co", "Andrés Ramírez",
                "A-102", "Programación Web", EstadoUsuario.ACTIVO);
        Profesor monica = profesor("monica.salazar@uco.edu.co", "Mónica Salazar",
                "B-201", "Cálculo Diferencial", EstadoUsuario.ACTIVO);
        profesor("jorge.cardenas@uco.edu.co", "Jorge Cárdenas",
                "A-103", "Redes de Datos", EstadoUsuario.PENDIENTE);

        Estudiante juan = estudiante("juan.perez@uco.edu.co", "Juan Pérez",
                "A-101", "EST-2026-001", EstadoUsuario.ACTIVO);
        Estudiante sofia = estudiante("sofia.torres@uco.edu.co", "Sofía Torres",
                "A-101", "EST-2026-002", EstadoUsuario.ACTIVO);
        Estudiante camilo = estudiante("camilo.rojas@uco.edu.co", "Camilo Rojas",
                "A-102", "EST-2026-003", EstadoUsuario.ACTIVO);
        Estudiante valentina = estudiante("valentina.diaz@uco.edu.co", "Valentina Díaz",
                "A-102", "EST-2026-004", EstadoUsuario.ACTIVO);
        Estudiante diego = estudiante("diego.herrera@uco.edu.co", "Diego Herrera",
                "B-201", "EST-2026-005", EstadoUsuario.ACTIVO);
        estudiante("mariana.lopez@uco.edu.co", "Mariana López",
                "A-101", "EST-2026-006", EstadoUsuario.PENDIENTE);
        estudiante("santiago.munoz@uco.edu.co", "Santiago Muñoz",
                "A-102", "EST-2026-007", EstadoUsuario.EXPULSADO);

        nota(laura, juan, "2026-1", "4.5", "Excelente sustentación del proyecto");
        nota(laura, sofia, "2026-1", "2.9", "Debe reforzar consultas con JOIN");
        nota(andres, camilo, "2026-1", "3.7", "Cumplió con los requisitos");
        nota(andres, valentina, "2026-1", "4.9", "Trabajo destacado");
        nota(monica, diego, "2026-1", "3.2", "Aprobó con dificultades en derivadas");

        log.info("Datos de ejemplo cargados. Todas las cuentas demo usan la contraseña {}", PASSWORD_DEMO);
    }

    private Usuario usuario(String email, String nombre, Rol rol, EstadoUsuario estado) {
        return usuarios.findByEmailIgnoreCase(email)
                .orElseGet(() -> usuarios.save(new Usuario(
                        nombre, email, passwordEncoder.encode(PASSWORD_DEMO), rol, estado)));
    }

    private Profesor profesor(String email, String nombre, String aula, String materia, EstadoUsuario estado) {
        Usuario usuario = usuario(email, nombre, Rol.PROFESOR, estado);
        return profesores.findByUsuarioId(usuario.getId())
                .orElseGet(() -> profesores.save(new Profesor(usuario, aula, materia)));
    }

    private Estudiante estudiante(String email, String nombre, String aula, String codigo, EstadoUsuario estado) {
        Usuario usuario = usuario(email, nombre, Rol.ESTUDIANTE, estado);
        return estudiantes.findByUsuarioId(usuario.getId())
                .orElseGet(() -> estudiantes.save(new Estudiante(usuario, aula, codigo)));
    }

    private void nota(Profesor profesor, Estudiante estudiante, String periodo, String valor, String comentario) {
        boolean yaExiste = notas.findByEstudianteIdOrderByMateriaAscPeriodoAsc(estudiante.getId()).stream()
                .anyMatch(nota -> nota.getPeriodo().equals(periodo)
                        && nota.getMateria().equals(profesor.getMateria()));

        if (!yaExiste) {
            notas.save(new Nota(estudiante, profesor, periodo, new BigDecimal(valor), comentario));
        }
    }
}
