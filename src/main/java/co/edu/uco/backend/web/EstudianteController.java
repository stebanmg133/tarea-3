package co.edu.uco.backend.web;

import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.PromedioResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.service.EstudianteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estudiante")
@PreAuthorize("hasRole('ESTUDIANTE')")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil() {
        return estudianteService.perfil();
    }

    @GetMapping("/notas")
    public List<NotaResponse> notas() {
        return estudianteService.misNotas();
    }

    @GetMapping("/promedio")
    public PromedioResponse promedio() {
        return estudianteService.miPromedio();
    }
}
