# Project to generate Java classes from ISO18626 XSD schema

ISO18626 xsd schema file is save in resource folder with global mapping file.

Java classes are generated from schema and added in JAR file.

JAR file can be published to ReShare reposilite repository using gradle task 
`./gradlew publish`. Before running this task you need to specify credentials in env var `REPOSOLITE_USERNAME` and `REPOSOLITE_PASSWORD`. 

