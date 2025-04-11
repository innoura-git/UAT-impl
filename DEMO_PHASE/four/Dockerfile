FROM azul/zulu-openjdk:21-latest AS build

RUN apt-get update && apt-get install -y maven
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean install
FROM azul/zulu-openjdk:21-latest
WORKDIR /

COPY --from=build /app/target/four.jar app-four.jar

EXPOSE 4004

ENTRYPOINT ["java", "-jar", "app-four.jar"]
