package co.edu.uco.backend.repo;

import co.edu.uco.backend.domain.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {

    Optional<Profesor> findByUsuarioId(Long usuarioId);

    Optional<Profesor> findByUsuarioEmailIgnoreCase(String email);

    List<Profesor> findByUsuarioIdIn(Collection<Long> usuariosIds);
}
