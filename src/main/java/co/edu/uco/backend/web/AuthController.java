package co.edu.uco.backend.web;

import co.edu.uco.backend.dto.LoginRequest;
import co.edu.uco.backend.dto.LoginResponse;
import co.edu.uco.backend.dto.RegistroEstudianteRequest;
import co.edu.uco.backend.dto.RegistroProfesorRequest;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro/estudiante")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrarEstudiante(@Valid @RequestBody RegistroEstudianteRequest solicitud) {
        return authService.registrarEstudiante(solicitud);
    }

    @PostMapping("/registro/profesor")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrarProfesor(@Valid @RequestBody RegistroProfesorRequest solicitud) {
        return authService.registrarProfesor(solicitud);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest solicitud) {
        return authService.login(solicitud);
    }

    @GetMapping("/me")
    public UsuarioResponse usuarioActual() {
        return authService.usuarioActual();
    }
}
