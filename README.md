# SLF4J Simple Logger

![GraalVM Enabled](https://img.shields.io/badge/GraalVM-Ready-orange?style=plastic)
[![GitHub Action](https://github.com/goodforgod/slf4j-simple-logger/workflows/Java%20CI/badge.svg)](https://github.com/GoodforGod/slf4j-simple-logger/actions?query=workflow%3A%22Java+CI%22)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=ncloc)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)

[SLF4J](https://github.com/qos-ch/slf4j) based, simple, efficient logger.

Features:
- Performance optimizations (105-800% performance improvements).
- Logger name abbreviation (logback analog).
- Environment variables logging.
- Global logger level change.

## Dependency :rocket:

Java 11+ compatible.

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```groovy
implementation "io.goodforgod:slf4j-simple-logger:0.11.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```xml
<dependency>
    <groupId>io.goodforgod</groupId>
    <artifactId>slf4j-simple-logger</artifactId>
    <version>0.11.0</version>
</dependency>
```

Based on SLF4J 1.7.36

## Log Example

Below is example of logged message:
```java
logger.debug("Message is printed for this logger");
```

And the detailed result:
```text
       Date Time        Implementation  Log Level         Environment variables           Thread         Logger Name                     Log Message
           |                   |            |                        |                      |                |                               |
___________|__________   ______|_______   __|__   ___________________|__________________  __|__   ___________|___________    ________________|________________
|                     | |              | |     | |                                      | |    | |                       |  |                                |
|                     | |              | |     | |                                      | |    | |                       |  |                                |
2022-02-23T15:43:40.331 [0.9.0-SNAPSHOT] [DEBUG] [SESSIONNAME=Console, PROCESSOR_LEVEL=6] [main] io.goodforgod.Application - Message is printed for this logger
```

## Features

### Performance optimizations

This implementation is based on default *slf4j-simple-logger*, but there are plenty of performance improvements.

Some cases are 100% faster others are even 800% faster, you can read more about here in my [JVM benchmark]().

### DateTime output

### Logger name abbreviation

### Environment logging

### Logger level change

## Configuration

Example of full *simplelogger.properties* file:
```properties
# Default logging level for all loggers. Must be one of ("TRACE", "DEBUG", "INFO", "WARN", or "ERROR").
org.slf4j.simpleLogger.defaultLogLevel=INFO
# Set to true to show current datetime in output.
org.slf4j.simpleLogger.showDateTime=true
# Set datetime output type. Must be one of ("DATE_TIME", "UNIX_TIME", "MILLIS_FROM_START").
org.slf4j.simpleLogger.dateTimeOutputType=DATE_TIME
# The date and time formatter pattern to be used in the output.
org.slf4j.simpleLogger.dateTimeFormat=uuuu-MM-dd'T'HH:mm:ss.SSS
# Set to true if to show application implementation version from MANIFEST.MF
org.slf4j.simpleLogger.showImplementationVersion=false
# Set to true to show logging level in brackets like: [INFO]
org.slf4j.simpleLogger.levelInBrackets=true
# Set to true if to show current thread in output.
org.slf4j.simpleLogger.showThreadName=false
# Set to true to show only class name in output.
org.slf4j.simpleLogger.showShortLogName=false
# Set to true if to show full class name in output (package + class name).
org.slf4j.simpleLogger.showLogName=true
# Set maximum logger name to output and abbreviate if it exceeds length.
org.slf4j.simpleLogger.logNameLength=36
# Set environment names to show in output.
org.slf4j.simpleLogger.environments=SESSION_ID,ORIGIN,HOST
# Set to true to show environment with nullable values.
org.slf4j.simpleLogger.environmentShowNullable=false
# Set to true to show environment names.
org.slf4j.simpleLogger.environmentShowName=false
# Set to true to remember env values on configuration initialization.
org.slf4j.simpleLogger.environmentRememberOnStart=true
org.slf4j.simpleLogger.logFile=System.out

org.slf4j.simpleLogger.log.path.to.class=WARN
```

### Recommended configuration

Below is recommended minimal configuration.

*simplelogger.properties* file:
```properties
org.slf4j.simpleLogger.defaultLogLevel=INFO
org.slf4j.simpleLogger.logNameLength=36
org.slf4j.simpleLogger.logFile=System.out
```

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details