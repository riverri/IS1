package es.ucm.fdi.is1.apuestas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de Better, la web de apuestas con moneditas virtuales.
 * <ul>
 *   <li>{@code @SpringBootApplication}: busca los componentes (controladores, servicios, repositorios…) en este
 *       paquete y sus subpaquetes y configura Spring automáticamente.</li>
 *   <li>{@code @ConfigurationPropertiesScan}: carga los records de configuración, como
 *       {@link es.ucm.fdi.is1.apuestas.api.ApiProperties}.</li>
 *   <li>{@code @EnableScheduling}: activa las tareas programadas de
 *       {@link es.ucm.fdi.is1.apuestas.api.SincronizacionProgramada}.</li>
 * </ul>
 *
 * <h2>Cómo leer este proyecto (guía para principiantes)</h2>
 * Cada paquete agrupa una parte de la web (apuestas, eventos, usuarios...). Dentro de cada uno, las clases se
 * reparten en capas, y una petición del navegador las recorre siempre en este orden:
 * <ol>
 *   <li><b>Controlador</b> ({@code ...Controller}, anotado con {@code @Controller}): recibe la petición HTTP
 *       (por ejemplo, el usuario pulsa "Apostar"), lee los datos del formulario y decide qué página enseñar.
 *       No contiene reglas de negocio: se las pide al servicio.</li>
 *   <li><b>Servicio</b> ({@code ...Service}, anotado con {@code @Service}): aplica las reglas del negocio
 *       (¿tiene saldo?, ¿el partido ha empezado?) dentro de una transacción.</li>
 *   <li><b>Repositorio</b> ({@code ...Repository}): una interfaz que Spring Data rellena sola; lee y guarda
 *       objetos en la base de datos sin que escribamos SQL.</li>
 *   <li><b>Entidad</b> (anotada con {@code @Entity}, como {@code Usuario} o {@code Apuesta}): una clase Java
 *       cuyos objetos se guardan como filas de una tabla de la base de datos.</li>
 * </ol>
 * Además hay <b>formularios</b> ({@code ...Form}: los campos que envía una página), <b>excepciones</b>
 * ({@code ...Exception}: errores con nombre propio) y <b>plantillas</b> HTML en {@code src/main/resources/templates}.
 *
 * <p><b>Inyección de dependencias:</b> casi ninguna clase hace {@code new} de otra. Spring crea un único objeto de
 * cada clase anotada ({@code @Service}, {@code @Controller}, {@code @Component}...) y se lo pasa al constructor de
 * quien lo pida. Por eso los constructores reciben repositorios y servicios como parámetros.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class ApuestasApplication {

    /** Arranca el servidor web embebido (puerto 8080, o el de la variable de entorno PORT). */
    public static void main(String[] args) {
        // Arranca Spring: crea todos los componentes, conecta la base de datos, aplica las migraciones de Flyway
        // y deja el servidor escuchando peticiones hasta que se pare el programa
        SpringApplication.run(ApuestasApplication.class, args);
    }
}
