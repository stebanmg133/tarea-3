package co.edu.uco.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.Objects;

/**
 * Usuario de la plataforma (profesor, estudiante o administrador).
 *
 * <p>Es una entidad rica: el cambio de estado y de datos pasa por métodos del dominio
 * en lugar de setters públicos, para que las reglas queden en un solo sitio.</p>
 */
@Entity
@Table(
        name = "UCO_USUARIO",
        uniqueConstraints = @UniqueConstraint(name = "UK_UCO_USUARIO_EMAIL", columnNames = "EMAIL")
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NOMBRE_COMPLETO", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "EMAIL", nullable = false, length = 160)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROL", nullable = false, length = 20)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(name = "CREADO_EN", nullable = false)
    private Instant creadoEn;

    /** Constructor requerido por JPA. */
    protected Usuario() {
    }

    public Usuario(String nombreCompleto, String email, String passwordHash, Rol rol, EstadoUsuario estado) {
        this.nombreCompleto = requireTexto(nombreCompleto, "nombreCompleto");
        this.email = requireTexto(email, "email").toLowerCase();
        this.passwordHash = requireTexto(passwordHash, "passwordHash");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
        this.estado = Objects.requireNonNull(estado, "estado es obligatorio");
        this.creadoEn = Instant.now();
    }

    public void actualizarNombre(String nuevoNombre) {
        this.nombreCompleto = requireTexto(nuevoNombre, "nombreCompleto");
    }

    public void cambiarEstado(EstadoUsuario nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "estado es obligatorio");
    }

    public boolean estaActivo() {
        return estado == EstadoUsuario.ACTIVO;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMIN;
    }

    private static String requireTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        return valor.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    @Override
    public boolean equals(Object otro) {
        if (this == otro) {
            return true;
        }
        if (!(otro instanceof Usuario usuario)) {
            return false;
        }
        return id != null && id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
