package co.edu.uco.backend.service.impl;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Profesor;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.dto.ActualizarUsuarioRequest;
import co.edu.uco.backend.dto.CrearUsuarioRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.ResumenAdminResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.exception.RecursoNoEncontradoException;
import co.edu.uco.backend.exception.ReglaNegocioException;
import co.edu.uco.backend.mapper.NotaMapper;
import co.edu.uco.backend.mapper.UsuarioMapper;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.NotaRepository;
import co.edu.uco.backend.repo.ProfesorRepository;
import co.edu.uco.backend.repo.UsuarioRepository;
import co.edu.uco.backend.security.UsuarioActualService;
import co.edu.uco.backend.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UsuarioRepository usuarios;
    private final EstudianteRepository estudiantes;
    private final ProfesorRepository profesores;
    private final NotaRepository notas;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;
    private final NotaMapper notaMapper;
    private final UsuarioActualService usuarioActual;

    public AdminServiceImpl(UsuarioRepository usuarios,
                            EstudianteRepository estudiantes,
                            ProfesorRepository profesores,
                            NotaRepository notas,
                            PasswordEncoder passwordEncoder,
                            UsuarioMapper usuarioMapper,
                            NotaMapper notaMapper,
                            UsuarioActualService usuarioActual) {
        this.usuarios = usuarios;
        this.estudiantes = estudiantes;
        this.profesores = profesores;
        this.notas = notas;
        this.passwordEncoder = passwordEncoder;
        this.usuarioMapper = usuarioMapper;
        this.notaMapper = notaMapper;
        this.usuarioActual = usuarioActual;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(Rol rol, EstadoUsuario estado) {
        List<Usuario> resultado;
        if (rol != null && estado != null) {
            resultado = usuarios.findByRolAndEstadoOrderByNombreCompletoAsc(rol, estado);
        } else if (rol != null) {
            resultado = usuarios.findByRolOrderByNombreCompletoAsc(rol);
        } else if (estado != null) {
            resultado = usuarios.findByEstadoOrderByCreadoEnAsc(estado);
        } else {
            resultado = usuarios.findAllByOrderByCreadoEnAsc();
        }
        return usuarioMapper.aResponses(resultado);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long usuarioId) {
        return usuarioMapper.aResponse(buscar(usuarioId));
    }

    @Override
    public UsuarioResponse crear(CrearUsuarioRequest solicitud) {
        if (usuarios.existsByEmailIgnoreCase(solicitud.email())) {
            throw new ReglaNegocioException("Ya existe una cuenta con el correo " + solicitud.email());
        }

        EstadoUsuario estado = solicitud.estado() != null ? solicitud.estado() : EstadoUsuario.ACTIVO;
        Usuario usuario = new Usuario(
                solicitud.nombreCompleto(),
                solicitud.email(),
                passwordEncoder.encode(solicitud.password()),
                solicitud.rol(),
                estado
        );
        usuarios.save(usuario);
        crearPerfil(usuario, solicitud);
        return usuarioMapper.aResponse(usuario);
    }

    @Override
    public UsuarioResponse actualizar(Long usuarioId, ActualizarUsuarioRequest solicitud) {
        Usuario usuario = buscar(usuarioId);

        if (solicitud.nombreCompleto() != null) {
            usuario.actualizarNombre(solicitud.nombreCompleto());
        }
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            Estudiante estudiante = estudiantes.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> RecursoNoEncontradoException.de("Perfil de estudiante", usuarioId));
            String aula = solicitud.aula() != null ? solicitud.aula() : estudiante.getAula();
            String codigo = solicitud.codigoEstudiantil() != null
                    ? solicitud.codigoEstudiantil()
                    : estudiante.getCodigoEstudiantil();
            estudiante.actualizarDatos(aula, codigo);
        } else if (usuario.getRol() == Rol.PROFESOR) {
            Profesor profesor = profesores.findByUsuarioId(usuarioId)
                    .orElseThrow(() -> RecursoNoEncontradoException.de("Perfil de profesor", usuarioId));
            String aula = solicitud.aula() != null ? solicitud.aula() : profesor.getAula();
            String materia = solicitud.materia() != null ? solicitud.materia() : profesor.getMateria();
            profesor.actualizarDatos(aula, materia);
        }

        return usuarioMapper.aResponse(usuario);
    }

    @Override
    public UsuarioResponse aprobar(Long usuarioId) {
        return cambiarEstado(usuarioId, EstadoUsuario.ACTIVO,
                EnumSet.of(EstadoUsuario.PENDIENTE, EstadoUsuario.RECHAZADO), "aprobar");
    }

    @Override
    public UsuarioResponse rechazar(Long usuarioId) {
        return cambiarEstado(usuarioId, EstadoUsuario.RECHAZADO,
                EnumSet.of(EstadoUsuario.PENDIENTE), "rechazar");
    }

    @Override
    public UsuarioResponse expulsar(Long usuarioId) {
        return cambiarEstado(usuarioId, EstadoUsuario.EXPULSADO,
                EnumSet.of(EstadoUsuario.ACTIVO), "expulsar");
    }

    @Override
    public UsuarioResponse reactivar(Long usuarioId) {
        return cambiarEstado(usuarioId, EstadoUsuario.ACTIVO,
                EnumSet.of(EstadoUsuario.EXPULSADO, EstadoUsuario.RECHAZADO), "reactivar");
    }

    @Override
    public void eliminar(Long usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getId().equals(usuarioActual.id())) {
            throw new ReglaNegocioException("No puedes eliminar tu propia cuenta");
        }
        if (usuario.esAdministrador()) {
            throw new ReglaNegocioException("No se puede eliminar a un administrador");
        }

        estudiantes.findByUsuarioId(usuarioId).ifPresent(estudiante -> {
            notas.deleteByEstudianteId(estudiante.getId());
            estudiantes.delete(estudiante);
        });
        profesores.findByUsuarioId(usuarioId).ifPresent(profesor -> {
            notas.deleteByProfesorId(profesor.getId());
            profesores.delete(profesor);
        });
        usuarios.delete(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenAdminResponse resumen() {
        return new ResumenAdminResponse(
                usuarios.countByRol(Rol.ESTUDIANTE),
                usuarios.countByRol(Rol.PROFESOR),
                usuarios.countByRol(Rol.ADMIN),
                usuarios.countByEstado(EstadoUsuario.PENDIENTE),
                usuarios.countByEstado(EstadoUsuario.ACTIVO),
                usuarios.countByEstado(EstadoUsuario.RECHAZADO),
                usuarios.countByEstado(EstadoUsuario.EXPULSADO),
                notas.count()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaResponse> listarNotas() {
        return notaMapper.aResponses(notas.findAllByOrderByActualizadaEnDesc());
    }

    private UsuarioResponse cambiarEstado(Long usuarioId, EstadoUsuario nuevoEstado,
                                          Set<EstadoUsuario> estadosPermitidos, String accion) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.esAdministrador()) {
            throw new ReglaNegocioException("No se puede " + accion + " a un administrador");
        }
        if (usuario.getId().equals(usuarioActual.id())) {
            throw new ReglaNegocioException("No puedes cambiar el estado de tu propia cuenta");
        }
        if (!estadosPermitidos.contains(usuario.getEstado())) {
            throw new ReglaNegocioException("No se puede " + accion + " un usuario en estado "
                    + usuario.getEstado());
        }
        usuario.cambiarEstado(nuevoEstado);
        return usuarioMapper.aResponse(usuario);
    }

    private void crearPerfil(Usuario usuario, CrearUsuarioRequest solicitud) {
        switch (usuario.getRol()) {
            case ESTUDIANTE -> {
                exigirDato(solicitud.aula(), "aula");
                exigirDato(solicitud.codigoEstudiantil(), "codigoEstudiantil");
                estudiantes.save(new Estudiante(usuario, solicitud.aula(), solicitud.codigoEstudiantil()));
            }
            case PROFESOR -> {
                exigirDato(solicitud.aula(), "aula");
                exigirDato(solicitud.materia(), "materia");
                profesores.save(new Profesor(usuario, solicitud.aula(), solicitud.materia()));
            }
            case ADMIN -> {
                // El administrador no tiene perfil académico.
            }
        }
    }

    private static void exigirDato(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio para ese rol");
        }
    }

    private Usuario buscar(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .orElseThrow(() -> RecursoNoEncontradoException.de("Usuario", usuarioId));
    }
}
