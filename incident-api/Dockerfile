FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY . .
RUN ./mvnw -q -f aiops-incident-management/pom.xml -pl incident-api -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/aiops-incident-management/incident-api/target/incident-api-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
