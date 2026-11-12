package co.edu.uco.backend.service;

import co.edu.uco.backend.dto.EstudianteResumenResponse;
import co.edu.uco.backend.dto.NotaRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.UsuarioResponse;

import java.util.List;

/** Casos de uso del profesor: sus estudiantes del aula y la gestión de notas. */
public interface ProfesorService {

    UsuarioResponse perfil();

    List<EstudianteResumenResponse> misEstudiantes();

    List<NotaResponse> misNotas();

    NotaResponse crearNota(NotaRequest solicitud);

    NotaResponse actualizarNota(Long notaId, NotaRequest solicitud);

    void eliminarNota(Long notaId);
}
