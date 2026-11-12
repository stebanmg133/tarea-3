package co.edu.uco.backend.dto;

import java.math.BigDecimal;

public record PromedioResponse(Long estudianteId, long cantidadNotas, BigDecimal promedio) {
}
