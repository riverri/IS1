package es.ucm.fdi.is1.apuestas.web;

import java.net.URI;

/**
 * Ruta de esta misma web a la que volver (por ejemplo, la del Referer); si no es segura, la indicada.
 * Evita las redirecciones abiertas: un enlace preparado no puede mandar al usuario a otra web.
 */
public final class RutaSegura {

    // Clase de utilidades: no se crean objetos de ella
    private RutaSegura() {
    }

    /**
     * Ruta y parámetros de {@code referer}, sin el servidor; {@code porDefecto} si falta o no vale.
     *
     * @param referer    dirección a la que volver (puede ser null)
     * @param porDefecto ruta interna que se usa si la anterior no sirve
     */
    public static String de(String referer, String porDefecto) {
        if (referer == null) {
            return porDefecto;
        }
        try {
            URI uri = URI.create(referer);
            // Solo se queda la ruta: aunque el Referer sea de otro servidor, se vuelve a esta web
            String ruta = uri.getRawPath();
            // "//otra-web.com" el navegador lo entendería como otro servidor; a /login no tiene sentido volver
            if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//") || ruta.startsWith("/login")) {
                return porDefecto;
            }
            return uri.getRawQuery() == null ? ruta : ruta + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            // No es una dirección bien formada
            return porDefecto;
        }
    }
}
