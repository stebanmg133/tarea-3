package co.edu.uco.backend.dto;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Creación directa de un usuario por parte del administrador.
 * Si no se envía estado, el usuario queda ACTIVO (el admin ya lo está creando).
 */
public record CrearUsuarioRequest(

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

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        EstadoUsuario estado,

        @Size(max = 80, message = "El aula no puede superar 80 caracteres")
        String aula,

        @Size(max = 80, message = "La materia no puede superar 80 caracteres")
        String materia,

        @Size(max = 30, message = "El código no puede superar 30 caracteres")
        String codigoEstudiantil
) {
}
