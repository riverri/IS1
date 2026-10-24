package es.ucm.fdi.is1.apuestas.cripto;

/**
 * Tipo de criptomoneda, para agruparlas y avisar del riesgo (HU-69). Las memecoins van primero: son las protagonistas
 * de la sección por lo mucho que se mueven, y por eso llevan su aviso de riesgo muy alto.
 */
public enum CategoriaCripto {

    MEMECOIN("Memecoins", "🐸", "Riesgo muy alto: su precio puede subir o hundirse mucho en horas"),
    REFERENCIA("De referencia", "🪙", "Riesgo alto: también son volátiles, aunque menos que las memecoins");

    private final String nombre;
    private final String icono;
    private final String riesgo;

    CategoriaCripto(String nombre, String icono, String riesgo) {
        this.nombre = nombre;
        this.icono = icono;
        this.riesgo = riesgo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getIcono() {
        return icono;
    }

    public String getRiesgo() {
        return riesgo;
    }
}
