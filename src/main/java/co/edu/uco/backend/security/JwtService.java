package co.edu.uco.backend.security;

import co.edu.uco.backend.config.UcoProperties;
import co.edu.uco.backend.domain.Usuario;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** Emisión y verificación de los tokens JWT (HS256). */
@Service
public class JwtService {

    private static final String CLAIM_USUARIO_ID = "uid";
    private static final String CLAIM_ROL = "rol";

    private final Algorithm algoritmo;
    private final JWTVerifier verificador;
    private final Duration vigencia;

    public JwtService(UcoProperties propiedades) {
        UcoProperties.Jwt jwt = propiedades.jwt();
        this.algoritmo = Algorithm.HMAC256(jwt.secret());
        this.verificador = JWT.require(algoritmo).build();
        this.vigencia = jwt.ttl();
    }

    public TokenJwt generar(Usuario usuario) {
        Instant expiraEn = Instant.now().plus(vigencia);
        String token = JWT.create()
                .withSubject(usuario.getEmail())
                .withClaim(CLAIM_USUARIO_ID, usuario.getId())
                .withClaim(CLAIM_ROL, usuario.getRol().name())
                .withIssuedAt(Instant.now())
                .withExpiresAt(expiraEn)
                .sign(algoritmo);
        return new TokenJwt(token, expiraEn);
    }

    /** @throws com.auth0.jwt.exceptions.JWTVerificationException si el token no es válido o expiró. */
    public DecodedJWT verificar(String token) {
        return verificador.verify(token);
    }

    public Long usuarioId(DecodedJWT jwt) {
        return jwt.getClaim(CLAIM_USUARIO_ID).asLong();
    }

    public record TokenJwt(String valor, Instant expiraEn) {
    }
}
