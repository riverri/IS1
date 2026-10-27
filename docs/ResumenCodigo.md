# Análisis del código (A mano)

### 

### Introducción



He empezado leyendo y viendo por encima la distribución del proyecto, destaco que tiene una profunda raíz arraigada de carpetas, no es fácil encontrar el código que buscas a menos que sepas muy bien donde encontrarlo, se podría mejorar. Más bien nos vendría bien cambiarlo porque no es una estructura normal y puede llegar a ser confusa hasta para el propio equipo de trabajo.



#### Detalles sobre el código



En esta sección nos introduciremos a fondo en la explicación del código comprendido por el equipo de trabajo sin el uso de la IA. Cabe destacar que por el nivel que tenemos no somos capaces de comprender gran parte del código. Uno de las principales incógnitas la cual no vamos a profundizar pues no es relevante son los archivos referidos a configuración tanto de entornos locales como de entornos en la nube ya sea dockers, render, neón u otra tecnología similar. Sin embargo uno de los archivos que si podemos destacar son los .gitignore y todos aquellos que usamos para el control de versiones. Estos archivos son muy útiles para hacer un buen uso y buena praxis de los sistemas de control de versiones que usamos actualmente.



##### SRC.test



Esta es la sección donde podemos encontrar el código mayormente en java. La vemos dividida en dos ramas: test y main. Empezaremos por test, dentro de esta carpeta podemos encontrar los diferentes test que ha creado y comprobado la IA sobre cada parte del código. Encontramos toda la codificación de las pruebas en principio de caja blanca en la subcarpeta java (Con unas cuantas subcarpetas mas completamente innecesarias). Por otro lado tenemos resources donde encontramos la configuración para las diversas pruebas.



##### SRC.main



En esta otra rama podemos encontrar todo el código principal para nuestra página web. Hablaremos de dos grandes partes por un lado los recursos y por otra el código en si.



###### resources

Aquí encontramos dos cosas muy importantes por un lado todas las configuraciones necesarias para el proyecto y por otro lado todos los archivos que usamos de recursos principales para el proyecto entre ellos destacamos por un lado los archivos para la visualización de la página como los templates (esquemas en html) y los .css y por otra parte encontramos todos los archivos necesarios para inicializar la base de datos, un total de 10 archivos .sql para inicializar y construir las tablas del proyecto.



###### Java

En esta otra sección si vamos al fondo de las carpetas podemos encontrar el propio código en java de la aplicación, esta parte se trata del backend. Siendo realista se trata 12 paquetes con sus clases (+10 por paquete) y no creo que llegue a leerlas todas pues se trata de miles de líneas de código pero puedo dar una visión general de estos paquetes, en esta primera visión y revisión general del proyecto no voy a profundizar mucho en estos paquetes pero en un futuro la idea es tener conocimiento de que hace cada paquete sin marguen de error. Por ahora sabemos que tenemos unos paquetes más generales que son:

* api
* web
* gestión
* config
* notificaciones

Estos paquetes tratan de controlar las acciones del usuario en la página web y manejar el flujo de las acciones. Por otro lado tenemos un conjunto de paquetes que nos sirven más para manejar el contenido de la información que mostramos como son los siguientes paquetes:

* equipos
* eventos
* ligas
* mercados

Y por último otros paquetes que son fundamentales que involucran muchas funcionalidades de nuestra página web, que son:

* apuesta
* usuarios



##### Conclusión (por el momento)



Hemos dado una visión general del proyecto sin adentrarnos en el código en profundidad, en la próxima revisión trataremos de cubrir todo el código que conocemos (java) y empezar a aprender los conceptos básicos de otros lenguajes y tecnologías que no dominamos tanto (html, css, sql...).





&#x20;

