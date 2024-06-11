## Taikai

### 1. Introduction
Taikai is an automated architecture testing tool for Java projects designed to maintain clean and consistent architecture. It enforces predefined and custom architectural constraints, ensuring code quality, maintainability, and adherence to best practices.

### 2. Getting Started
To use Taikai, include it as a dependency in your Maven pom.xml:

```javaxml
<dependency>
    <groupId>com.enofex</groupId>
    <artifactId>taikai</artifactId>
    <version>${taikai.version}</version>
    <scope>test</scope>
</dependency>
```

Architecture rules are defined using Taikai's fluent API, allowing developers to specify constraints on classes, methods, imports, naming conventions, and more. Taikai provides pre-defined configurations for common architectural patterns and best practices.

### 3. Usage

#### Test Configuration

Test configuration involves specifying constraints related to testing frameworks and practices.

- **JUnit 5 Configuration**: Ensure that JUnit 5 test classes and methods are not annotated with `@Disabled`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .classesShouldNotBeAnnotatedWithDisabled()
            .methodsShouldNotBeAnnotatedWithDisabled()))
    .build()
    .check();
```

#### Java Configuration

Java configuration involves defining constraints related to Java language features, coding standards, and architectural patterns.

- **No Usage of Deprecated APIs**: Ensure that deprecated APIs annotated with `Deprecated` not used in the codebase.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .noUsageOfDeprecatedAPIs())
    .build()
    .check();
```

- **Classes Should Implement `hashCode` and `equals`**: Ensure that classes override the `hashCode` and `equals` methods.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldImplementHashCodeAndEquals())
    .build()
    .check();
```

- **Methods Should Not Throw Generic Exception**: Ensure that methods do not throw generic exceptions like `Exception` and `RuntimeException` and use specific exception types instead.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .methodsShouldNotThrowGenericException())
    .build()
    .check();
```

- **Utility Classes Should Be Final and Have Private Constructor**: Ensure that utility classes are declared as `final` and have private constructors to prevent instantiation.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .utilityClassesShouldBeFinalAndHavePrivateConstructor())
    .build()
    .check();
```

- **Imports Configuration**: Ensure that there are no cyclic dependencies in imports and disallow specific imports.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .imports(imports -> imports
            .shouldHaveNoCycles()
            .shouldNotImport("..shaded..")
            .shouldNotImport("..lombok..")))
    .build()
    .check();
```

- **Naming Configuration**: Define naming conventions for classes, constants, and interfaces.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .naming(naming -> naming
            .classesShouldNotMatch(".*Impl")
            .constantsShouldFollowConvention()
            .interfacesShouldNotHavePrefixI())))
    .build()
    .check();
```

#### Spring Configuration

Spring configuration involves defining constraints specific to Spring Framework usage.

- **No Autowired Fields Configuration**: Ensure that fields are not annotated with `@Autowired` and constructor injection is preferred.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .noAutowiredFields())
    .build()
    .check();
```

- **Spring Boot Configuration**: Ensure that the main application class annotated with `@SpringBootApplication` is located in the default package.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .boot(boot -> boot
            .springBootApplicationShouldBeIn("com.company.yourproject")))
    .build()
    .check();
```

- **Configurations Configuration**: Ensure that configuration classes end with "Configuration" or match a specific regex pattern.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .configurations(configuration -> configuration
            .namesShouldEndWithConfiguration()
            .namesShouldMatch("regex")))
    .build()
    .check();
```

- **Controllers Configuration**: Ensure that controller classes end with "Controller" or match a specific regex pattern, are annotated with `@RestController`, do not depend on other controllers, and are package-private.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .controllers(controllers -> controllers
            .shouldBeAnnotatedWithRestController()
            .namesShouldEndWithController()
            .namesShouldMatch("regex")
            .shouldNotDependOnOtherControllers()
            .shouldBePackagePrivate()))
    .build()
    .check();
```

- **Services Configuration**: Ensure that service classes end with "Service" or match a specific regex pattern and are annotated with `@Service`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .services(services -> services
            .shouldBeAnnotatedWithService()
            .namesShouldMatch("regex")
            .namesShouldEndWithService()))
    .build()
    .check();
```

- **Repositories Configuration**: Ensure that repository classes end with "Repository" or match a specific regex pattern and are annotated with `@Repository`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .repositories(repositories -> repositories
            .shouldBeAnnotatedWithRepository()
            .namesShouldMatch("regex")
            .namesShouldEndWithRepository()))
    .build()
    .check();
```

#### Adding Custom ArchUnit Rules

In addition to the predefined rules provided by Taikai, you can also add custom ArchUnit rules to tailor the architecture testing to your specific project requirements. Here's how you can integrate custom rules into your Taikai configuration:

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .addRule(TaikaiRule.of(...)) // Add custom ArchUnit rule here
    .build()
    .check();
```
By using the `addRule()` method and providing a custom ArchUnit rule, you can extend Taikai's capabilities to enforce additional architectural constraints that are not covered by the predefined rules. This flexibility allows you to adapt Taikai to suit the unique architectural needs of your Java project.

#### Examples

Below are some examples demonstrating the usage of Taikai to define and enforce architectural rules in Java projects, including Spring-specific configurations:

```java
class ArchitectureTest {

  @Test
  void shouldFulfilConstrains() {
    Taikai.builder()
        .namespace("com.company.yourproject")
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled()))
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotThrowGenericException()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor())
        .spring(spring -> spring
            .repositories(repositories -> repositories
                .namesShouldEndWithRepository()
                .shouldBeAnnotatedWithRepository())
            .services(services -> services
                .namesShouldEndWithService()
                .shouldBeAnnotatedWithService())
            .boot(boot -> boot
                .shouldBeAnnotatedWithSpringBootApplication())
            .noAutowiredFields()
            .configurations(configuration -> configuration
                .namesShouldEndWithConfiguration()
                .namesShouldMatch("regex"))
            .controllers(controllers -> controllers
                .shouldBeAnnotatedWithRestController()
                .namesShouldEndWithController()
                .namesShouldMatch("regex")
                .shouldNotDependOnOtherControllers()
                .shouldBePackagePrivate()))
        .build()
        .check();
  }
}
```