# Preguntas frecuentes

**¿Qué arquitectura tiene la aplicación?**
Una aplicación web Spring Boot por capas (MVC): plantillas Thymeleaf para la vista, controladores que reciben las peticiones, servicios con las reglas del negocio y las transacciones, y entidades JPA con sus repositorios sobre H2 o PostgreSQL. El código está organizado por funcionalidad en 12 paquetes ([Paquetes y clases](Paquetes-y-clases)).

**¿Cómo se calculan las cuotas?**
Nivel = calidad + 0,4 × forma; diferencia con 0,5 de ventaja para el local; probabilidad de empate según lo igualados que estén; el resto con una curva logística; ajuste por el dinero apostado (hasta un 30 %); y cuota = 1 / (probabilidad × 1,07) ([Algoritmo de cuotas](Algoritmo-de-cuotas)).

**¿Cómo se asegura que la casa no pierde a la larga?**
Con el margen: la suma de 1/cuota de un evento siempre es mayor que 1, porque las probabilidades se multiplican por 1,07 y la cuota se redondea hacia abajo. Las cuentas de la casa comparan el margen real con el teórico (6,5 %).

**¿Qué pasa si se corrige un resultado ya pagado?**
Las apuestas se reevalúan y el saldo se ajusta con la diferencia entre lo que debe cobrar y lo que ya cobró. Puede quedar negativo; es una decisión documentada (0002).

**¿Por qué Flyway?**
Porque las bases de datos guardadas no tenían las columnas nuevas al cambiar las entidades y la aplicación fallaba. Con Flyway cada cambio es un script numerado que se aplica una vez y en orden, sin perder datos.

**¿Cómo sabéis que funciona?**
266 pruebas automáticas que se ejecutan en cada pull request con H2 y con PostgreSQL, y la matriz de trazabilidad, que enlaza los 92 criterios de aceptación de las historias hechas con su prueba (88 automáticos).

**¿Qué es una transacción y dónde la usáis?**
Un grupo de cambios que se hace entero o no se hace. Al apostar se descuenta el saldo y se guarda la apuesta: si algo falla, no se descuenta nada. En la sincronización con la API se usa una por partido para que un fallo no deshaga los demás.

**¿Cómo protegéis las contraseñas y las páginas?**
BCrypt para las contraseñas; Spring Security decide qué es público, qué exige sesión y qué exige ser creador; token CSRF en los formularios; bloqueo de 15 minutos tras 5 fallos ([Seguridad](Seguridad)).

**¿Qué es el MVP y cuándo lo tuvisteis?**
El producto mínimo viable: las filas Must del backlog (catálogo, cuentas, moneditas, cuotas, apuestas simples y combinadas, resolución y panel del creador). Quedó completo en el Sprint 4.

**¿Qué historias no se han hecho y por qué?**
HU-17 (recuperar la contraseña) está pendiente. HU-15 y HU-16 (cuenta bancaria) se descartaron porque no hay dinero real, y HU-38 a HU-43 (parte social completa) por su coste; en su lugar se hicieron el perfil público y las ligas privadas.

**¿De dónde salen los partidos reales?**
De la API de football-data.org cada 30 minutos; al terminar un partido se resuelven sus apuestas. La clave va en una variable de entorno ([API de datos deportivos](API-de-datos-deportivos)).

**¿Cómo está publicada?**
En Render (imagen Docker construida desde GitHub al fusionar en `main`), con la base de datos en Neon y UptimeRobot para que no se apague ([Ejecutar y desplegar](Ejecutar-y-desplegar)).

**¿Cuál es la cuenta del creador?**
`creador@apuestas.es` / `creador123` en local. En Render o con el túnel, la contraseña que se puso en `CREADOR_PASSWORD`.
