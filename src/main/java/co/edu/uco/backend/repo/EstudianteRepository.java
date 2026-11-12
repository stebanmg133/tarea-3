package co.edu.uco.backend.repo;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {

    Optional<Estudiante> findByUsuarioId(Long usuarioId);

    Optional<Estudiante> findByUsuarioEmailIgnoreCase(String email);

    boolean existsByCodigoEstudiantilIgnoreCase(String codigoEstudiantil);

    List<Estudiante> findByUsuarioIdIn(Collection<Long> usuariosIds);

    List<Estudiante> findByAulaIgnoreCaseOrderByUsuarioNombreCompletoAsc(String aula);

    List<Estudiante> findByAulaIgnoreCaseAndUsuarioEstadoOrderByUsuarioNombreCompletoAsc(
            String aula, EstadoUsuario estado);
}
