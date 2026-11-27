# Imagen para publicar la aplicación en un servidor (Render, ver README).
# 1) Compila con Maven; 2) separa el jar en capas; 3) la ejecuta solo con Java, sin el código fuente, con una caché
# de clases (CDS) que se prepara al construir la imagen para que arranque antes.

FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests package
# Capas: las dependencias (que casi nunca cambian) van aparte de nuestro código, así al subir un cambio solo se
# reconstruye y se descarga la capa pequeña de la aplicación
RUN java -Djarmode=tools -jar target/apuestas-0.1.0-SNAPSHOT.jar extract --layers --destination extraido

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/extraido/dependencies/ ./
COPY --from=compilacion /app/extraido/spring-boot-loader/ ./
COPY --from=compilacion /app/extraido/snapshot-dependencies/ ./
COPY --from=compilacion /app/extraido/application/ ./
ENV SPRING_PROFILES_ACTIVE=nube
# La aplicación ya fija su zona horaria; esto evita sorpresas en los registros del servidor
ENV TZ=Europe/Madrid
# El plan gratuito tiene 512 MB de memoria y Java gasta unos 290 MB fijos fuera del montón (clases, código compilado,
# símbolos). Con el 70 % para el montón se pasaba de 512 MB y Render reiniciaba la web (25/11). Medido en local con
# estos ajustes: unos 430 MB con carga y sesión iniciada; los datos vivos ocupan unos 84 MB, así que un montón del 35 %
# (unos 180 MB) sobra. TieredStopAtLevel=1: solo el compilador rápido, que gasta mucha menos memoria.
# preferIPv4Stack: que el servidor escuche en IPv4 (0.0.0.0), que es donde Render busca el puerto (server.address)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=35 -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=48m -XX:TieredStopAtLevel=1 -XX:+UseSerialGC -Xss512k -Djava.net.preferIPv4Stack=true"
# Menos zonas de memoria nativa por hilo y como mucho 50 hilos de Tomcat (el plan gratuito tiene 0,1 CPU)
ENV MALLOC_ARENA_MAX=2 SERVER_TOMCAT_THREADS_MAX=50
# Caché de clases (CDS): se arranca una vez la aplicación hasta tener el contexto de Spring listo y se para
# (spring.context.exit=onRefresh), guardando las clases que ha cargado. Así no se ejecuta nada ni se conecta al
# servidor: usa una base de datos H2 en memoria y el perfil por defecto. Al arrancar de verdad, Java lee las
# clases de la caché en vez de buscarlas y comprobarlas una a una dentro de los jar.
RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh -Dspring.profiles.active=default \
        -Dspring.datasource.url=jdbc:h2:mem:cds -Dapuestas.cripto.actualizacion-automatica=false \
        -jar apuestas-0.1.0-SNAPSHOT.jar
# La aplicación no se ejecuta como root: si alguien lograra ejecutar algo dentro del contenedor, no tendría permisos
# de administrador. Solo necesita leer /app, escribir en /tmp y, si no hay PostgreSQL configurado, guardar la base
# de datos H2 en /app/datos (spring.datasource.url=jdbc:h2:file:./datos/apuestas)
RUN useradd --system --uid 1001 --no-create-home apuestas && mkdir -p /app/datos && chown apuestas /app/datos
USER apuestas
EXPOSE 8080
CMD ["java", "-XX:SharedArchiveFile=app.jsa", "-jar", "apuestas-0.1.0-SNAPSHOT.jar"]
