package co.edu.uco.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** Nota que un profesor registra a un estudiante en una materia/periodo. */
@Entity
@Table(name = "UCO_NOTA")
public class Nota {

    private static final BigDecimal NOTA_MINIMA = new BigDecimal("0.0");
    private static final BigDecimal NOTA_MAXIMA = new BigDecimal("5.0");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ESTUDIANTE_ID", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "PROFESOR_ID", nullable = false)
    private Profesor profesor;

    @Column(name = "MATERIA", nullable = false, length = 80)
    private String materia;

    @Column(name = "AULA", nullable = false, length = 80)
    private String aula;

    @Column(name = "PERIODO", nullable = false, length = 20)
    private String periodo;

    @Column(name = "VALOR", nullable = false, precision = 4, scale = 2)
    private BigDecimal valor;

    @Column(name = "COMENTARIO", length = 300)
    private String comentario;

    @Column(name = "ACTUALIZADA_EN", nullable = false)
    private Instant actualizadaEn;

    /** Constructor requerido por JPA. */
    protected Nota() {
    }

    public Nota(Estudiante estudiante, Profesor profesor, String periodo, BigDecimal valor, String comentario) {
        this.estudiante = Objects.requireNonNull(estudiante, "estudiante es obligatorio");
        this.profesor = Objects.requireNonNull(profesor, "profesor es obligatorio");
        this.materia = profesor.getMateria();
        this.aula = profesor.getAula();
        this.periodo = validarPeriodo(periodo);
        this.valor = validarValor(valor);
        this.comentario = normalizarOpcional(comentario);
        this.actualizadaEn = Instant.now();
    }

    public void actualizar(String nuevoPeriodo, BigDecimal nuevoValor, String nuevoComentario) {
        this.periodo = validarPeriodo(nuevoPeriodo);
        this.valor = validarValor(nuevoValor);
        this.comentario = normalizarOpcional(nuevoComentario);
        this.actualizadaEn = Instant.now();
    }

    public boolean perteneceA(Long profesorId) {
        return profesor.getId() != null && profesor.getId().equals(profesorId);
    }

    private static String validarPeriodo(String periodo) {
        if (periodo == null || periodo.isBlank()) {
            throw new IllegalArgumentException("El periodo es obligatorio");
        }
        return periodo.trim();
    }

    private static BigDecimal validarValor(BigDecimal valor) {
        if (valor == null) {
            throw new IllegalArgumentException("La nota es obligatoria");
        }
        if (valor.compareTo(NOTA_MINIMA) < 0 || valor.compareTo(NOTA_MAXIMA) > 0) {
            throw new IllegalArgumentException("La nota debe estar entre 0.0 y 5.0");
        }
        return valor;
    }

    private static String normalizarOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    public Long getId() {
        return id;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public String getMateria() {
        return materia;
    }

    public String getAula() {
        return aula;
    }

    public String getPeriodo() {
        return periodo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getActualizadaEn() {
        return actualizadaEn;
    }
}
