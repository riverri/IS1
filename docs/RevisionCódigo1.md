# Revisión de código 1



En este documento podrás buscar información sobre como funciona el código del paquete equipos. Los paquetes cuotas, eventos, ligas y mercados no se llegaron a revisar en este documento (ver el final).







Hemos visto que el código no tiene un sistema correcto para calcular las cuotas, a continuación revisamos que realiza el código y donde podrían estar los errores.



En la clase CalculadoraCuotas tenemos toda la lógica detrás del calculo de las cuotas usando la probabilidad. Siendo sincero no logro ver toda la lógica detrás de los cálculos de las cuotas, necesitaremos depurar en caso de que no se calculen correctamente. También debemos tener en cuenta que depende mucho de los datos recibidos de la api de deportes, con esto quiero decir que las cuotas pueden estar mal calculadas por que la propia api tenga errores en sus datos. 



Dentro de cuotas podemos encontrar las siguientes clases:

* Especial, Resultado y VolumenApostado, que son clases que únicamente se usan como enums e interfaces para definir el resto
* Cuotas, gestiona el sistema de cuotas es como un "controlador" para la generación de cuotas.
* CalculadoraCuotas, se encarga de en base a unos datos recibidos en este caso por la api generar las cuotas en cada caso



## Equipo



Aquí las clases se dividen de forma distinta porque debemos almacenar y trabajar con mas datos y deben estar bien diseñado. El paquete se divide de la siguiente manera:

* Competiciones, deporte, equipo, plantilla y alineación; se usan para crear todo con objetos y muchos tienen objetos de cada clase
* Clases auxiliares(nombres, posición) nos ayudan a generalizar casos



En este otro caso la estructura del código se parece mas a lo que estamos viendo en tecnología de la programación en cuanto al uso de java y la encapsulación.





\----------------------------------------------------------------------------

## Paquetes que no se revisaron aquí

Cuotas, eventos, ligas y mercados estaban asignados a Carlos Martín-Salas y quedaron sin revisar en este documento: en el Sprint 4 el trabajo pasó a la ampliación del producto. Se retiran como pendientes el 27/11 (defecto B-12 de la revisión técnica interna). Lo que hay de ellos está en otros sitios:

- **Cómo funcionan:** el modelo de clases y las secuencias principales están en [docs/diseno/uml.md](diseno/uml.md).
- **Fallos encontrados y corregidos:** en la wiki, [Revisión de fallos](https://github.com/riverri/IS1/wiki/Revisión-de-fallos) (revisión de código y nueve depuraciones).
