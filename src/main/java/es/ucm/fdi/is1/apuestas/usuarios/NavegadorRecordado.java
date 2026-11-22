package es.ucm.fdi.is1.apuestas.usuarios;

import java.time.LocalDateTime;

/** Safe browser details shown in the account page without exposing the persistent token. */
public record NavegadorRecordado(String serie, String descripcion, LocalDateTime creado,
                                 LocalDateTime ultimoUso) {
}
