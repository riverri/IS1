# Imagen para publicar la aplicación en un servidor (Render, ver README).
# 1) Compila con Maven; 2) la ejecuta solo con Java, sin el código fuente.

FROM eclipse-temurin:21-jdk AS compilacion
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/target/apuestas-0.1.0-SNAPSHOT.jar app.jar
ENV SPRING_PROFILES_ACTIVE=nube
# El plan gratuito tiene 512 MB de memoria
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
