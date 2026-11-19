# ms-catalogo

Productos, categorías, blog, imágenes y cotizaciones. Puerto **8082**, esquema **catalogo**.

Las lecturas de `/v1/productos`, `/v1/categorias` y `/v1/publicaciones` son públicas. La administración permanece protegida y deshabilitada hasta configurar un proveedor de identidad.

`POST /interno/v1/cotizaciones` exige el secreto `X-Internal-Token`, configurado como `CATALOGO_INTERNAL_TOKEN` en catálogo y pedidos.

Las imágenes utilizan multipart con campo `imagen`; el almacenamiento multimedia es independiente de la autenticación y la persistencia SQL.

Configuración de PostgreSQL, ejecución y pruebas: [README principal](../README.md).
