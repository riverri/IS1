package es.ucm.fdi.is1.apuestas.web;

import java.net.URI;

/** Ruta de esta misma web a la que volver (por ejemplo, la del Referer); si no es segura, la indicada. */
public final class RutaSegura {

    private RutaSegura() {
    }

    public static String de(String referer, String porDefecto) {
        if (referer == null) {
            return porDefecto;
        }
        try {
            URI uri = URI.create(referer);
            String ruta = uri.getRawPath();
            if (ruta == null || !ruta.startsWith("/") || ruta.startsWith("//") || ruta.startsWith("/login")) {
                return porDefecto;
            }
            return uri.getRawQuery() == null ? ruta : ruta + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return porDefecto;
        }
    }
}
