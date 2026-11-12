package co.edu.uco.backend.config;

import co.edu.uco.backend.domain.EstadoUsuario;
import co.edu.uco.backend.domain.Rol;
import co.edu.uco.backend.domain.Usuario;
import co.edu.uco.backend.repo.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Crea el administrador inicial si la base de datos todavía no tiene ninguno. */
@Component
public class AdminInicialRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicialRunner.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final UcoProperties propiedades;

    public AdminInicialRunner(UsuarioRepository usuarios, PasswordEncoder passwordEncoder, UcoProperties propiedades) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.propiedades = propiedades;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = propiedades.admin().email();

        if (usuarios.findByEmailIgnoreCase(email).isPresent()) {
            log.info("Administrador ya existente: {}", email);
            return;
        }

        Usuario admin = new Usuario(
                propiedades.admin().nombre(),
                email,
                passwordEncoder.encode(propiedades.admin().password()),
                Rol.ADMIN,
                EstadoUsuario.ACTIVO
        );
        usuarios.save(admin);
        log.info("Administrador creado: {} (cambia la contraseña con UCO_ADMIN_PASSWORD)", email);
    }
}
