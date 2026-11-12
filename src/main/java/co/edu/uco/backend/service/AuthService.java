package co.edu.uco.backend.service;

import co.edu.uco.backend.dto.LoginRequest;
import co.edu.uco.backend.dto.LoginResponse;
import co.edu.uco.backend.dto.RegistroEstudianteRequest;
import co.edu.uco.backend.dto.RegistroProfesorRequest;
import co.edu.uco.backend.dto.UsuarioResponse;

public interface AuthService {

    /** Registra un estudiante en estado PENDIENTE (requiere aprobación del admin). */
    UsuarioResponse registrarEstudiante(RegistroEstudianteRequest solicitud);

    /** Registra un profesor (con aula y materia) en estado PENDIENTE. */
    UsuarioResponse registrarProfesor(RegistroProfesorRequest solicitud);

    LoginResponse login(LoginRequest solicitud);

    UsuarioResponse usuarioActual();
}
