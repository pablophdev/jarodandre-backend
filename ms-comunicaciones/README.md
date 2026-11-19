# ms-comunicaciones

Contacto, suscripciones y bandeja administrativa. Puerto **8084**, esquema **comunicaciones**.

`POST /v1/contacto` y `POST /v1/suscripciones` son públicos una vez configurada la persistencia.

La administración permanece protegida y deshabilitada hasta configurar autenticación. Los listados utilizan `pagina` y `tamano`. El servicio no envía correos automáticamente.

Configuración genérica de PostgreSQL, ejecución y pruebas: [README principal](../README.md).
