##
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY lib /home/app/lib
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#
# Package stage
#

FROM openjdk:11
WORKDIR /app
COPY target/rapid-scheduler-1.0-SNAPSHOT-jar-with-dependencies.jar ./ds.jar
COPY run_ds.sh ./
EXPOSE 9000
EXPOSE 9001
CMD ["sh","run_ds.sh"]
