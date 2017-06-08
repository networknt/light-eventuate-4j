FROM openjdk:8-jre-alpine

ADD /target/eventuate-cdcservice-1.3.1.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -cp /server.jar com.networknt.server.Server"]
