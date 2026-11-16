package cl.jajoyeria.auth.controller;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.service.TerritorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/regiones")
@RequiredArgsConstructor
public class TerritorioController {
    private final TerritorioService territorios;

    @GetMapping
    public List<RegionResponse> regiones() {
        return territorios.regiones();
    }

    @GetMapping("/{region}/comunas")
    public List<ComunaResponse> comunas(@PathVariable String region) {
        return territorios.comunas(region);
    }
}
