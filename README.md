# EVM in Java

[![Build Status](https://travis-ci.org/semuxproject/evm.svg?branch=master)](https://travis-ci.org/semuxproject/evm)

EVM in Java is a standalone EVM implementation, derived from the EthereumJ project.

It's light-weight and can be easily integrated into other projects.

## How to use it?

```
<dependency>
    <groupId>com.github.semuxproject</groupId>
    <artifactId>evm</artifactId>
    <version>[GIT_COMMIT_HASH]</version>
</dependency>
```

## Build from source

```
mvn install
```


## Code style

```
mvn formatter:format license:format findbugs:check
```


## License

This projected is licensed in [LGPLv3](./LICENSE).