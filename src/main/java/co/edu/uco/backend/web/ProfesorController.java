package co.edu.uco.backend.web;

import co.edu.uco.backend.dto.EstudianteResumenResponse;
import co.edu.uco.backend.dto.MensajeResponse;
import co.edu.uco.backend.dto.NotaRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.service.ProfesorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/profesor")
@PreAuthorize("hasRole('PROFESOR')")
public class ProfesorController {

    private final ProfesorService profesorService;

    public ProfesorController(ProfesorService profesorService) {
        this.profesorService = profesorService;
    }

    @GetMapping("/perfil")
    public UsuarioResponse perfil() {
        return profesorService.perfil();
    }

    @GetMapping("/estudiantes")
    public List<EstudianteResumenResponse> estudiantes() {
        return profesorService.misEstudiantes();
    }

    @GetMapping("/notas")
    public List<NotaResponse> notas() {
        return profesorService.misNotas();
    }

    @PostMapping("/notas")
    @ResponseStatus(HttpStatus.CREATED)
    public NotaResponse crearNota(@Valid @RequestBody NotaRequest solicitud) {
        return profesorService.crearNota(solicitud);
    }

    @PutMapping("/notas/{id}")
    public NotaResponse actualizarNota(@PathVariable Long id, @Valid @RequestBody NotaRequest solicitud) {
        return profesorService.actualizarNota(id, solicitud);
    }

    @DeleteMapping("/notas/{id}")
    public MensajeResponse eliminarNota(@PathVariable Long id) {
        profesorService.eliminarNota(id);
        return new MensajeResponse("Nota eliminada correctamente");
    }
}
