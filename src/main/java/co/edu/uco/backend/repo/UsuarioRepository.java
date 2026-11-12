package co.edu.uco.backend.repo;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByRolOrderByNombreCompletoAsc(Rol rol);

    List<Usuario> findByEstadoOrderByCreadoEnAsc(EstadoUsuario estado);

    List<Usuario> findByRolAndEstadoOrderByNombreCompletoAsc(Rol rol, EstadoUsuario estado);

    List<Usuario> findAllByOrderByCreadoEnAsc();

    long countByRol(Rol rol);

    long countByEstado(EstadoUsuario estado);
}
