FROM eclipse-temurin:21-jdk
LABEL authors="giuli"

ENTRYPOINT ["top", "-b"]


FROM ubuntu:latest

WORKDIR /app

COPY target/projetoAssurance-0.0.1-SNAPSHOT.jar awsecr.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms512m", "-Xmx1536m", "-jar", "awsecr.jar"]

