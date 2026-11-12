package co.edu.uco.backend.service;

import co.edu.uco.backend.dto.NotaResponse;
import co.edu.uco.backend.dto.PromedioResponse;
import co.edu.uco.backend.dto.UsuarioResponse;

import java.util.List;

public interface EstudianteService {

    UsuarioResponse perfil();

    List<NotaResponse> misNotas();

    PromedioResponse miPromedio();
}
