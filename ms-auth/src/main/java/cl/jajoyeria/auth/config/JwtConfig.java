package cl.jajoyeria.auth.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.IOException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Objects;

@Configuration
public class JwtConfig {

    public static final String ISSUER = "urn:ja-joyeria:ms-auth";
    public static final String AUDIENCE = "ja-joyeria";

    @Bean
    public RSAKey rsaKey(
            @Value("${app.jwt.private-key:file:/etc/jarodandre-keys/private.pem}")
            Resource privateResource,
            @Value("${app.jwt.public-key:file:/etc/jarodandre-keys/public.pem}")
            Resource publicResource
    ) throws IOException {
        RSAPrivateKey privateKey;
        RSAPublicKey publicKey;

        try (var stream = privateResource.getInputStream()) {
            privateKey = Objects.requireNonNull(
                    RsaKeyConverters.pkcs8().convert(stream)
            );
        }

        try (var stream = publicResource.getInputStream()) {
            publicKey = Objects.requireNonNull(
                    RsaKeyConverters.x509().convert(stream)
            );
        }

        if (!privateKey.getModulus().equals(publicKey.getModulus())) {
            throw new IllegalStateException("Las claves RSA no corresponden al mismo par");
        }

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("ja-auth-1")
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAKey rsaKey) {
        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(rsaKey))
        );
    }
}