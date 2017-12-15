This is the CDC (data change captuer) service that is built on top of light-rest-4j framework
and the service can be started standalone. It currently only support Mysql database and more
databases will be follow.

There is a docker-compose-cdc-server.yml in light-docker repo that can start this service.

The reason this service is not part of the docker-compose-eventuate.yml is due to another
service is available call cdc-service which is a microservice built on top of light-hybrid-4j
and can be started with hybrid-command server. In that case, it can save one more instance
of microservice.

This restful service should only be used if all other light-eventuate-4j services are implemented
with light-rest-4j.



