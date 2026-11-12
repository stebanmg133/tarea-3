package co.edu.uco.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para que un profesor solicite su registro (incluye aula y materia). */
public record RegistroProfesorRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 160, message = "El correo no puede superar 160 caracteres")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        @NotBlank(message = "El aula es obligatoria")
        @Size(max = 80, message = "El aula no puede superar 80 caracteres")
        String aula,

        @NotBlank(message = "La materia es obligatoria")
        @Size(max = 80, message = "La materia no puede superar 80 caracteres")
        String materia
) {
}
