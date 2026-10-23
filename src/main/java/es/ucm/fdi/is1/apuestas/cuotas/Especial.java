package es.ucm.fdi.is1.apuestas.cuotas;

/**
 * Otros tipos de apuesta de un partido de fútbol, además del 1X2 (HU-52):
 * doble oportunidad, más/menos de 2,5 goles y ambos marcan.
 * Cada uno tiene un símbolo corto, una descripción y el grupo en el que se muestra en la página del partido.
 * Sus cuotas las calcula {@link CalculadoraCuotas#especiales(double, double)}.
 */
public enum Especial {

    DOBLE_1X("1X", "Local o empate", "Doble oportunidad"),
    DOBLE_X2("X2", "Empate o visitante", "Doble oportunidad"),
    DOBLE_12("12", "Local o visitante", "Doble oportunidad"),
    MAS_2_5("+2,5", "Más de 2,5 goles", "Goles"),
    MENOS_2_5("-2,5", "Menos de 2,5 goles", "Goles"),
    AMBOS_SI("AM", "Ambos marcan", "Ambos marcan"),
    AMBOS_NO("NM", "No marcan los dos", "Ambos marcan");

    private final String simbolo;
    private final String descripcion;
    private final String grupo;

    Especial(String simbolo, String descripcion, String grupo) {
        this.simbolo = simbolo;
        this.descripcion = descripcion;
        this.grupo = grupo;
    }

    /** Las de doble oportunidad se deciden con el resultado; el resto necesitan el marcador. */
    public boolean isNecesitaMarcador() {
        // name() devuelve el nombre del valor tal como se escribe en el código: "DOBLE_1X", "MAS_2_5"...
        return !name().startsWith("DOBLE");
    }

    /**
     * Si la selección se ha acertado. Devuelve null si no se puede saber porque solo se conoce el ganador
     * y no el marcador; entonces la selección se anula (cuenta con cuota 1,00).
     */
    public Boolean acierta(Resultado resultado, Integer golesLocal, Integer golesVisitante) {
        if (!isNecesitaMarcador()) {
            // Doble oportunidad: se acierta si no sale el único resultado que no cubre.
            // "this" es el valor del enum sobre el que se llama (por ejemplo, DOBLE_1X)
            return switch (this) {
                case DOBLE_1X -> resultado != Resultado.VISITANTE;
                case DOBLE_X2 -> resultado != Resultado.LOCAL;
                // DOBLE_12
                default -> resultado != Resultado.EMPATE;
            };
        }
        if (golesLocal == null || golesVisitante == null) {
            return null;
        }
        int total = golesLocal + golesVisitante;
        boolean ambos = golesLocal > 0 && golesVisitante > 0;
        // Los goles son enteros: más de 2,5 es 3 o más y menos de 2,5 es 2 o menos
        return switch (this) {
            case MAS_2_5 -> total > 2;
            case MENOS_2_5 -> total < 3;
            case AMBOS_SI -> ambos;
            // AMBOS_NO
            default -> !ambos;
        };
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getGrupo() {
        return grupo;
    }
}
