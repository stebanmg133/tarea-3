package co.edu.uco.backend;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integración del flujo completo:
 * registro → aprobación del admin → login → notas → expulsión.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlujoUcoIntegrationTest {

    private static final String EMAIL_PROFESOR = "ana.docente@uco.edu.co";
    private static final String EMAIL_ESTUDIANTE = "carlos.estudiante@uco.edu.co";

    @Autowired
    private MockMvc mvc;

    @Test
    void registroAprobacionNotasYExpulsion() throws Exception {
        String tokenAdmin = login("admin@uco.edu.co", "Admin123!");

        registrarProfesor();
        registrarEstudiante();

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoLogin(EMAIL_ESTUDIANTE, "Estudiante123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value(
                        "Tu registro está pendiente de aprobación por el administrador"));

        long idUsuarioProfesor = idPendiente(tokenAdmin, EMAIL_PROFESOR);
        long idUsuarioEstudiante = idPendiente(tokenAdmin, EMAIL_ESTUDIANTE);

        aprobar(tokenAdmin, idUsuarioProfesor);
        aprobar(tokenAdmin, idUsuarioEstudiante);

        String tokenProfesor = login(EMAIL_PROFESOR, "Profesor123!");
        String tokenEstudiante = login(EMAIL_ESTUDIANTE, "Estudiante123!");

        assertThat(estudiantesDelAula(tokenProfesor))
                .as("el profesor solo ve estudiantes de su aula")
                .contains(EMAIL_ESTUDIANTE);

        long idEstudiante = idEstudiante(tokenProfesor, EMAIL_ESTUDIANTE);

        long idNota = crearNota(tokenProfesor, idEstudiante, "3.5");

        mvc.perform(put("/api/profesor/notas/{id}", idNota)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProfesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"periodo":"2026-1","valor":4.7,"comentario":"Nota corregida"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(4.7));

        mvc.perform(get("/api/estudiante/notas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].valor").value(4.7));

        String promedio = mvc.perform(get("/api/estudiante/promedio")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenEstudiante))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat((Double) JsonPath.read(promedio, "$.promedio")).isEqualTo(4.7);

        mvc.perform(get("/api/admin/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenEstudiante))
                .andExpect(status().isForbidden());

        mvc.perform(patch("/api/admin/usuarios/{id}/expulsar", idUsuarioEstudiante)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXPULSADO"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoLogin(EMAIL_ESTUDIANTE, "Estudiante123!")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Tu cuenta fue expulsada de la plataforma"));
    }

    private void registrarProfesor() throws Exception {
        mvc.perform(post("/api/auth/registro/profesor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreCompleto":"Ana Docente","email":"%s","password":"Profesor123!",
                                 "aula":"A-101","materia":"Bases de Datos"}""".formatted(EMAIL_PROFESOR)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.aula").value("A-101"))
                .andExpect(jsonPath("$.materia").value("Bases de Datos"));
    }

    private void registrarEstudiante() throws Exception {
        mvc.perform(post("/api/auth/registro/estudiante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombreCompleto":"Carlos Estudiante","email":"%s","password":"Estudiante123!",
                                 "aula":"A-101","codigoEstudiantil":"EST-001"}""".formatted(EMAIL_ESTUDIANTE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.codigoEstudiantil").value("EST-001"));
    }

    private long idPendiente(String tokenAdmin, String email) throws Exception {
        String cuerpo = mvc.perform(get("/api/admin/usuarios")
                        .param("estado", "PENDIENTE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Number> ids = JsonPath.read(cuerpo, "$[?(@.email=='" + email + "')].id");
        assertThat(ids).as("usuario pendiente " + email).hasSize(1);
        return ids.getFirst().longValue();
    }

    private void aprobar(String tokenAdmin, long usuarioId) throws Exception {
        mvc.perform(patch("/api/admin/usuarios/{id}/aprobar", usuarioId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    private List<String> estudiantesDelAula(String tokenProfesor) throws Exception {
        String cuerpo = mvc.perform(get("/api/profesor/estudiantes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProfesor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$[*].email");
    }

    private long idEstudiante(String tokenProfesor, String email) throws Exception {
        String cuerpo = mvc.perform(get("/api/profesor/estudiantes")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProfesor))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Number> ids = JsonPath.read(cuerpo, "$[?(@.email=='" + email + "')].estudianteId");
        return ids.getFirst().longValue();
    }

    private long crearNota(String tokenProfesor, long idEstudiante, String valor) throws Exception {
        String cuerpo = mvc.perform(post("/api/profesor/notas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProfesor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estudianteId":%d,"periodo":"2026-1","valor":%s,"comentario":"Primera entrega"}"""
                                .formatted(idEstudiante, valor)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.materia").value("Bases de Datos"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(cuerpo, "$.id")).longValue();
    }

    private String login(String email, String password) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpoLogin(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }

    private static String cuerpoLogin(String email, String password) {
        return """
                {"email":"%s","password":"%s"}""".formatted(email, password);
    }
}
