package co.edu.uco.backend.service;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.dto.ActualizarUsuarioRequest;
import co.edu.uco.backend.dto.CrearUsuarioRequest;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.ResumenAdminResponse;
import co.edu.uco.backend.dto.UsuarioResponse;

import java.util.List;

public interface AdminService {

    List<UsuarioResponse> listar(Rol rol, EstadoUsuario estado);

    UsuarioResponse obtener(Long usuarioId);

    UsuarioResponse crear(CrearUsuarioRequest solicitud);

    UsuarioResponse actualizar(Long usuarioId, ActualizarUsuarioRequest solicitud);

    UsuarioResponse aprobar(Long usuarioId);

    UsuarioResponse rechazar(Long usuarioId);

    UsuarioResponse expulsar(Long usuarioId);

    UsuarioResponse reactivar(Long usuarioId);

    void eliminar(Long usuarioId);

    ResumenAdminResponse resumen();

    List<NotaResponse> listarNotas();
}
