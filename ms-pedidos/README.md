# JA Joyería · ms-pedidos

Microservicio de carrito, checkout, pagos simulados, historial, comprobantes, despacho y reportes de ventas. Usa Java 21 y la misma estructura Spring Boot que ms-auth y ms-catalogo. Los paquetes controller, service, entity, repository, dto y config viven bajo cl.jajoyeria.pedidos. Los DTO son records.

## Inicio local

En dos terminales de PowerShell con Java 21:

```powershell
cd "$env:USERPROFILE\Desktop\ms-catalogo"
.\mvnw.cmd spring-boot:run
```

```powershell
cd "$env:USERPROFILE\Desktop\ms-pedidos"
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Catálogo escucha en 8082 y pedidos en 8083. La compra como invitado funciona con ambos servicios. Para las cuentas y la administración, configurar Cognito e iniciar también ms-auth en 8081.

En el perfil local, application-local.properties usa H2 persistente en data/ventas. Liquibase crea ventas.carritos, carrito_items, pedidos, pedido_items, intentos_pago y accesos_invitado mediante resources/db/changelog. Hibernate valida el esquema al arrancar. El servicio conserva solo hashes de los secretos de invitados.

## Probar una compra como invitado

```powershell
$api = 'http://localhost:8083/v1'
$sesion = Invoke-RestMethod -Method Post -Uri "$api/carrito"
$cabeceras = @{ 'X-Carrito-Token' = $sesion.tokenInvitado }
Invoke-RestMethod -Method Put -Uri "$api/carrito/items/3" -Headers $cabeceras -ContentType 'application/json' -Body '{"cantidad":2}'
$carrito = Invoke-RestMethod -Uri "$api/carrito" -Headers $cabeceras
$cabeceras['Idempotency-Key'] = [guid]::NewGuid().ToString()
$datos = @{ entrega = @{nombre='Ana Soto';correo='ana@example.cl';direccion='Los Alerces 10';ciudad='Santiago'}; totalEsperadoClp=$carrito.totalClp } | ConvertTo-Json
$checkout = Invoke-RestMethod -Method Post -Uri "$api/checkout" -Headers $cabeceras -ContentType 'application/json' -Body $datos
$id = $checkout.pedido.id
Invoke-RestMethod -Method Post -Uri "$api/pedidos/$id/pago-simulado" -Headers $cabeceras -ContentType 'application/json' -Body '{"accion":"CONFIRMAR"}'
Invoke-RestMethod -Uri "$api/pedidos/$id/comprobante" -Headers @{'X-Pedido-Token'=$checkout.tokenConsulta}
```

El servidor cotiza las líneas de nuevo y compara con totalEsperadoClp antes de crear la orden. Una diferencia devuelve 409. Se conserva una copia de nombres, precios y cantidades en el pedido. La disponibilidad CONSULTAR mantiene el comportamiento de compra de la demo. AGOTADO y los artículos retirados impiden confirmar. La v1 opera con disponibilidad declarada, sin reservas de inventario físico.

## Contrato HTTP

| Método y ruta | Operación |
|---|---|
| POST /v1/carrito | Crear sesión invitada o recuperar el carrito de la cuenta autenticada. Reenviar X-Carrito-Token permite recuperar la sesión invitada. |
| GET /v1/carrito | Leer líneas, disponibilidad, total vigente y número de checkout pendiente. |
| PUT /v1/carrito/items/{productoId} | Fijar cantidad de 1 a 99. Hasta 100 productos distintos por carrito. |
| DELETE /v1/carrito/items/{productoId} | Quitar la línea. |
| POST /v1/checkout | Crear el pedido pendiente o recuperar el mismo resultado al repetir Idempotency-Key (UUID) y los mismos datos. |
| POST /v1/pedidos/{id}/pago-simulado | CONFIRMAR aprueba y vacía el carrito. CANCELAR registra el fallo y conserva sus líneas. |
| GET /v1/mis-pedidos | Historial paginado de la cuenta, más reciente primero. |
| GET /v1/pedidos/{id} | Detalle para su titular, administrador o portador del token privado de consulta. |
| GET /v1/pedidos/{id}/comprobante | Datos imprimibles de una compra pagada. |
| POST /v1/pedidos/{id}/vincular | Vincular una compra de invitado ya resuelta a una cuenta activa con el mismo correo verificado y el token de consulta. |
| GET /v1/admin/pedidos | Listado paginado para administración. |
| PATCH /v1/admin/pedidos/{id}/entrega | Enviar un pedido pagado y después completarlo. Cuerpo con estado ENVIADO o COMPLETADO. |
| GET /v1/admin/reportes/ventas | Ingresos pagados, ticket promedio, cantidades por estado, diez productos y diez clientes principales. |

El token de carrito permite operar como invitado. El token de consulta es distinto para cada orden y dura 30 días. Para enlaces de confirmación en el frontend, recibir el secreto en el fragmento de la URL y enviarlo a la API en X-Pedido-Token. La vinculación revoca ese acceso de invitado. Las cuentas usan un access token de Cognito en Authorization. El servicio valida el JWT y consulta GET /v1/me de ms-auth para los permisos de negocio.

Solo puede haber un checkout pendiente por carrito. Mientras esté pendiente, primero hay que confirmarlo o cancelarlo para editar las líneas. Si se pierde la respuesta, se puede repetir la clave original o recuperar pedidoPendienteId del carrito. Repetir un resultado de pago idéntico devuelve el pedido existente. Un reintento tras cancelar comienza un checkout con una nueva clave. Los bloqueos de fila y transacciones hacen atómica la confirmación del pedido, el registro de pago y el vaciado.

## Configuración posterior

| Variable | Uso |
|---|---|
| SPRING_PROFILES_ACTIVE | local o postgres |
| DB_URL, DB_USERNAME, DB_PASSWORD | URL JDBC PostgreSQL y credenciales |
| DB_POOL_SIZE | Conexiones máximas por instancia, por defecto 4 |
| MS_CATALOGO_URL, MS_AUTH_URL | URL base de cada microservicio |
| COGNITO_ISSUER, COGNITO_CLIENT_ID | Emisor y cliente web de JA Joyería |
| COGNITO_TOKEN_URL, PEDIDOS_CLIENT_ID, PEDIDOS_CLIENT_SECRET | Token endpoint y credenciales del cliente de servicio con scope catalogo/cotizar |
| FRONTEND_ORIGIN, PORT | Origen CORS y puerto |
| PAGO_SIMULADO | En postgres es false por defecto. Activar explícitamente solo en un ambiente de demostración. |

En local se consultan las fichas públicas reales de catálogo, lo que permite probar la compra de invitado antes de configurar Cognito. CATALOGO_MODO=oauth permite probar la cotización por lote también en desarrollo. El perfil postgres usa client_credentials para llamar a POST /interno/v1/cotizaciones. Configurar PEDIDOS_CLIENT_ID con el mismo valor en ambos servicios.

Para Supabase usar TLS en DB_URL. Con un pooler en modo transacción añadir prepareThreshold=0. Se puede ejecutar Liquibase con una conexión de sesión y credenciales diferenciadas mediante SPRING_LIQUIBASE_URL, SPRING_LIQUIBASE_USER y SPRING_LIQUIBASE_PASSWORD. Otorgar los permisos de ejecución sobre las tablas y secuencias de ventas. Los datos locales de H2 pertenecen al ambiente de desarrollo.

El modo de pago actual es una simulación. Con el simulador deshabilitado, checkout devuelve 503 hasta integrar la pasarela real. El comprobante BOL es una representación interna de la compra.
