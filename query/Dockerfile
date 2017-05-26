
FROM openjdk:8-jre-alpine
EXPOSE 8080 8081 8082
ADD /target/eventuate-query-1.3.0.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -cp /server.jar:/service/* com.networknt.server.Server"]
