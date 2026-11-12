package co.edu.uco.backend.web;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.dto.ActualizarUsuarioRequest;
import co.edu.uco.backend.dto.CrearUsuarioRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.ResumenAdminResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol,
                                        @RequestParam(required = false) EstadoUsuario estado) {
        return adminService.listar(rol, estado);
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return adminService.obtener(id);
    }

    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest solicitud) {
        UsuarioResponse creado = adminService.crear(solicitud);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creado.id())
                .toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/usuarios/{id}")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest solicitud) {
        return adminService.actualizar(id, solicitud);
    }

    @PatchMapping("/usuarios/{id}/aprobar")
    public UsuarioResponse aprobar(@PathVariable Long id) {
        return adminService.aprobar(id);
    }

    @PatchMapping("/usuarios/{id}/rechazar")
    public UsuarioResponse rechazar(@PathVariable Long id) {
        return adminService.rechazar(id);
    }

    @PatchMapping("/usuarios/{id}/expulsar")
    public UsuarioResponse expulsar(@PathVariable Long id) {
        return adminService.expulsar(id);
    }

    @PatchMapping("/usuarios/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Long id) {
        return adminService.reactivar(id);
    }

    @DeleteMapping("/usuarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        adminService.eliminar(id);
    }

    @GetMapping("/resumen")
    public ResumenAdminResponse resumen() {
        return adminService.resumen();
    }

    @GetMapping("/notas")
    public List<NotaResponse> notas() {
        return adminService.listarNotas();
    }
}
