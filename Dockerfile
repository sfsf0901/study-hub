FROM openjdk:21-jdk

COPY ./build/libs/*SNAPSHOT.jar project.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]