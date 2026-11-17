package cl.jajoyeria.auth.controller;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.service.TerritorioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/regiones")
@RequiredArgsConstructor
@Tag(name = "Territorios", description = "Regiones y comunas disponibles para los formularios")
public class TerritorioController {
    private final TerritorioService territorios;

    @Operation(summary = "Listar regiones", description = "Obtiene las regiones disponibles ordenadas por nombre.")
    @ApiResponse(responseCode = "200", description = "Regiones obtenidas")
    @GetMapping
    public List<RegionResponse> regiones() {
        return territorios.regiones();
    }

    @Operation(summary = "Listar comunas de una región", description = "Obtiene las comunas de la región indicada, ordenadas por nombre.")
    @ApiResponse(responseCode = "200", description = "Comunas obtenidas")
    @GetMapping("/{region}/comunas")
    public List<ComunaResponse> comunas(
            @Parameter(description = "Identificador de la región", example = "RM") @PathVariable String region) {
        return territorios.comunas(region);
    }
}
