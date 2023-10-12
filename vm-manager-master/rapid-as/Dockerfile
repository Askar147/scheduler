FROM openjdk:8-jdk-oraclelinux7
WORKDIR /rapid
COPY ./rapid-linux-as.jar ./

CMD ["java", "-Djava.library.path=./rapid-server/libs/", "-jar", "rapid-linux-as.jar"]


