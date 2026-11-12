package co.edu.uco.backend.service.impl;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Profesor;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.dto.LoginRequest;
import co.edu.uco.backend.dto.LoginResponse;
import co.edu.uco.backend.dto.RegistroEstudianteRequest;
import co.edu.uco.backend.dto.RegistroProfesorRequest;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.exception.CredencialesInvalidasException;
import co.edu.uco.backend.exception.CuentaNoHabilitadaException;
import co.edu.uco.backend.exception.ReglaNegocioException;
import co.edu.uco.backend.mapper.UsuarioMapper;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.ProfesorRepository;
import co.edu.uco.backend.repo.UsuarioRepository;
import co.edu.uco.backend.security.JwtService;
import co.edu.uco.backend.security.UsuarioActualService;
import co.edu.uco.backend.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarios;
    private final EstudianteRepository estudiantes;
    private final ProfesorRepository profesores;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioMapper mapper;
    private final UsuarioActualService usuarioActual;

    public AuthServiceImpl(UsuarioRepository usuarios,
                           EstudianteRepository estudiantes,
                           ProfesorRepository profesores,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           UsuarioMapper mapper,
                           UsuarioActualService usuarioActual) {
        this.usuarios = usuarios;
        this.estudiantes = estudiantes;
        this.profesores = profesores;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
        this.usuarioActual = usuarioActual;
    }

    @Override
    public UsuarioResponse registrarEstudiante(RegistroEstudianteRequest solicitud) {
        exigirCorreoDisponible(solicitud.email());
        if (estudiantes.existsByCodigoEstudiantilIgnoreCase(solicitud.codigoEstudiantil())) {
            throw new ReglaNegocioException("Ya existe un estudiante con el código "
                    + solicitud.codigoEstudiantil());
        }

        Usuario usuario = new Usuario(
                solicitud.nombreCompleto(),
                solicitud.email(),
                passwordEncoder.encode(solicitud.password()),
                Rol.ESTUDIANTE,
                EstadoUsuario.PENDIENTE
        );
        usuarios.save(usuario);
        estudiantes.save(new Estudiante(usuario, solicitud.aula(), solicitud.codigoEstudiantil()));

        return mapper.aResponse(usuario);
    }

    @Override
    public UsuarioResponse registrarProfesor(RegistroProfesorRequest solicitud) {
        exigirCorreoDisponible(solicitud.email());

        Usuario usuario = new Usuario(
                solicitud.nombreCompleto(),
                solicitud.email(),
                passwordEncoder.encode(solicitud.password()),
                Rol.PROFESOR,
                EstadoUsuario.PENDIENTE
        );
        usuarios.save(usuario);
        profesores.save(new Profesor(usuario, solicitud.aula(), solicitud.materia()));

        return mapper.aResponse(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest solicitud) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(solicitud.email())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(solicitud.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }
        if (!usuario.estaActivo()) {
            throw new CuentaNoHabilitadaException(usuario.getEstado());
        }

        JwtService.TokenJwt token = jwtService.generar(usuario);
        return new LoginResponse(
                token.valor(),
                "Bearer",
                token.expiraEn(),
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getEstado()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse usuarioActual() {
        return mapper.aResponse(usuarioActual.requerir());
    }

    private void exigirCorreoDisponible(String email) {
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + email);
        }
    }
}
