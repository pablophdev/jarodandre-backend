package cl.jajoyeria.auth.config;

import cl.jajoyeria.auth.service.CorreoVerificado;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtValidationTest {

    private static JwtEncoder encoder;
    private static JwtDecoder decoder;
    private static final String SUJETO = UUID.randomUUID().toString();

    @BeforeAll
    static void configurar() throws Exception {
        RSAKey clave = new RSAKeyGenerator(2048)
                .keyID("test")
                .algorithm(JWSAlgorithm.RS256)
                .generate();

        encoder = new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(clave))
        );

        decoder = new SecurityConfig().decoder(clave);
    }

    @Test
    void aceptaTokenFirmadoConEmisorYAudienciaCorrectos() {
        String token = firmar(
                Instant.now().plusSeconds(900),
                JwtConfig.AUDIENCE,
                JwtConfig.ISSUER
        );

        assertEquals(SUJETO, decoder.decode(token).getSubject());
    }

    @Test
    void rechazaTokenVencido() {
        String token = firmar(
                Instant.now().minusSeconds(120),
                JwtConfig.AUDIENCE,
                JwtConfig.ISSUER
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rechazaOtraAudiencia() {
        String token = firmar(
                Instant.now().plusSeconds(900),
                "otra-aplicacion",
                JwtConfig.ISSUER
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rechazaOtroEmisor() {
        String token = firmar(
                Instant.now().plusSeconds(900),
                JwtConfig.AUDIENCE,
                "otro-emisor"
        );

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void rechazaTokenInvalido() {
        assertThrows(JwtException.class, () -> decoder.decode("token"));
    }

    @Test
    void conservaLaVerificacionDeCorreoPendiente() {
        var error = assertThrows(
                ResponseStatusException.class,
                () -> new CorreoVerificado().obtener(null)
        );

        assertEquals(503, error.getStatusCode().value());
    }

    private String firmar(Instant vencimiento, String audiencia, String emisor) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emisor)
                .audience(List.of(audiencia))
                .subject(SUJETO)
                .issuedAt(vencimiento.minusSeconds(900))
                .expiresAt(vencimiento)
                .claim("token_use", "access")
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("test")
                .build();

        return encoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}