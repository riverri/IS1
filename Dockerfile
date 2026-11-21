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
# El plan gratuito tiene 512 MB de memoria. preferIPv4Stack: que el servidor escuche en IPv4 (0.0.0.0),
# que es donde Render busca el puerto (ver server.address en application-nube.properties)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -Djava.net.preferIPv4Stack=true"
# Caché de clases (CDS): se arranca una vez la aplicación hasta tener el contexto de Spring listo y se para
# (spring.context.exit=onRefresh), guardando las clases que ha cargado. Así no se ejecuta nada ni se conecta al
# servidor: usa una base de datos H2 en memoria y el perfil por defecto. Al arrancar de verdad, Java lee las
# clases de la caché en vez de buscarlas y comprobarlas una a una dentro de los jar.
RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh -Dspring.profiles.active=default \
        -Dspring.datasource.url=jdbc:h2:mem:cds -Dapuestas.cripto.actualizacion-automatica=false \
        -jar apuestas-0.1.0-SNAPSHOT.jar
EXPOSE 8080
CMD ["java", "-XX:SharedArchiveFile=app.jsa", "-jar", "apuestas-0.1.0-SNAPSHOT.jar"]
