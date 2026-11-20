package cl.jajoyeria.catalogo.service;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.entity.Resena;
import cl.jajoyeria.catalogo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
public class ResenaService {
    private final ResenaRepository resenas;
    private final ProductoRepository productos;

    public Page<ResenaResponse> listar(long producto, int pagina) {
        return resenas.findByProductoIdOrderByCreadoEnDesc(producto, PageRequest.of(pagina,20)).map(this::respuesta);
    }

    @Transactional(isolation=Isolation.READ_COMMITTED)
    public ResenaResponse guardar(long productoId, CompraVerificadaClient.Perfil perfil, ResenaRequest datos) {
        var producto=productos.bloquear(productoId).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"El producto ya no está disponible."));
        var resena=resenas.findByProductoIdAndUsuarioId(productoId,perfil.id())
                .orElseGet(()->new Resena(productoId,perfil.id()));
        resena.editar(perfil.nombre(),datos.estrellas(),datos.comentario());
        resenas.saveAndFlush(resena);
        producto.setCantidadResenas(Math.toIntExact(resenas.countByProductoId(productoId)));
        producto.setValoracion((int)Math.round(resenas.promedio(productoId)));
        return respuesta(resena);
    }

    private ResenaResponse respuesta(Resena r) {
        return new ResenaResponse(r.getId(),r.getAutor(),r.getEstrellas(),r.getComentario(),r.getCreadoEn());
    }
}
