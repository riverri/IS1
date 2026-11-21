# 0005 · Cabeceras de seguridad y límite de envíos por IP

**Estado:** PROPUESTA (21/11/2026). Entró con [riverri/IS1#150](https://github.com/riverri/IS1/pull/150).

## Contexto
- La web está publicada en Internet (Render). Spring Security ya pone CSRF y `X-Frame-Options`, pero no una política de contenido: si se colara HTML en una página, el navegador cargaría scripts de cualquier sitio.
- Registrarse y pedir el correo de recuperar la contraseña no piden sesión. Un programa podría crear miles de cuentas o llenar el buzón de alguien.

## Decisión
- **Content-Security-Policy:** scripts, estilos, fuentes y envíos de formularios, solo de la propia web.
  - Se permite `'unsafe-inline'` porque las plantillas llevan scripts y estilos dentro (las confirmaciones, el modo oscuro).
  - Las imágenes pueden venir de cualquier `https:` (escudos de la API, logotipos de criptomonedas, anuncios).
- **Referrer-Policy** `strict-origin-when-cross-origin` y **Permissions-Policy**, que desactiva la cámara, el micrófono, la ubicación, los pagos y el USB.
- **Límite por IP:** 10 envíos cada 15 minutos en `POST /registro` y otros 10 en `POST /recuperar`, con ventana deslizante en memoria. Al pasarse, la respuesta es un 429 con `Retry-After`.

## Alternativas descartadas
- **CSP sin `'unsafe-inline'` (con nonces):** obligaría a reescribir todos los `onsubmit="return confirm(…)"` de las plantillas. Se puede hacer más adelante.
- **Límite guardado en la base de datos:** sobrevive a un reinicio, pero cada intento sería una escritura. Con una sola instancia en Render, la memoria basta.
- **CAPTCHA:** depende de un servicio externo y empeora la accesibilidad (HU-97 a HU-101).

## Consecuencias
- Un script o una hoja de estilos de otra web (un CDN) no carga hasta añadir su dominio a `SeguridadConfig.CSP`.
- Varios jugadores detrás de la misma IP (la red de la facultad) comparten el límite. 10 cada 15 minutos da de sobra para una clase, y se sube con `apuestas.limite-peticiones.maximo`.
- El límite se reinicia al reiniciar la aplicación.
