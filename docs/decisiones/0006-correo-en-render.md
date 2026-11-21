# 0006 · Correo desde el servidor gratuito de Render

**Estado:** PROPUESTA (21/11/2026). Pendiente de probar con una cuenta real.

## Contexto
- La aplicación manda correos para recuperar la contraseña (HU-17), al ganar (HU-77) y el resumen semanal (HU-92). Usa SMTP con las variables `MAIL_*`.
- Desde el 26/09/2025, Render bloquea en los servicios gratuitos las conexiones salientes a los puertos 25, 465 y 587 ([aviso de Render](https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports)). Con Gmail, el envío no conecta y el enlace solo queda en el registro.

## Decisión
- Usar un servicio de envío que acepte SMTP por otro puerto: **Brevo** por el **2525** (`smtp-relay.brevo.com`). Es gratis hasta 300 correos al día.
- La aplicación no cambia: solo las variables `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `APUESTAS_CORREO_REMITENTE` en Render. Los pasos están en el README.

## Alternativas descartadas
- **Plan de pago de Render** (unos 7 $ al mes): deja usar el 587, pero el proyecto tiene que poder funcionar gratis.
- **API HTTP del servicio de correo:** obligaría a escribir un cliente para un proveedor concreto. Con SMTP se puede cambiar de proveedor tocando solo las variables.

## Consecuencias
- Si Brevo tampoco conectara desde Render, los correos seguirían sin salir (la web funciona igual, y el enlace de recuperar sale en *Logs*). En ese caso habría que replantear esta decisión.
- Las claves de Brevo van solo en Render, nunca en el repositorio.
