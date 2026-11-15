# PRUEBAS (para el informe)



Hay 359 pruebas automáticas en 58 clases. Todas pasan.



### Herramientas: 



* JUnit 5 y AssertJ, con las de Spring Boot:
* MockMvc: simula peticiones a la web sin navegador.
* Spring Security Test: para entrar como un usuario concreto.
* Mockito: sustituye servicios externos.
* MockRestServiceServer: hace de servidor falso para las API.



### Tipos:



* Unitarias: prueban una clase sola, sin Spring ni base de datos. Ej.: CalculadoraCuotasTest, LmsrTest, EquipoTest, UsuarioTest, KeepAliveTest.
* De integración web (las que acaban en ...WebTest): arrancan la app entera con una base de datos H2 en memoria, creada con las mismas migraciones Flyway que la real, y simulan lo que hace un usuario. Ej.: "Ana inicia sesión, apuesta 10 y su saldo baja 10". Casi todas llevan @Transactional, así que lo que hacen se deshace al terminar.
* De las API externas: FootballDataClienteTest y CoinGeckoClienteTest, con un servidor falso. Ninguna prueba sale a Internet.
* De concurrencia: ConcurrenciaSaldoTest comprueba que dos apuestas a la vez no dejan mal el saldo.
* Con servidor real: CompresionTest arranca Tomcat en un puerto libre.
* De regresión: RevisionFallosWebTest tiene una prueba por cada uno de los 34 fallos de la revisión de código.
* De arranque: ApuestasApplicationTests comprueba que la app arranca.



### Dónde están:

* En src/test/java/es/ucm/fdi/is1/apuestas/, en carpetas con los mismos paquetes que el código (apuesta, cuotas, usuarios, cripto, prediccion, web...).
* Su configuración, en src/test/resources/config/application.properties.
* La tabla por paquete está en la página "Pruebas y calidad" de la wiki.



### Cuándo se ejecutan: 

En cada pull request y en cada cambio de main, con el CI de GitHub (.github/workflows/ci.yml). Se ejecutan dos veces, con H2 y con PostgreSQL.



### Relación con los requisitos: 

docs/requisitos/trazabilidad.md enlaza cada criterio de aceptación con la prueba que lo comprueba. Unos pocos se comprueban a mano porque dependen del navegador.

