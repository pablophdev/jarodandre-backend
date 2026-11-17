package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Carrito", description = "Operaciones del carrito de compra para invitados y cuentas")
public class CarritoController {
    private final CarritoService carritos;
    private final IdentidadService identidad;

    @Operation(summary = "Iniciar o recuperar un carrito", description = "Crea o recupera el carrito de la cuenta autenticada o, para invitados, crea o recupera la sesión mediante X-Carrito-Token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito creado o recuperado"),
            @ApiResponse(responseCode = "401", description = "Token de carrito de invitado inválido")
    })
    @PostMapping
    public CarritoSesion iniciar(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        return carritos.iniciar(identidad.consultar(jwt), token);
    }

    @Operation(summary = "Consultar carrito", description = "Devuelve las líneas con su disponibilidad, precios vigentes, total y el identificador de un checkout pendiente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrito consultado"),
            @ApiResponse(responseCode = "401", description = "Se requiere una sesión de carrito válida")
    })
    @GetMapping
    public CarritoVista ver(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        return carritos.ver(identidad.consultar(jwt), token);
    }

    @Operation(summary = "Fijar cantidad de un producto", description = "Agrega el producto al carrito o actualiza su cantidad entre 1 y 99.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cantidad actualizada"),
            @ApiResponse(responseCode = "400", description = "Cantidad o identificador inválido"),
            @ApiResponse(responseCode = "401", description = "Se requiere una sesión de carrito válida"),
            @ApiResponse(responseCode = "409", description = "Producto no disponible, carrito con checkout pendiente o límite de productos alcanzado")
    })
    @PutMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cantidad(
            @Parameter(description = "Identificador del producto", example = "3") @PathVariable @Positive long id,
            @Valid @RequestBody CantidadRequest datos, @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        carritos.modificar(identidad.consultar(jwt), token, id, datos.cantidad());
    }

    @Operation(summary = "Quitar un producto", description = "Elimina la línea de producto del carrito.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto quitado"),
            @ApiResponse(responseCode = "401", description = "Se requiere una sesión de carrito válida"),
            @ApiResponse(responseCode = "409", description = "El carrito tiene un checkout pendiente")
    })
    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void quitar(
            @Parameter(description = "Identificador del producto", example = "3") @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token) {
        carritos.modificar(identidad.consultar(jwt), token, id, null);
    }
}
