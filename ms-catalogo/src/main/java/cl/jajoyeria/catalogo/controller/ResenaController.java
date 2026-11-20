package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/productos/{id}/resenas")
@RequiredArgsConstructor
@Tag(name="Reseñas de compras verificadas")
public class ResenaController {
    private final ResenaService resenas;
    private final CompraVerificadaClient compras;

    @GetMapping
    @Operation(summary="Consultar reseñas de compradores")
    public Page<ResenaResponse> listar(@PathVariable long id, @RequestParam(defaultValue="0") @Min(0) int pagina) {
        return resenas.listar(id,pagina);
    }

    @PutMapping
    @SecurityRequirement(name="BearerAuth")
    @Operation(summary="Valorar un producto comprado", description="Una reseña por cuenta y producto. Requiere una compra pagada, verificada en pedidos. Reenviar modifica la reseña existente.")
    public ResenaResponse guardar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ResenaRequest datos) {
        var perfil=compras.verificar(jwt,id);
        return resenas.guardar(id,perfil,datos);
    }
}
