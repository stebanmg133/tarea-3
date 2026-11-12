package co.edu.uco.backend.service.impl;

import co.edu.uco.backend.domain.Estudiante;
import co.edu.uco.backend.domain.Nota;
import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.PromedioResponse;
import co.edu.uco.backend.dto.UsuarioResponse;
import co.edu.uco.backend.exception.ReglaNegocioException;
import co.edu.uco.backend.mapper.NotaMapper;
import co.edu.uco.backend.mapper.UsuarioMapper;
import co.edu.uco.backend.repo.EstudianteRepository;
import co.edu.uco.backend.repo.NotaRepository;
import co.edu.uco.backend.security.UsuarioActualService;
import co.edu.uco.backend.service.EstudianteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class EstudianteServiceImpl implements EstudianteService {

    private final UsuarioActualService usuarioActual;
    private final EstudianteRepository estudiantes;
    private final NotaRepository notas;
    private final UsuarioMapper usuarioMapper;
    private final NotaMapper notaMapper;

    public EstudianteServiceImpl(UsuarioActualService usuarioActual,
                                 EstudianteRepository estudiantes,
                                 NotaRepository notas,
                                 UsuarioMapper usuarioMapper,
                                 NotaMapper notaMapper) {
        this.usuarioActual = usuarioActual;
        this.estudiantes = estudiantes;
        this.notas = notas;
        this.usuarioMapper = usuarioMapper;
        this.notaMapper = notaMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse perfil() {
        return usuarioMapper.aResponse(estudianteActual().getUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotaResponse> misNotas() {
        return notaMapper.aResponses(
                notas.findByEstudianteIdOrderByMateriaAscPeriodoAsc(estudianteActual().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public PromedioResponse miPromedio() {
        Estudiante estudiante = estudianteActual();
        List<Nota> propias = notas.findByEstudianteIdOrderByMateriaAscPeriodoAsc(estudiante.getId());

        BigDecimal promedio = propias.isEmpty() ? null : propias.stream()
                .map(Nota::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(propias.size()), 2, RoundingMode.HALF_UP);

        return new PromedioResponse(estudiante.getId(), propias.size(), promedio);
    }

    private Estudiante estudianteActual() {
        Long usuarioId = usuarioActual.id();
        return estudiantes.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("La cuenta no tiene perfil de estudiante"));
    }
}
