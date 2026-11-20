package cl.jajoyeria.auth;

import cl.jajoyeria.auth.controller.*;
import cl.jajoyeria.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

class FormularioRegistroTest {
    @Test
    void explicaLosCamposInvalidosEnEspanol() throws Exception {
        var auth=mock(AuthService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new AuthController(auth,null))
                .setControllerAdvice(new ValidacionHandler()).build();
        mvc.perform(post("/v1/auth/registro").contentType("application/json").content("""
                {"correo":"invalido","password":"123123","rut":"12345678-5","nombre":"Ana Soto","comunaId":null}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").isString())
                .andExpect(jsonPath("$.errores.password",containsString("8 y 72")))
                .andExpect(jsonPath("$.errores.comunaId",containsString("Comuna")));
        verifyNoInteractions(auth);
    }

    @Test
    void aceptaTelefonoVacioComoOpcional() throws Exception {
        var auth=mock(AuthService.class);
        var mvc=MockMvcBuilders.standaloneSetup(new AuthController(auth,null))
                .setControllerAdvice(new ValidacionHandler()).build();
        mvc.perform(post("/v1/auth/registro").contentType("application/json").content("""
                {"correo":"ana@example.cl","password":"Clave1234","rut":"12345678-5","nombre":"Ana Soto","telefono":"  ","comunaId":1}
                """))
                .andExpect(status().isCreated());
        verify(auth).registrar(argThat(datos->datos.telefono()==null));
    }

    @Test
    void explicaUnTipoDeDatoIncorrecto() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new AuthController(mock(AuthService.class),null))
                .setControllerAdvice(new ValidacionHandler()).build();
        mvc.perform(post("/v1/auth/registro").contentType("application/json").content("""
                {"correo":"ana@example.cl","password":"Clave1234","rut":"12345678-5","nombre":"Ana Soto","comunaId":"Santiago"}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",containsString("Revisa los números")));
    }
}
