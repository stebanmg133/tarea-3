package co.edu.uco.backend.service.impl;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Nota;
import co.edu.uco.backend.domain.Profesor;
import co.edu.uco.backend.dto.EstudianteResumenResponse;
import co.edu.uco.backend.dto.NotaRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.exception.AccesoDenegadoException;
import co.edu.uco.backend.exception.RecursoNoEncontradoException;
import co.edu.uco.backend.exception.ReglaNegocioException;
import co.edu.uco.backend.mapper.NotaMapper;
import co.edu.uco.backend.mapper.UsuarioMapper;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.NotaRepository;
import co.edu.uco.backend.repo.ProfesorRepository;
import co.edu.uco.backend.security.UsuarioActualService;
import co.edu.uco.backend.service.ProfesorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfesorServiceImpl implements ProfesorService {

    private final UsuarioActualService usuarioActual;
    private final ProfesorRepository profesores;
    private final EstudianteRepository estudiantes;
    private final NotaRepository notas;
    private final UsuarioMapper usuarioMapper;
    private final NotaMapper notaMapper;

    public ProfesorServiceImpl(UsuarioActualService usuarioActual,
                               ProfesorRepository profesores,
                               EstudianteRepository estudiantes,
                               NotaRepository notas,
                               UsuarioMapper usuarioMapper,
                               NotaMapper notaMapper) {
        this.usuarioActual = usuarioActual;
        this.profesores = profesores;
        this.estudiantes = estudiantes;
        this.notas = notas;
        this.usuarioMapper = usuarioMapper;
        this.notaMapper = notaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse perfil() {
        return usuarioMapper.aResponse(profesorActual().getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstudianteResumenResponse> misEstudiantes() {
        Profesor profesor = profesorActual();
        List<Estudiante> delAula = estudiantes
                .findByAulaIgnoreCaseAndUsuarioEstadoOrderByUsuarioNombreCompletoAsc(
                        profesor.getAula(), EstadoUsuario.ACTIVO);

        if (delAula.isEmpty()) {
            return List.of();
        }

        List<Long> ids = delAula.stream().map(Estudiante::getId).toList();
        Map<Long, BigDecimal> promedios = notas.promediosDe(ids).stream()
                .collect(Collectors.toMap(
                        NotaRepository.PromedioPorEstudiante::getEstudianteId,
                        NotaRepository.PromedioPorEstudiante::getPromedio,
                        (a, b) -> a,
                        HashMap::new));

        return delAula.stream()
                .map(estudiante -> usuarioMapper.aResumen(estudiante, promedios.get(estudiante.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaResponse> misNotas() {
        return notaMapper.aResponses(
                notas.findByProfesorIdOrderByEstudianteUsuarioNombreCompletoAsc(profesorActual().getId()));
    }

    @Override
    public NotaResponse crearNota(NotaRequest solicitud) {
        if (solicitud.estudianteId() == null) {
            throw new ReglaNegocioException("El estudianteId es obligatorio para crear una nota");
        }
        Profesor profesor = profesorActual();
        Estudiante estudiante = estudiantes.findById(solicitud.estudianteId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Estudiante", solicitud.estudianteId()));

        if (!estudiante.getUsuario().estaActivo()) {
            throw new ReglaNegocioException("El estudiante no está activo en la plataforma");
        }
        if (!profesor.dictaEn(estudiante.getAula())) {
            throw new AccesoDenegadoException(
                    "Solo puedes calificar estudiantes del aula " + profesor.getAula());
        }

        Nota nota = new Nota(estudiante, profesor, solicitud.periodo(), solicitud.valor(), solicitud.comentario());
        notas.save(nota);
        return notaMapper.aResponse(nota);
    }

    @Override
    public NotaResponse actualizarNota(Long notaId, NotaRequest solicitud) {
        Nota nota = notas.findByIdAndProfesorId(notaId, profesorActual().getId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Nota", notaId));

        nota.actualizar(solicitud.periodo(), solicitud.valor(), solicitud.comentario());
        return notaMapper.aResponse(nota);
    }

    @Override
    public void eliminarNota(Long notaId) {
        Nota nota = notas.findByIdAndProfesorId(notaId, profesorActual().getId())
                .orElseThrow(() -> RecursoNoEncontradoException.de("Nota", notaId));
        notas.delete(nota);
    }

    private Profesor profesorActual() {
        Long usuarioId = usuarioActual.id();
        return profesores.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("La cuenta no tiene perfil de profesor"));
    }
}
