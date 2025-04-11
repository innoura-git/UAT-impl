FROM azul/zulu-openjdk:21-latest AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install
FROM azul/zulu-openjdk:21-latest
WORKDIR /

COPY --from=build /app/target/two.jar app-two.jar

EXPOSE 4002

ENTRYPOINT ["java", "-jar", "app-two.jar"]
