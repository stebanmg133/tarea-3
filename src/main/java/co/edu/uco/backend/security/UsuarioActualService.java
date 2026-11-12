package co.edu.uco.backend.security;

import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.exception.AccesoDenegadoException;
import co.edu.uco.backend.repo.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** Resuelve el usuario autenticado de la petición actual. */
@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarios;

    public UsuarioActualService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario requerir() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new AccesoDenegadoException("No hay una sesión activa");
        }
        return usuarios.findByEmailIgnoreCase(autenticacion.getName())
                .orElseThrow(() -> new AccesoDenegadoException("El usuario de la sesión ya no existe"));
    }

    public Long id() {
        return requerir().getId();
    }
}
