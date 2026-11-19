# ms-auth

Perfiles, roles, estados y territorios. Puerto **8081**, esquema **identidad**.

El proveedor de identidad está pendiente. Los endpoints de perfiles y administración permanecen protegidos y no aceptan tokens. La verificación de correo tampoco utiliza un proveedor externo.

Las regiones y comunas son públicas una vez configurada la persistencia. Se conservan entidades y migraciones SQL, pero no hay cuentas ni contraseñas locales.

Configuración genérica de PostgreSQL y ejecución: [README principal](../README.md).
