# SLF4J Simple Logger

![GraalVM Enabled](https://img.shields.io/badge/GraalVM-Ready-orange?style=plastic)
[![GitHub Action](https://github.com/goodforgod/slf4j-simple-logger/workflows/Java%20CI/badge.svg)](https://github.com/GoodforGod/slf4j-simple-logger/actions?query=workflow%3A%22Java+CI%22)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=ncloc)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)

[SLF4J](https://github.com/qos-ch/slf4j) based, simple, efficient logger.

This logger is great for applications that use synchronous output or run in single-thread like command line applications or serverless applications.

Features:
- Performance optimizations.
- GraalVM friendly.
- Environment variables logging.
- [slf4j-simple-logger](https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html) compatible configuration.

## Dependency :rocket:

Java 11+ compatible.

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```groovy
implementation "io.goodforgod:slf4j-simple-logger:0.13.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```xml
<dependency>
    <groupId>io.goodforgod</groupId>
    <artifactId>slf4j-simple-logger</artifactId>
    <version>0.13.0</version>
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
       Date Time        Implementation  Log Level         Environment variables       Thread         Logger Name                     Log Message
           |                   |            |                        |                  |                |                               |
___________|__________   ______|_______   __|__   ___________________|______________  __|__   ___________|___________    ________________|________________
|                     | |              | |     | |                                  | |    | |                       |  |                                |
|                     | |              | |     | |                                  | |    | |                       |  |                                |
2022-02-23T15:43:40.331 [0.9.0-SNAPSHOT] [DEBUG] [SESSION=Console, PROCESSOR_LEVEL=6] [main] io.goodforgod.Application - Message is printed for this logger
```

## Features

### Performance optimizations

This implementation is based on default *slf4j-simple-logger*, but there are plenty of performance and feature improvements.

Some cases are 200% faster others are 800% faster, you can read more about here in my [JVM benchmark](https://github.com/GoodforGod/java-logger-benchmark).

### DateTime output

There are three options to output date & time:
1) DATE_TIME - in format *uuuu-MM-dd'T'HH:mm:ss.SSS*, example is - *2022-02-23T15:43:40.331* (read more about Java [Date & Time formats here](https://goodforgod.dev/posts/2/))
2) TIME - in format *HH:mm:ss.SSS*, example is - *15:43:40.331* (read more about Java [Date & Time formats here](https://goodforgod.dev/posts/2/))
3) UNIX_TIME - time [since epoch](https://en.wikipedia.org/wiki/Unix_time).
4) MILLIS_FROM_START - Millis from SimpleLoggerConfiguration initialization (may not properly work in GraalVM setups)

You can also change formatter for DATE_TIME and TIME via configuration:
```properties
org.slf4j.simpleLogger.dateTimeFormat=uuuu-MM-dd'T'HH:mm:ss.SSS
```

### Logger name abbreviation

There is configuration to abbreviate logger name, like in logback.

If configuration is:
```properties
org.slf4j.simpleLogger.showShortLogName=false
org.slf4j.simpleLogger.showLogName=true
org.slf4j.simpleLogger.logNameLength=36
```

And logger is:
```java
package io.goodforgod.internal.logger.example;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Application {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(Application.class);
        logger.info("Message is printed for this logger");
    }
}
```

Then logger name will be outputted like:
```text
2022-02-23T15:43:40.331 [DEBUG] i.g.i.logger.example.Application - Message is printed for this logger
```

Abbreviation happened to full logger name:
*io.goodforgod.internal.logger.example.Application* -> *i.g.i.logger.example.Application*

### Environment logging

It is possible to log environment, format how it will be outputted can be seen [here](#log-example).

If the configuration is:
```properties
# Set environment names to show in output. Envs will be printed out in order they preserve in configuration.
org.slf4j.simpleLogger.environments=SESSION,PROCESSOR_LEVEL
# Set to true to show environment with nullable values.
org.slf4j.simpleLogger.environmentShowNullable=false
# Set to true to show environment names.
org.slf4j.simpleLogger.environmentShowName=true
# Set to true to caches environment values on configuration initialization and then always uses them when logging.
org.slf4j.simpleLogger.environmentRememberOnStart=false
```

Then the output will be:
```text
2022-02-23T15:43:40.331 [DEBUG] [SESSION=Console, PROCESSOR_LEVEL=6] [main] io.goodforgod.Application - Message is printed for this logger
```

Environment variables are printed in order they preserve in configuration (in example above first SESSION, then PROCESSOR_LEVEL).

### Output split

There is possibility to split *WARN* and *ERROR* logs to different output.

If configuration is:
```properties
# Set default logger output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFile=System.out
# Set logger WARN logs output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFileWarn=System.error
# Set logger ERROR logs output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFileError=System.error
```

Then all logs of TRACE, DEBUG, INFO will be forwarded to *System.out* and all WARN & ERROR logs will be forwarded to *System.error*.

### Logger level change

You can change loggers level using *io.goodforgod.slf4j.simplelogger.SimpleLoggerFactory*:
```java
SimpleLoggerFactory factory = (SimpleLoggerFactory) LoggerFactory.getILoggerFactory();
factory.setLogLevel(Level.DEBUG);
```

Or you can use predicate to filter out loggers:
```java
SimpleLoggerFactory factory = (SimpleLoggerFactory) LoggerFactory.getILoggerFactory();
factory.setLogLevel(Level.DEBUG, logger -> logger.getName().startsWith("io.goodforgod.internal.logger.example"));
```

## Configuration

Library is fully compatibly with *slf4j-simple-logger* configuration, you can check it [here](https://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html).

Example of full *simplelogger.properties* file:
```properties
# Default logging level for all loggers. Must be one of ("TRACE", "DEBUG", "INFO", "WARN", or "ERROR"). (default INFO)
org.slf4j.simpleLogger.defaultLogLevel=INFO
# Set to true to show current datetime in output. (default true)
org.slf4j.simpleLogger.showDateTime=true
# Set datetime output type. Must be one of ("TIME", "DATE_TIME", "UNIX_TIME", "MILLIS_FROM_START"). (default DATE_TIME)
org.slf4j.simpleLogger.dateTimeOutputType=DATE_TIME
# The date and time formatter pattern to be used in the output. (default uuuu-MM-dd'T'HH:mm:ss.SSS)
org.slf4j.simpleLogger.dateTimeFormat=uuuu-MM-dd'T'HH:mm:ss.SSS
# Set to true if to show application implementation version from MANIFEST.MF (default false)
org.slf4j.simpleLogger.showImplementationVersion=false
# Set to true to show logging level in brackets like: [INFO] (default true)
org.slf4j.simpleLogger.levelInBrackets=true
# Set to true if to show current thread in output. (default false)
org.slf4j.simpleLogger.showThreadName=false
# Set to true to show only class name in output. (default false)
org.slf4j.simpleLogger.showShortLogName=false
# Set to true if to show full class name in output (package + class name). (default true)
org.slf4j.simpleLogger.showLogName=true
# Set maximum logger name to output and abbreviate if it exceeds length. (default null)
org.slf4j.simpleLogger.logNameLength=36
# Set logger output charset to use (default UTF-8)
org.slf4j.simpleLogger.charset=UTF-8
# Set environment names to show in output. Envs will be printed out in order they preserve in configuration. (default null)
org.slf4j.simpleLogger.environments=SESSION_ID,ORIGIN,HOST
# Set to true to show environment with nullable values. (default false)
org.slf4j.simpleLogger.environmentShowNullable=false
# Set to true to show environment names. (default false)
org.slf4j.simpleLogger.environmentShowName=false
# Set to true to caches environment values on configuration initialization and then always uses them when logging. (default false)
org.slf4j.simpleLogger.environmentRememberOnStart=false
# Set default logger output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFile=System.out
# Set logger WARN logs output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFileWarn=System.out
# Set logger ERROR logs output file or System.out or System.error (default System.out)
org.slf4j.simpleLogger.logFileError=System.out


# Set log level for custom loggers
org.slf4j.simpleLogger.log.path.to.class=WARN
```

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details