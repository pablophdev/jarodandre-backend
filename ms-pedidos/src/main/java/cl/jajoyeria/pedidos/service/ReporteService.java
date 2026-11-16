package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.dto.ReporteVentas;
import cl.jajoyeria.pedidos.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class ReporteService {
    private final ReporteRepository reportes;

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ReporteVentas ventas() {
        return reportes.ventas();
    }
}
