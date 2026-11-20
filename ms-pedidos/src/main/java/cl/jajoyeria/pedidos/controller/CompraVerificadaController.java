package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.CompraVerificadaResponse;
import cl.jajoyeria.pedidos.service.CompraVerificadaService;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
public class CompraVerificadaController {
    private final CompraVerificadaService compras;
    @GetMapping("/interno/v1/compras/verificar")
    @Operation(summary="Verificar compra pagada para reseñas", description="Uso interno. Requiere X-Internal-Token compartido con catálogo.")
    public CompraVerificadaResponse verificar(@RequestParam UUID usuarioId, @RequestParam @Positive long productoId) {
        return compras.verificar(usuarioId,productoId);
    }
}
