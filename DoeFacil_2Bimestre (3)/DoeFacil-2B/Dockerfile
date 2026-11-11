FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN mvn -q package
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/classes classes
COPY --from=build /src/target/test-classes test-classes
COPY --from=build /src/target/dependency lib
CMD ["java","-cp","classes:lib/*","doefacil.Main"]
