# EVM in Java

[![Build Status](https://travis-ci.com/semuxproject/evm.svg?branch=master)](https://travis-ci.com/semuxproject/evm)

EVM in Java is a standalone EVM implementation, derived from the EthereumJ project.

It's light-weight and can be easily integrated into other projects.

## How to use it?

```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.semuxproject</groupId>
    <artifactId>evm</artifactId>
    <version>[GIT_COMMIT_HASH]</version>
</dependency>
```

## Build from source

```
git clone https://github.com/semuxproject/evm
cd evm
mvn install
```


## Code style

To format the source code, run the following command:
```
mvn formatter:format license:format
```


## License

This projected is licensed under [LGPLv3](./LICENSE).
