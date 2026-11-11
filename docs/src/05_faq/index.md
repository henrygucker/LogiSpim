# Frequently Asked Questions (FAQ)

The LogiSpim's frequently asked questions are separated into the following
categories depending on which components of LogiSpim they pertain to:
 - [Application FAQ](01_application_faq/index.md)
 - [Processor FAQ](02_processor_faq/index.md)
 - [Overall FAQ](#overall-faq)

## Overall FAQ
Click on any question below to view an answer.

<details>
    <summary><h3 style="display: inline; margin: 0">Q: How do I install LogiSpim?</h3></summary>

See the [installation guide](../03_installation/index.md).
___
</details><br/>

<details>
    <summary><h3 style="display: inline; margin: 0">Q: How do I run a program on LogiSpim?</h3></summary>

See the [usage guide](../04_usage_guide/index.md).
___
</details><br/>

<details>
    <summary><h3 style="display: inline; margin: 0">Q: How do I install LogiSpim on ARM-based Windows or Linux?</h3></summary>

Clone the LogiSpim repository and checkout a version of the repository.
This can be done similarly to how the following command checks out version `v0.1.0-beta`.
```shell
git checkout v0.1.0-beta
```

Then, ensure you have Maven installed (run `mvn --version` to see), and build the repository with the following
command from inside the root directory of the repository:
```shell
mvn clean build
```

Navigate to `FileProcessor/target/jar` and extract `FileProcessor-<version>-jar-with-dependencies.jar`.
Place this JAR file in the directory of your choosing and run it with the following command:
```shell
java -jar /path/to/jar.jar
```
___
</details><br/>

<details>
    <summary><h3 style="display: inline; margin: 0">Q: Does LogiSpim support non-Debian-based Linux distributions?</h3></summary>

Unfortunately, LogiSpim is not configured to support such distributions due to mixed cross-compilation toolchains.
Feel free to contribute to the project to implement support for other distributions!
___
</details><br/>


