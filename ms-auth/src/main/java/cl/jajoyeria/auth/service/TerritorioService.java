package cl.jajoyeria.auth.service;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TerritorioService {
    private final RegionRepository regiones;
    private final ComunaRepository comunas;

    public List<RegionResponse> regiones() {
        return regiones.findAllByOrderByNombre().stream().map(r -> new RegionResponse(r.getId(), r.getNombre())).toList();
    }

    public List<ComunaResponse> comunas(String region) {
        return comunas.findByRegionIdOrderByNombre(region).stream().map(c -> new ComunaResponse(c.getId(), c.getNombre())).toList();
    }
}
