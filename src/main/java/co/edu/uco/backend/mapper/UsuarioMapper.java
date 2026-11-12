package co.edu.uco.backend.mapper;

import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Profesor;
import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.dto.EstudianteResumenResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.ProfesorRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Traduce entidades {@link Usuario} a DTOs, agregando los datos del perfil académico. */
@Component
public class UsuarioMapper {

    private final EstudianteRepository estudiantes;
    private final ProfesorRepository profesores;

    public UsuarioMapper(EstudianteRepository estudiantes, ProfesorRepository profesores) {
        this.estudiantes = estudiantes;
        this.profesores = profesores;
    }

    public UsuarioResponse aResponse(Usuario usuario) {
        Estudiante estudiante = estudiantes.findByUsuarioId(usuario.getId()).orElse(null);
        Profesor profesor = profesores.findByUsuarioId(usuario.getId()).orElse(null);
        return construir(usuario, estudiante, profesor);
    }

    /** Versión por lote: evita el problema N+1 al listar usuarios. */
    public List<UsuarioResponse> aResponses(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            return List.of();
        }
        List<Long> ids = usuarios.stream().map(Usuario::getId).toList();
        Map<Long, Estudiante> estudiantesPorUsuario = estudiantes.findByUsuarioIdIn(ids).stream()
                .collect(Collectors.toMap(e -> e.getUsuario().getId(), Function.identity()));
        Map<Long, Profesor> profesoresPorUsuario = profesores.findByUsuarioIdIn(ids).stream()
                .collect(Collectors.toMap(p -> p.getUsuario().getId(), Function.identity()));

        return usuarios.stream()
                .map(usuario -> construir(usuario,
                        estudiantesPorUsuario.get(usuario.getId()),
                        profesoresPorUsuario.get(usuario.getId())))
                .toList();
    }

    public EstudianteResumenResponse aResumen(Estudiante estudiante, BigDecimal promedio) {
        return new EstudianteResumenResponse(
                estudiante.getId(),
                estudiante.getUsuario().getNombreCompleto(),
                estudiante.getUsuario().getEmail(),
                estudiante.getAula(),
                estudiante.getCodigoEstudiantil(),
                promedio
        );
    }

    private static UsuarioResponse construir(Usuario usuario, Estudiante estudiante, Profesor profesor) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getEstado(),
                usuario.getCreadoEn(),
                estudiante != null ? estudiante.getAula() : (profesor != null ? profesor.getAula() : null),
                profesor != null ? profesor.getMateria() : null,
                estudiante != null ? estudiante.getCodigoEstudiantil() : null
        );
    }
}
