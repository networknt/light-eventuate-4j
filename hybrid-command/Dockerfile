FROM openjdk:8-jre-alpine
EXPOSE 8083
ADD /target/eventuate-command-1.4.6.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -cp /server.jar:/service/* com.networknt.server.Server"]
