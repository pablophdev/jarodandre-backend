package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.repository.PedidoItemRepository;
import cl.jajoyeria.pedidos.dto.CompraVerificadaResponse;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class CompraVerificadaService {
    private final PedidoItemRepository items;
    public CompraVerificadaResponse verificar(UUID usuarioId, long productoId) {
        return new CompraVerificadaResponse(items.comprasPagadas(usuarioId,productoId)>0);
    }
}
