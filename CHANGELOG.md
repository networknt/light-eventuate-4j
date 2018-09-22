# Change Log

## [1.5.19](https://github.com/networknt/light-eventuate-4j/tree/1.5.19) (2018-09-22)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.18...1.5.19)

**Closed issues:**

- refactor StringUtil to StringUtils in the utility [\#89](https://github.com/networknt/light-eventuate-4j/issues/89)

## [1.5.18](https://github.com/networknt/light-eventuate-4j/tree/1.5.18) (2018-08-16)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.17...1.5.18)

**Closed issues:**

- flatten the config files into the same directory for k8s [\#86](https://github.com/networknt/light-eventuate-4j/issues/86)
- upgrade to undertow 2.0.11.Final [\#85](https://github.com/networknt/light-eventuate-4j/issues/85)
- Can CDC server work with Oracle Streams instead of Golden Gate? [\#79](https://github.com/networknt/light-eventuate-4j/issues/79)

## [1.5.17](https://github.com/networknt/light-eventuate-4j/tree/1.5.17) (2018-07-15)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.16...1.5.17)

## [1.5.16](https://github.com/networknt/light-eventuate-4j/tree/1.5.16) (2018-07-05)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.15...1.5.16)

## [1.5.15](https://github.com/networknt/light-eventuate-4j/tree/1.5.15) (2018-06-18)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.14...1.5.15)

**Closed issues:**

- remove maria db client from the dependencies [\#84](https://github.com/networknt/light-eventuate-4j/issues/84)
- upgrade curator version to 4.0.1 [\#83](https://github.com/networknt/light-eventuate-4j/issues/83)
- upgrade mysql binlog version to 0.16.1 [\#82](https://github.com/networknt/light-eventuate-4j/issues/82)

## [1.5.14](https://github.com/networknt/light-eventuate-4j/tree/1.5.14) (2018-05-19)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.13...1.5.14)

**Closed issues:**

- Test mariadb for binlog CDC support [\#78](https://github.com/networknt/light-eventuate-4j/issues/78)
- \[question\] Using MongoDB as event store and ActiveMQ as broker [\#77](https://github.com/networknt/light-eventuate-4j/issues/77)

## [1.5.13](https://github.com/networknt/light-eventuate-4j/tree/1.5.13) (2018-04-20)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.12...1.5.13)

**Closed issues:**

- cdc retry 5 times and return immediately if succeed [\#76](https://github.com/networknt/light-eventuate-4j/issues/76)
- move JdkTimer scheduler to the same package as the interface [\#75](https://github.com/networknt/light-eventuate-4j/issues/75)
- reduce connection pool size to 2 in cdc and service [\#74](https://github.com/networknt/light-eventuate-4j/issues/74)
- enable cdc test case again [\#73](https://github.com/networknt/light-eventuate-4j/issues/73)
- add build.sh and Dockerfile for tram cdcserver docker release [\#72](https://github.com/networknt/light-eventuate-4j/issues/72)
- remove the restclient package [\#71](https://github.com/networknt/light-eventuate-4j/issues/71)
- abstract producer and consumer interface to support other message brokers [\#70](https://github.com/networknt/light-eventuate-4j/issues/70)
- cdc server to mysql database root password cannot be externalized [\#66](https://github.com/networknt/light-eventuate-4j/issues/66)

## [1.5.12](https://github.com/networknt/light-eventuate-4j/tree/1.5.12) (2018-04-08)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.11...1.5.12)

**Closed issues:**

- remove unused dependencies in eventuate-client [\#68](https://github.com/networknt/light-eventuate-4j/issues/68)
- captured this error from light-bot auto build [\#50](https://github.com/networknt/light-eventuate-4j/issues/50)

## [1.5.11](https://github.com/networknt/light-eventuate-4j/tree/1.5.11) (2018-03-31)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.10...1.5.11)

**Closed issues:**

- support multiple packages in eventuate-client.yml [\#67](https://github.com/networknt/light-eventuate-4j/issues/67)
- change the signature of light-hybrid-4j handler to pass exchange [\#65](https://github.com/networknt/light-eventuate-4j/issues/65)
- disable two test cases that fail occasionally [\#64](https://github.com/networknt/light-eventuate-4j/issues/64)
- remove apache commons-lang dependency [\#62](https://github.com/networknt/light-eventuate-4j/issues/62)
- mysql binlog integration test failed when cdcserver for eventuate is running [\#59](https://github.com/networknt/light-eventuate-4j/issues/59)
- rollback mysqluser update and switch back to root as mysqlbinlog needs it. [\#58](https://github.com/networknt/light-eventuate-4j/issues/58)
- change publishing exception to runtime exception [\#57](https://github.com/networknt/light-eventuate-4j/issues/57)
- switch mysql database user to mysqluser instead of root [\#56](https://github.com/networknt/light-eventuate-4j/issues/56)
- upgrade to 1.5.11 in develop branch [\#55](https://github.com/networknt/light-eventuate-4j/issues/55)
- add jar file into gitignore [\#54](https://github.com/networknt/light-eventuate-4j/issues/54)
- \[question\] Event store can be used to fully recover business service local database store? [\#53](https://github.com/networknt/light-eventuate-4j/issues/53)

**Merged pull requests:**

- fixes \#62 remove apache commons-lang dependency [\#63](https://github.com/networknt/light-eventuate-4j/pull/63) ([chenyan71](https://github.com/chenyan71))

## [1.5.10](https://github.com/networknt/light-eventuate-4j/tree/1.5.10) (2018-03-02)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.9...1.5.10)

**Implemented enhancements:**

- Reconnect or retry from CDC server to Kafka [\#25](https://github.com/networknt/light-eventuate-4j/issues/25)

**Closed issues:**

- remove warnings for java-doc [\#52](https://github.com/networknt/light-eventuate-4j/issues/52)

## [1.5.9](https://github.com/networknt/light-eventuate-4j/tree/1.5.9) (2018-02-21)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.8...1.5.9)

**Closed issues:**

- throwing RuntimeException breaks light-saga-4j integration tests [\#51](https://github.com/networknt/light-eventuate-4j/issues/51)
- disable travis ci develop branch build [\#49](https://github.com/networknt/light-eventuate-4j/issues/49)

## [1.5.8](https://github.com/networknt/light-eventuate-4j/tree/1.5.8) (2018-02-03)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.7...1.5.8)

**Closed issues:**

- consolidate both consumer and producer to use kafka.yml for config [\#46](https://github.com/networknt/light-eventuate-4j/issues/46)
- \[discusssion\] Go away from broker centric systems and SQL database to get performance [\#45](https://github.com/networknt/light-eventuate-4j/issues/45)
- update readme.md to move document site to doc.networknt.com [\#44](https://github.com/networknt/light-eventuate-4j/issues/44)

## [1.5.7](https://github.com/networknt/light-eventuate-4j/tree/1.5.7) (2018-01-09)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.6...1.5.7)

**Closed issues:**

- remove docs folder and move it to light-doc repo [\#43](https://github.com/networknt/light-eventuate-4j/issues/43)

## [1.5.6](https://github.com/networknt/light-eventuate-4j/tree/1.5.6) (2017-12-28)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.1...1.5.6)

**Closed issues:**

- maven build warnings [\#41](https://github.com/networknt/light-eventuate-4j/issues/41)
- Need to update service.yml for four services to remove Java SPI [\#39](https://github.com/networknt/light-eventuate-4j/issues/39)
- Upgrade to light-4j 1.5.4 [\#38](https://github.com/networknt/light-eventuate-4j/issues/38)
- \[question\] How messages are distributed across cluster of services? [\#37](https://github.com/networknt/light-eventuate-4j/issues/37)
- \[question\] event of chain rollback [\#35](https://github.com/networknt/light-eventuate-4j/issues/35)
- \[question\] Monitoring of the ecosystem [\#34](https://github.com/networknt/light-eventuate-4j/issues/34)
- \[question\] How mature or industrial proven is this framework? [\#33](https://github.com/networknt/light-eventuate-4j/issues/33)

**Merged pull requests:**

- Refactor polling cdc for oracle as required by user [\#40](https://github.com/networknt/light-eventuate-4j/pull/40) ([chenyan71](https://github.com/chenyan71))

## [1.5.1](https://github.com/networknt/light-eventuate-4j/tree/1.5.1) (2017-11-09)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.5.0...1.5.1)

## [1.5.0](https://github.com/networknt/light-eventuate-4j/tree/1.5.0) (2017-10-23)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.6...1.5.0)

**Closed issues:**

- Upgrade dependencies and add maven-version [\#32](https://github.com/networknt/light-eventuate-4j/issues/32)

## [1.4.6](https://github.com/networknt/light-eventuate-4j/tree/1.4.6) (2017-09-24)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.4...1.4.6)

## [1.4.4](https://github.com/networknt/light-eventuate-4j/tree/1.4.4) (2017-09-21)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.3...1.4.4)

## [1.4.3](https://github.com/networknt/light-eventuate-4j/tree/1.4.3) (2017-09-10)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.2...1.4.3)

## [1.4.2](https://github.com/networknt/light-eventuate-4j/tree/1.4.2) (2017-08-31)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.1...1.4.2)

**Closed issues:**

- Update to use the latest signature of SingletonServiceFactory [\#31](https://github.com/networknt/light-eventuate-4j/issues/31)

## [1.4.1](https://github.com/networknt/light-eventuate-4j/tree/1.4.1) (2017-08-30)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.4.0...1.4.1)

**Closed issues:**

- Upgrade to newer version for Undertow and Jackson [\#29](https://github.com/networknt/light-eventuate-4j/issues/29)

## [1.4.0](https://github.com/networknt/light-eventuate-4j/tree/1.4.0) (2017-08-23)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.5...1.4.0)

**Closed issues:**

- Replace Client with Http2Client and remove apache httpclient [\#28](https://github.com/networknt/light-eventuate-4j/issues/28)
- rest-query cannot be started [\#27](https://github.com/networknt/light-eventuate-4j/issues/27)
- Upgrade to Undertow 1.4.18.Final for Http2 [\#26](https://github.com/networknt/light-eventuate-4j/issues/26)
- One of the test cases failed during build [\#18](https://github.com/networknt/light-eventuate-4j/issues/18)
- Make cdcservice to support postgresql database along with mysql. [\#15](https://github.com/networknt/light-eventuate-4j/issues/15)

## [1.3.5](https://github.com/networknt/light-eventuate-4j/tree/1.3.5) (2017-08-02)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.4...1.3.5)

**Closed issues:**

- refactor hybrid-command and hybrid-query servers [\#24](https://github.com/networknt/light-eventuate-4j/issues/24)
- Disable cdc-mysql test cases so that it build on be done without mysql/zookeeper/kafka dependencies. [\#23](https://github.com/networknt/light-eventuate-4j/issues/23)
- Move docker-compose files to light-docker so that you don't need to checkout light-eventuate-4j while building services [\#22](https://github.com/networknt/light-eventuate-4j/issues/22)
- Refactor light-eventuate-4j to sync with eventuate-local and eventuate-client [\#21](https://github.com/networknt/light-eventuate-4j/issues/21)
- add document folder [\#5](https://github.com/networknt/light-eventuate-4j/issues/5)

## [1.3.4](https://github.com/networknt/light-eventuate-4j/tree/1.3.4) (2017-07-08)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.3...1.3.4)

**Closed issues:**

- Another failed test case [\#19](https://github.com/networknt/light-eventuate-4j/issues/19)

## [1.3.3](https://github.com/networknt/light-eventuate-4j/tree/1.3.3) (2017-07-01)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.2...1.3.3)

## [1.3.2](https://github.com/networknt/light-eventuate-4j/tree/1.3.2) (2017-06-14)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.1...1.3.2)

**Closed issues:**

- Add cleanup.sh to remove docker containers and images for light-eventuate-4j [\#17](https://github.com/networknt/light-eventuate-4j/issues/17)

## [1.3.1](https://github.com/networknt/light-eventuate-4j/tree/1.3.1) (2017-06-04)
[Full Changelog](https://github.com/networknt/light-eventuate-4j/compare/1.3.0...1.3.1)

**Closed issues:**

- docker integration test issue fix [\#16](https://github.com/networknt/light-eventuate-4j/issues/16)
- Add cdcservice to command server in Dockerfile [\#14](https://github.com/networknt/light-eventuate-4j/issues/14)
- Add docker-compose to start all services together [\#13](https://github.com/networknt/light-eventuate-4j/issues/13)
- Add cdcservice as light-hybrid-4j service and add command and query servers [\#12](https://github.com/networknt/light-eventuate-4j/issues/12)
- Change parent artifact name and version as well as all sub projects. [\#11](https://github.com/networknt/light-eventuate-4j/issues/11)
- add comments on the API for java doc API document  [\#10](https://github.com/networknt/light-eventuate-4j/issues/10)
- handle publish events to Kafka by program [\#9](https://github.com/networknt/light-eventuate-4j/issues/9)

## [1.3.0](https://github.com/networknt/light-eventuate-4j/tree/1.3.0) (2017-05-06)
**Closed issues:**

- change project names to 4j as Java is a trademark of Oracle [\#8](https://github.com/networknt/light-eventuate-4j/issues/8)
- initial admin console for event sourcing platform [\#7](https://github.com/networknt/light-eventuate-4j/issues/7)
- change event handle process [\#6](https://github.com/networknt/light-eventuate-4j/issues/6)
- readme update [\#3](https://github.com/networknt/light-eventuate-4j/issues/3)
- cds kafka producer/consumer core service update [\#2](https://github.com/networknt/light-eventuate-4j/issues/2)
- Upgrade to light-java framework 1.2.7 and change the config format to yml [\#1](https://github.com/networknt/light-eventuate-4j/issues/1)



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*