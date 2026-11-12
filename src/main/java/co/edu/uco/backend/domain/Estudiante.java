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

/** Perfil académico de un usuario con rol ESTUDIANTE. */
@Entity
@Table(
        name = "UCO_ESTUDIANTE",
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_UCO_ESTUDIANTE_USUARIO", columnNames = "USUARIO_ID"),
                @UniqueConstraint(name = "UK_UCO_ESTUDIANTE_CODIGO", columnNames = "CODIGO_ESTUDIANTIL")
        }
)
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(name = "AULA", nullable = false, length = 80)
    private String aula;

    @Column(name = "CODIGO_ESTUDIANTIL", nullable = false, length = 30)
    private String codigoEstudiantil;

    /** Constructor requerido por JPA. */
    protected Estudiante() {
    }

    public Estudiante(Usuario usuario, String aula, String codigoEstudiantil) {
        this.usuario = Objects.requireNonNull(usuario, "usuario es obligatorio");
        this.aula = normalizar(aula);
        this.codigoEstudiantil = normalizar(codigoEstudiantil);
    }

    public void actualizarDatos(String nuevaAula, String nuevoCodigo) {
        this.aula = normalizar(nuevaAula);
        this.codigoEstudiantil = normalizar(nuevoCodigo);
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

    public String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }
}
