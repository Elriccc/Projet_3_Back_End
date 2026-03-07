FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q -e -C dependency:go-offline
COPY src ./src
RUN mvn package -DskipITs

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/app.jar app.jar

EXPOSE 8080 5005
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
ENTRYPOINT ["java","-jar","/app/app.jar"]