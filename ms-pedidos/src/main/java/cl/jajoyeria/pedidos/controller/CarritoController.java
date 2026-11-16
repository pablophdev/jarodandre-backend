package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {
    private final CarritoService carritos;
    private final IdentidadService identidad;

    @PostMapping
    public CarritoSesion iniciar(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        return carritos.iniciar(identidad.consultar(jwt), token);
    }

    @GetMapping
    public CarritoVista ver(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        return carritos.ver(identidad.consultar(jwt), token);
    }

    @PutMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cantidad(@PathVariable @Positive long id, @Valid @RequestBody CantidadRequest datos, @AuthenticationPrincipal Jwt jwt,
                         @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        carritos.modificar(identidad.consultar(jwt), token, id, datos.cantidad());
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        carritos.modificar(identidad.consultar(jwt), token, id, null);
    }
}
