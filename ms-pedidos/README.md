# ms-pedidos

Carritos, checkout, pagos simulados, comprobantes y reportes. Puerto **8083**, esquema **ventas**.

- Invitados: `X-Carrito-Token` para el carrito y `X-Pedido-Token` para consultar pedidos privados.
- Checkout: `Idempotency-Key` UUID para evitar pedidos duplicados.
- Precios: cotización por lote en catálogo usando `X-Internal-Token`.
- Los pagos de demostración requieren `PAGO_SIMULADO=true`; no realizan cobros.
- Usuarios y administración: operaciones protegidas, pendientes de configurar autenticación.

Se conservan las entidades y migraciones, no los datos de las bases locales eliminadas.

Configuración genérica de PostgreSQL, ejecución y pruebas: [README principal](../README.md).
