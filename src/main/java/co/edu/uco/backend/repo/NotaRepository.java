package co.edu.uco.backend.repo;

import co.edu.uco.backend.domain.Nota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NotaRepository extends JpaRepository<Nota, Long> {

    List<Nota> findByEstudianteIdOrderByMateriaAscPeriodoAsc(Long estudianteId);

    List<Nota> findByProfesorIdOrderByEstudianteUsuarioNombreCompletoAsc(Long profesorId);

    Optional<Nota> findByIdAndProfesorId(Long id, Long profesorId);

    List<Nota> findAllByOrderByActualizadaEnDesc();

    void deleteByProfesorId(Long profesorId);

    void deleteByEstudianteId(Long estudianteId);

    @Query("""
            select n.estudiante.id as estudianteId, avg(n.valor) as promedio
              from Nota n
             where n.estudiante.id in :estudiantesIds
             group by n.estudiante.id
            """)
    List<PromedioPorEstudiante> promediosDe(@Param("estudiantesIds") Collection<Long> estudiantesIds);

    /** Proyección con el promedio de un estudiante. */
    interface PromedioPorEstudiante {
        Long getEstudianteId();

        BigDecimal getPromedio();
    }
}
