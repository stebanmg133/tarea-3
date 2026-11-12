package co.edu.uco.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/** Perfil académico de un usuario con rol PROFESOR: dicta una materia en un aula. */
@Entity
@Table(
        name = "UCO_PROFESOR",
        uniqueConstraints = @UniqueConstraint(name = "UK_UCO_PROFESOR_USUARIO", columnNames = "USUARIO_ID")
)
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "AULA", nullable = false, length = 80)
    private String aula;

    @Column(name = "MATERIA", nullable = false, length = 80)
    private String materia;

    /** Constructor requerido por JPA. */
    protected Profesor() {
    }

    public Profesor(Usuario usuario, String aula, String materia) {
        this.usuario = Objects.requireNonNull(usuario, "usuario es obligatorio");
        this.aula = normalizar(aula);
        this.materia = normalizar(materia);
    }

    public void actualizarDatos(String nuevaAula, String nuevaMateria) {
        this.aula = normalizar(nuevaAula);
        this.materia = normalizar(nuevaMateria);
    }

    /** Un profesor solo puede calificar estudiantes de su propia aula. */
    public boolean dictaEn(String aulaEstudiante) {
        return aula.equalsIgnoreCase(aulaEstudiante);
    }

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El valor es obligatorio");
        }
        return valor.trim();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getAula() {
        return aula;
    }

    public String getMateria() {
        return materia;
    }
}
