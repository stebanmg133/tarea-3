package co.edu.uco.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record NotaResponse(
        Long id,
        Long estudianteId,
        String estudianteNombre,
        String estudianteCodigo,
        Long profesorId,
        String profesorNombre,
        String materia,
        String aula,
        String periodo,
        BigDecimal valor,
        String comentario,
        Instant actualizadaEn
) {
}
