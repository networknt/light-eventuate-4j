
FROM openjdk:8-jre-alpine
EXPOSE 8083 8084 8085
ADD /target/eventuate-command-1.3.0.jar server.jar
ADD /service/eventuate-cdcservice-1.3.0.jar cdcservice.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -cp /server.jar:/cdcservice.jar:/service/* com.networknt.server.Server"]
