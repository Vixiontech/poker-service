FROM maven:openjdk as maven

WORKDIR /app
COPY . .

RUN mvn package -DskipTests && cp target/*.jar app.jar

CMD ["mvn", "spring-boot:run", "-DskipTests"]

FROM openjdk:17-alpine

WORKDIR /app
COPY --from=maven /app/app.jar ./app.jar

CMD ["java", "-jar", "/app/app.jar"]
