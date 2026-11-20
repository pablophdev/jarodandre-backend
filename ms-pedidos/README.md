# ms-pedidos

Carritos, checkout, pagos simulados, historial, comprobantes, despacho y reportes. Puerto `8083`, base MySQL `ventas` y paquete `cl.jajoyeria.pedidos`.

## Modelo y flujo

Liquibase crea `carritos`, `carrito_items`, `pedidos`, `pedido_items`, `intentos_pago` y `accesos_invitado`. Las líneas de pedido conservan producto, nombre, precio unitario, cantidad y subtotal de la compra. Los UUID se almacenan en binario y los importes CLP en enteros.

El carrito pertenece a una cuenta o a una sesión invitada. Admite hasta 100 productos distintos y de 1 a 99 unidades por línea. El checkout consulta catálogo, exige un carrito comprable y comprueba el total mostrado al usuario. Una clave idempotente recupera la misma orden cuando se reintenta la solicitud con los mismos datos.

Mientras un carrito tenga un checkout pendiente, hay que resolver su pago antes de modificarlo. Confirmar el pago simulado vacía las líneas; cancelarlo conserva el carrito. Un reintento de compra tras un fallo utiliza una nueva clave de checkout. Las transacciones y los bloqueos coordinan el estado del carrito, el pedido y el intento de pago.

## API

| Método y ruta | Operación |
| --- | --- |
| `POST /v1/carrito` | Crear o recuperar la sesión de carrito. |
| `GET /v1/carrito` | Líneas, cotización y pedido pendiente. |
| `PUT /v1/carrito/items/{id}` | Fijar la cantidad de un producto. |
| `DELETE /v1/carrito/items/{id}` | Quitar una línea. |
| `POST /v1/checkout` | Crear o recuperar la orden de checkout. |
| `POST /v1/pedidos/{id}/pago-simulado` | Resolver con `CONFIRMAR` o `CANCELAR`. |
| `GET /v1/pedidos/{id}` | Consultar una orden autorizada. |
| `GET /v1/pedidos/{id}/comprobante` | Comprobante interno de una compra pagada. |
| `POST /v1/pedidos/{id}/vincular` | Vincular compra invitada resuelta a una cuenta. |
| `GET /v1/mis-pedidos` | Historial paginado del cliente. |
| `GET /v1/admin/pedidos` | Listado administrativo paginado. |
| `PATCH /v1/admin/pedidos/{id}/entrega` | Avanzar el despacho. |
| `GET /v1/admin/reportes/ventas` | Ingresos, ticket promedio, estados y principales productos y clientes. |

Los listados usan `page` y `size`. El checkout recibe `entrega` y `totalEsperadoClp` y exige el header `Idempotency-Key` con un UUID. Los clientes autenticados envían el JWT. Los invitados envían `X-Carrito-Token` para operar su carrito y `X-Pedido-Token` para consultar una orden.

## Ejemplo de compra invitada

Con catálogo y pedidos activos y pagos simulados habilitados:

```powershell
$api='http://localhost:8083/v1'
$sesion=Invoke-RestMethod -Method Post "$api/carrito"
$headers=@{'X-Carrito-Token'=$sesion.tokenInvitado}
Invoke-RestMethod -Method Put "$api/carrito/items/3" -Headers $headers -ContentType 'application/json' -Body '{"cantidad":2}'
$carrito=Invoke-RestMethod "$api/carrito" -Headers $headers
$headers['Idempotency-Key']=[guid]::NewGuid().ToString()
$body=@{entrega=@{nombre='Ana Soto';correo='ana@example.cl';direccion='Los Alerces 10';ciudad='Santiago'};totalEsperadoClp=$carrito.totalClp} | ConvertTo-Json
$checkout=Invoke-RestMethod -Method Post "$api/checkout" -Headers $headers -ContentType 'application/json' -Body $body
$id=$checkout.pedido.id
Invoke-RestMethod -Method Post "$api/pedidos/$id/pago-simulado" -Headers $headers -ContentType 'application/json' -Body '{"accion":"CONFIRMAR"}'
Invoke-RestMethod "$api/pedidos/$id/comprobante" -Headers @{'X-Pedido-Token'=$checkout.tokenConsulta}
```

El ejemplo deja una compra de demostración en la base utilizada. Conservar el token de consulta para recuperar esa orden como invitado.

## Autorización y estados

El titular y un administrador activo pueden consultar la orden; un invitado necesita su token privado, que dura 30 días. Vincular exige además una cuenta activa con el mismo correo, un pago resuelto y el token. La vinculación revoca ese acceso invitado. La autenticación local actual registra correos sin verificación por email.

El pago usa `PENDIENTE`, `PAGADO` y `FALLIDO`. La entrega usa `PENDIENTE`, `ENVIADO` y `COMPLETADO`. Despachar requiere pago aprobado y avance secuencial. La respuesta del comprobante es JSON para la vista imprimible del frontend. La integración con una pasarela real y un emisor tributario sigue pendiente.

## Ejecución

```powershell
$env:CATALOGO_INTERNAL_TOKEN='local-jarodandre-interno-2026'
$env:PAGO_SIMULADO='true'
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean verify
```

El perfil `mysql` usa `localhost:3307/ventas`. `MS_AUTH_URL` y `MS_CATALOGO_URL` apuntan por defecto a `localhost:8081` y `localhost:8082`. El token interno debe coincidir con catálogo. Con el simulador deshabilitado, checkout devuelve `503` hasta integrar un proveedor de pagos.

Consultar la [guía principal](../README.md) y [Swagger de pedidos](http://localhost:8083/swagger-ui/index.html), habilitado con `SWAGGER_ENABLED=true`.
