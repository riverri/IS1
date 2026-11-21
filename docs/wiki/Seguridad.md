# Seguridad

| Medida | Cómo |
|---|---|
| Quién ve qué | `SeguridadConfig`: catálogo, equipos, mercados, ranking, perfiles, registro y login son públicos; `/gestion/**` y `/h2-console` solo para `CREADOR`; el resto pide sesión |
| Contraseñas | Cifradas con BCrypt; máximo 72 bytes |
| Fuerza bruta | 5 contraseñas incorrectas seguidas bloquean el email 15 minutos |
| Recuperar la contraseña | Enlace aleatorio que caduca a los 30 minutos y sirve una vez; en la base de datos solo se guarda su resumen SHA-256; la respuesta no desvela si el email existe |
| Sesiones | Al cambiar la contraseña o darse de baja se cierran las de otros navegadores |
| CSRF | Cada formulario POST lleva un token que añade Thymeleaf; otra web no puede enviar formularios en nombre del usuario |
| XSS | Thymeleaf escapa el texto (`th:text`); los nombres nunca se meten dentro de JavaScript |
| Cabeceras | Content-Security-Policy (solo se ejecutan los archivos de `/js`, nunca JavaScript dentro de una página; estilos y formularios, solo de la propia web), Referrer-Policy y Permissions-Policy en todas las páginas ([decisión 0005](https://github.com/riverri/IS1/blob/main/docs/decisiones/0005-cabeceras-y-limite-de-envios.md)) |
| Envíos masivos | Como mucho 10 registros y 10 peticiones de recuperar la contraseña cada 15 minutos por IP; después, error 429 «Demasiados intentos» (`LimitePeticiones`) |
| Exportaciones CSV | Un texto que empieza por `=`, `+`, `-` o `@` lleva un apóstrofo delante, para que Excel no lo ejecute como fórmula |
| Página de error | Enseña el código de la petición (el mismo de la cabecera `X-Request-Id` y del registro), para que quien avise de un error pueda decir cuál fue |
| Actuator | Solo `/actuator/health` está publicado, y solo dice UP o DOWN |
| Datos de otros | Cada servicio comprueba que la apuesta, la liga o el aviso son del usuario conectado |
| Creador | No puede apostar. En los perfiles públicos (`compartir`, `nube`) su contraseña es obligatoria |
| Concurrencia | Bloqueo optimista del saldo: dos operaciones a la vez no pueden dejarlo mal |
| Secretos | Clave de la API y contraseñas del servidor solo en variables de entorno |
