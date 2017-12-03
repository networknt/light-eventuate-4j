This module is tightly dependent on Mysql database and all test cases must be
executed when Mysql, Zookeeper and Kafka are running. Currently, all test cases
are disabled. If you are changing this module, you can start the light-eventuate-4j
environment by following this [tutorial](https://networknt.github.io/light-eventuate-4j/tutorial/service-dev/)
and enable all the test cases by remove the command on Test/Before annotations.
