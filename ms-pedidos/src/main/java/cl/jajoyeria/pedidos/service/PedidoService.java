package cl.jajoyeria.pedidos.service;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.entity.*;
import cl.jajoyeria.pedidos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {
    private final CarritoService carritos;
    private final CarritoItemRepository carritoItems;
    private final PedidoRepository pedidos;
    private final PedidoItemRepository lineas;
    private final IntentoPagoRepository intentos;
    private final AccesoInvitadoRepository accesos;
    @Value("${app.pago.simulado:false}") private boolean simulador;

    public CheckoutVista checkout(IdentidadService.Perfil perfil, String token, UUID clave, CheckoutRequest req) {
        if (!simulador) throw new ResponseStatusException(SERVICE_UNAVAILABLE, "No hay una pasarela de pago habilitada");
        Carrito c = carritos.bloquear(perfil, token);
        Entrega e = normalizar(req.entrega());
        var previo = pedidos.findByCarritoIdAndClaveCheckout(c.getId(), clave);
        if (previo.isPresent()) {
            Pedido p = previo.get();
            if (!Objects.equals(c.getUsuarioId(), p.getUsuarioId()) || !entrega(p).equals(e) || !p.getTotalClp().equals(req.totalEsperadoClp())) {
                throw new ResponseStatusException(CONFLICT, "La clave de checkout ya está en uso");
            }
            return checkoutVista(p, token);
        }
        if (carritos.pendiente(c).isPresent()) throw new ResponseStatusException(CONFLICT, "Ya existe un checkout pendiente");
        var cotizacion = carritos.cotizar(c);
        if (cotizacion.isEmpty() || cotizacion.stream().anyMatch(l -> !CarritoService.comprable(l.disponibilidad()))) {
            throw new ResponseStatusException(CONFLICT, "Revisa los productos del carrito");
        }
        long total = CarritoService.total(cotizacion);
        if (total != req.totalEsperadoClp()) throw new ResponseStatusException(CONFLICT, "El precio cambió. Revisa el total vigente.");
        Instant ahora = Instant.now();
        Pedido p = pedidos.saveAndFlush(Pedido.builder().carritoId(c.getId()).usuarioId(c.getUsuarioId()).claveCheckout(clave)
                .nombreEntrega(e.nombre()).correoEntrega(e.correo()).direccionEntrega(e.direccion()).ciudadEntrega(e.ciudad())
                .totalClp(total).estadoPago(EstadoPago.PENDIENTE).estadoEntrega(EstadoEntrega.PENDIENTE).creadoEn(ahora).build());
        for (var l : cotizacion) {
            lineas.save(PedidoItem.builder().pedidoId(p.getId()).productoId(l.productoId()).cantidad(l.cantidad())
                    .nombreProducto(l.nombre()).precioUnitarioClp(l.precioClp()).subtotalClp(l.subtotalClp()).build());
        }
        intentos.save(IntentoPago.builder().id(UUID.randomUUID()).pedidoId(p.getId()).proveedor("SIMULADO")
                .montoClp(total).estado(EstadoPago.PENDIENTE).creadoEn(ahora).build());
        if (c.getUsuarioId() == null) {
            accesos.save(AccesoInvitado.builder().id(UUID.randomUUID()).pedidoId(p.getId())
                    .tokenHash(TokensInvitado.hash(TokensInvitado.consulta(token, p.getId()))).venceEn(ahora.plus(Duration.ofDays(30))).build());
        }
        return checkoutVista(p, token);
    }

    public PedidoVista pagar(long id, IdentidadService.Perfil perfil, String token, PagoRequest.Accion accion) {
        if (!simulador) throw new ResponseStatusException(NOT_FOUND);
        Carrito c = carritos.bloquear(perfil, token);
        Pedido p = bloqueado(id);
        if (!p.getCarritoId().equals(c.getId()) || !Objects.equals(p.getUsuarioId(), c.getUsuarioId())) throw new ResponseStatusException(NOT_FOUND);
        EstadoPago destino = accion == PagoRequest.Accion.CONFIRMAR ? EstadoPago.PAGADO : EstadoPago.FALLIDO;
        if (p.getEstadoPago() == destino) return vista(p);
        if (p.getEstadoPago() != EstadoPago.PENDIENTE) throw new ResponseStatusException(CONFLICT, "El intento ya fue resuelto");
        IntentoPago intento = intentos.findByPedidoIdAndEstado(id, EstadoPago.PENDIENTE).orElseThrow();
        Instant ahora = Instant.now();
        intento.setEstado(destino);
        intento.setResueltoEn(ahora);
        p.setEstadoPago(destino);
        if (destino == EstadoPago.PAGADO) {
            p.setPagadoEn(ahora);
            carritoItems.deleteByCarritoId(c.getId());
        }
        return vista(p);
    }

    private CheckoutVista checkoutVista(Pedido p, String token) {
        return new CheckoutVista(vista(p), p.getUsuarioId() == null ? TokensInvitado.consulta(token, p.getId()) : null);
    }

    private Pedido bloqueado(long id) {
        return pedidos.bloquear(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    private AccesoInvitado validarToken(long id, String token) {
        if (token == null || token.length() != 43) throw new ResponseStatusException(NOT_FOUND);
        var a = accesos.findByPedidoId(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (a.getRevocadoEn() != null || !a.getVenceEn().isAfter(Instant.now()) || !a.getTokenHash().equals(TokensInvitado.hash(token))) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        return a;
    }

    @Transactional(readOnly = true)
    public PedidoVista consultar(long id, IdentidadService.Perfil perfil, String token) {
        var p = pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (perfil == null || !(perfil.id().equals(p.getUsuarioId()) || "ADMIN".equals(perfil.rol()))) validarToken(id, token);
        return vista(p);
    }

    public PedidoVista vincular(long id, IdentidadService.Perfil perfil, String token) {
        Pedido p = bloqueado(id);
        if (perfil.id().equals(p.getUsuarioId())) return vista(p);
        if (p.getUsuarioId() != null) throw new ResponseStatusException(NOT_FOUND);
        var a = validarToken(id, token);
        if (!p.getCorreoEntrega().equalsIgnoreCase(perfil.correo())) throw new ResponseStatusException(FORBIDDEN);
        if (p.getEstadoPago() == EstadoPago.PENDIENTE) throw new ResponseStatusException(CONFLICT, "Finaliza primero el pago de invitado");
        p.setUsuarioId(perfil.id());
        a.setRevocadoEn(Instant.now());
        return vista(p);
    }

    public PedidoVista despachar(long id, EstadoEntrega destino) {
        Pedido p = bloqueado(id);
        if (p.getEstadoEntrega() == destino) return vista(p);
        if (p.getEstadoPago() != EstadoPago.PAGADO || destino.ordinal() != p.getEstadoEntrega().ordinal() + 1) {
            throw new ResponseStatusException(CONFLICT, "Se requiere pago aprobado y avance secuencial del despacho");
        }
        p.setEstadoEntrega(destino);
        return vista(p);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResumen> listar(UUID usuario, Pageable pagina) {
        var lista = usuario == null ? pedidos.findAll(pagina) : pedidos.findByUsuarioId(usuario, pagina);
        return lista.map(p -> new PedidoResumen(p.getId(), numero(p.getId()), p.getCreadoEn(), p.getTotalClp(), p.getEstadoPago(), p.getEstadoEntrega()));
    }

    private String numero(long id) { return "ORD-%04d".formatted(id); }

    private PedidoVista vista(Pedido p) {
        var items = lineas.findByPedidoIdOrderById(p.getId()).stream().map(l -> new LineaPedido(l.getProductoId(),
                l.getNombreProducto(), l.getPrecioUnitarioClp(), l.getCantidad(), l.getSubtotalClp())).toList();
        return new PedidoVista(p.getId(), numero(p.getId()), p.getUsuarioId(), p.getCreadoEn(), entrega(p), items,
                p.getTotalClp(), p.getEstadoPago(), p.getEstadoEntrega(), p.getPagadoEn());
    }

    private Entrega entrega(Pedido p) {
        return new Entrega(p.getNombreEntrega(), p.getCorreoEntrega(), p.getDireccionEntrega(), p.getCiudadEntrega());
    }

    private Entrega normalizar(Entrega e) {
        return new Entrega(e.nombre().strip(), e.correo().strip().toLowerCase(Locale.ROOT), e.direccion().strip(), e.ciudad().strip());
    }
}
