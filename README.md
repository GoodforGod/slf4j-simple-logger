# SLF4J Simple Logger

![GraalVM Enabled](https://img.shields.io/badge/GraalVM-Ready-orange?style=plastic)
[![GitHub Action](https://github.com/goodforgod/slf4j-simple-logger/workflows/Java%20CI/badge.svg)](https://github.com/GoodforGod/slf4j-simple-logger/actions?query=workflow%3A%22Java+CI%22)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=coverage)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=GoodforGod_slf4j-simple-logger&metric=ncloc)](https://sonarcloud.io/dashboard?id=GoodforGod_slf4j-simple-logger)

SLF4J library simple logger implementation.

## Dependency :rocket:

[**Gradle**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```groovy
implementation "io.goodforgod:slf4j-simple-logger:0.10.0"
```

[**Maven**](https://mvnrepository.com/artifact/io.goodforgod/slf4j-simple-logger)
```xml
<dependency>
    <groupId>io.goodforgod</groupId>
    <artifactId>slf4j-simple-logger</artifactId>
    <version>0.10.0</version>
</dependency>
```

## Features

Library have same features as default SLF4J simple logger implementation except:
- Library adds set default log level capability for *SimpleLoggerFactory*

## Compatability

Library is compatible with SLF4J version *1.7.32*.

Java 11+ compatible.

## Comfiguration

Example of *simplelogger.properties* file for boostrap your project.

```properties
# Default logging detail level for all instances of SimpleLogger. Must be one of ("trace", "debug", "info", "warn", or "error").
org.slf4j.simpleLogger.defaultLogLevel=info

# Set to true if you want the current date and time to be included in output messages.
org.slf4j.simpleLogger.showDateTime=true

# The date and time format to be used in the output messages. The pattern describing the date and time format is the same that is used in java.text.SimpleDateFormat. If the format is not specified or is invalid, will output the number of milliseconds elapsed since startup.
org.slf4j.simpleLogger.dateTimeFormat=uuuu-MM-dd'T'HH:mm:ss.SSS

# Set to true if you want to output the current thread name.
org.slf4j.simpleLogger.showThreadName=false
# Set to true if you want the Logger instance name to be included in output messages.
org.slf4j.simpleLogger.showLogName=true
# Set to true if you want the last component of the name to be included in output messages.
org.slf4j.simpleLogger.showShortLogName=true
org.slf4j.simpleLogger.levelInBrackets=true
org.slf4j.simpleLogger.logFile=System.out
```

## License

This project licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details