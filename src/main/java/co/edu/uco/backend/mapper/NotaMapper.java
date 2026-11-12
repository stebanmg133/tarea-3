package co.edu.uco.backend.mapper;

import co.edu.uco.backend.domain.Nota;
import co.edu.uco.backend.dto.NotaResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotaMapper {

    public NotaResponse aResponse(Nota nota) {
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

    public List<NotaResponse> aResponses(List<Nota> notas) {
        return notas.stream().map(this::aResponse).toList();
    }
}
