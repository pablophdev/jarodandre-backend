package cl.jajoyeria.catalogo.controller;
import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
@RestController @RequiredArgsConstructor @Tag(name="Personalizador de grillz")
public class GrillzController {
    private final GrillzService grillz;
    private final CompraVerificadaClient cuentas;
    public record Estimacion(long estimadoClp,String aviso) {}
    @GetMapping("/v1/grillz/opciones") public List<OpcionGrillzResponse> opciones(){return grillz.opciones();}
    @PostMapping("/v1/grillz/estimacion") public Estimacion estimacion(@Valid @RequestBody GrillzRequest r){return new Estimacion(grillz.estimar(r),"Referencia de diseño. El precio final y el ajuste se confirman mediante cotización y molde.");}
    @PostMapping("/v1/grillz/disenos") @ResponseStatus(HttpStatus.CREATED) @SecurityRequirement(name="BearerAuth")
    public GrillzResponse guardar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody GrillzRequest r){return grillz.guardar(r,cuentas.verificarPerfil(jwt));}
    @GetMapping("/v1/grillz/disenos") @SecurityRequirement(name="BearerAuth")
    public Page<GrillzResponse> mios(@AuthenticationPrincipal Jwt jwt,@RequestParam(defaultValue="0") @Min(0) int pagina){return grillz.mios(cuentas.verificarPerfil(jwt).id(),pagina);}
    @PostMapping("/v1/grillz/disenos/{id}/cotizacion") @SecurityRequirement(name="BearerAuth")
    public GrillzResponse solicitar(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID id){return grillz.solicitar(id,cuentas.verificarPerfil(jwt).id());}
    @GetMapping("/v1/admin/grillz/cotizaciones") @SecurityRequirement(name="BearerAuth")
    public Page<GrillzResponse> solicitudes(@RequestParam(defaultValue="0") @Min(0) int pagina){return grillz.solicitudes(pagina);}
}
