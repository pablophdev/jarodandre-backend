package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.entity.*;
import cl.jajoyeria.pedidos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class CarritoService {
    private final CarritoRepository carritos;
    private final CarritoItemRepository items;
    private final PedidoRepository pedidos;
    private final CatalogoClient catalogo;

    public CarritoSesion iniciar(IdentidadService.Perfil perfil, String token) {
        if (perfil != null) {
            var c = carritos.findByUsuarioId(perfil.id()).orElseGet(() -> carritos.saveAndFlush(
                    Carrito.builder().id(UUID.randomUUID()).usuarioId(perfil.id()).creadoEn(Instant.now()).build()));
            return new CarritoSesion(c.getId(), null);
        }
        if (token != null) return new CarritoSesion(propio(null, token).getId(), token);
        String secreto = TokensInvitado.nuevo();
        Carrito c = carritos.save(Carrito.builder().id(UUID.randomUUID()).tokenHash(TokensInvitado.hash(secreto))
                .creadoEn(Instant.now()).build());
        return new CarritoSesion(c.getId(), secreto);
    }

    Carrito propio(IdentidadService.Perfil perfil, String token) {
        if (perfil != null) return carritos.findByUsuarioId(perfil.id()).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Crea el carrito"));
        if (token == null || token.length() != 43) throw new ResponseStatusException(UNAUTHORIZED, "Falta la sesión de invitado");
        return carritos.findByTokenHash(TokensInvitado.hash(token)).orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED));
    }

    Carrito bloquear(IdentidadService.Perfil perfil, String token) {
        return carritos.bloquear(propio(perfil, token).getId()).orElseThrow();
    }

    Optional<Pedido> pendiente(Carrito c) {
        return pedidos.findByCarritoIdAndEstadoPago(c.getId(), EstadoPago.PENDIENTE);
    }

    @Transactional(readOnly = true)
    public CarritoVista ver(IdentidadService.Perfil perfil, String token) {
        Carrito c = propio(perfil, token);
        List<LineaCarrito> lineas = cotizar(c);
        return new CarritoVista(c.getId(), lineas, total(lineas), pendiente(c).map(Pedido::getId).orElse(null));
    }

    List<LineaCarrito> cotizar(Carrito c) {
        List<CarritoItem> lista = items.findByCarritoIdOrderById(c.getId());
        var precios = catalogo.cotizar(lista.stream().map(CarritoItem::getProductoId).toList());
        return lista.stream().map(i -> {
            Precio p = precios.get(i.getProductoId());
            if (p == null) return new LineaCarrito(i.getProductoId(), i.getCantidad(), null, null, "RETIRADO", 0L);
            long subtotal = comprable(p.disponibilidad()) ? Math.multiplyExact(p.precioClp(), i.getCantidad().longValue()) : 0;
            return new LineaCarrito(i.getProductoId(), i.getCantidad(), p.nombre(), p.precioClp(), p.disponibilidad(), subtotal);
        }).toList();
    }

    static boolean comprable(String disponibilidad) {
        return "DISPONIBLE".equals(disponibilidad) || "CONSULTAR".equals(disponibilidad);
    }

    static long total(List<LineaCarrito> lineas) {
        return lineas.stream().mapToLong(LineaCarrito::subtotalClp).reduce(0L, Math::addExact);
    }

    public void modificar(IdentidadService.Perfil perfil, String token, long productoId, Integer cantidad) {
        Carrito c = bloquear(perfil, token);
        if (pendiente(c).isPresent()) throw new ResponseStatusException(CONFLICT, "Resuelve el pago pendiente antes de editar el carrito");
        var existente = items.findByCarritoIdAndProductoId(c.getId(), productoId);
        if (cantidad == null) {
            existente.ifPresent(items::delete);
            return;
        }
        var precio = catalogo.cotizar(List.of(productoId)).get(productoId);
        if (precio == null || !comprable(precio.disponibilidad())) throw new ResponseStatusException(CONFLICT, "Producto no disponible");
        if (existente.isEmpty() && items.countByCarritoId(c.getId()) >= 100) throw new ResponseStatusException(CONFLICT, "Máximo de 100 productos distintos");
        var item = existente.orElseGet(() -> CarritoItem.builder().carritoId(c.getId()).productoId(productoId).build());
        item.setCantidad(cantidad);
        items.save(item);
    }
}
