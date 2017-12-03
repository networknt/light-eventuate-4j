FROM openjdk:8-jre-alpine

ADD /target/cdc-server-1.5.4.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -cp /server.jar com.networknt.server.Server"]
