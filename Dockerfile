FROM amazoncorretto:17-alpine

COPY target/k8s-poc-0.0.1-SNAPSHOT.jar k8s-poc-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java","-jar","/k8s-poc-0.0.1-SNAPSHOT.jar"]