package co.edu.uco.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Creación o actualización de una nota por parte de un profesor. */
public record NotaRequest(

        /** Solo se usa al crear; al actualizar se ignora. */
        Long estudianteId,

        @NotBlank(message = "El periodo es obligatorio")
        @Size(max = 20, message = "El periodo no puede superar 20 caracteres")
        String periodo,

        @NotNull(message = "La nota es obligatoria")
        @DecimalMin(value = "0.0", message = "La nota mínima es 0.0")
        @DecimalMax(value = "5.0", message = "La nota máxima es 5.0")
        BigDecimal valor,

        @Size(max = 300, message = "El comentario no puede superar 300 caracteres")
        String comentario
) {
}
