package cl.jajoyeria.auth.config;

import cl.jajoyeria.auth.dto.RegistroAuthRequest;
import cl.jajoyeria.auth.entity.*;
import cl.jajoyeria.auth.repository.UsuarioRepository;
import cl.jajoyeria.auth.service.AuthService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

@Configuration
@ConditionalOnProperty(name="app.local-admin.enabled", havingValue="true")
public class LocalAdminConfig {
    @Bean
    ApplicationRunner crearAdminLocal(UsuarioRepository usuarios, AuthService auth,
            @Value("${app.local-admin.correo:admin@jarodandre.local}") String correo,
            @Value("${app.local-admin.password:Admin123123}") String password) {
        return args -> {
            if (usuarios.findByCorreo(correo).isPresent()) return;
            var resultado = auth.registrar(new RegistroAuthRequest(correo, password,
                    "11111111-1", "Administrador Local", null, 1L));
            var usuario = usuarios.findById(resultado.usuario().id()).orElseThrow();
            usuario.cambiarAcceso(Rol.ADMIN, Estado.ACTIVO);
            usuarios.saveAndFlush(usuario);
        };
    }
}
