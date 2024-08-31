# Taikai

## 1. Introduction

Taikai is an automated architecture testing tool for Java projects designed to maintain clean and consistent architecture. It enforces predefined and custom architectural constraints, ensuring code quality, maintainability, and adherence to best practices.

## 2. Getting Started

To use Taikai, include it as a dependency in your Maven `pom.xml`:

```xml
<dependency>
    <groupId>com.enofex</groupId>
    <artifactId>taikai</artifactId>
    <version>${taikai.version}</version>
    <scope>test</scope>
</dependency>
```

Ensure to configure `${taikai.version}` to the latest stable version compatible with your project's ArchUnit version.

## 3. Usage

### 3.1 Setting the Namespace

The `namespace` setting specifies the base package of your project. Taikai will analyze all classes within this namespace. The default mode is `WITHOUT_TESTS`, which excludes test classes from the import check.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .build()
    .check();
```

### 3.2 Setting the JavaClasses

You can configure `classes` as well. This allows you to specify specific [Java classes](https://www.archunit.org/userguide/html/000_Index.html#_importing_classes) to analyze. Note that setting both `namespace` and `classes` simultaneously is not supported and will result in an `IllegalArgumentException`.

```java
JavaClasses classes = new ClassFileImporter()
    .importClasses(ClassToCheck.class)

Taikai.builder()
    .classes(classes)
    .build()
    .check();
```

### 3.3 Enforcing Rules on Empty Sets

The `failOnEmpty` setting determines whether the build should fail if no classes match a given rule. This is useful to ensure that your rules are applied consistently and to avoid false positives. The default is `false`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .failOnEmpty(true)
    .build()
    .check();
```

### 3.4 Excluding Classes Globally

You can globally exclude specific classes from all rule checks by using the `excludeClass` or `excludeClasses` methods in the builder. This ensures that the specified classes are not checked by any rule.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .excludeClass("com.company.yourproject.SomeClassToExclude")
    .excludeClasses(Set.of("com.company.yourproject.foo.ClassToExclude", "com.company.yourproject.bar.ClassToExclude"))
    .build()
    .check();
```

### 3.5 Modifying an Existing Configuration
The `toBuilder` method allows you to create a new Builder instance from an existing Taikai configuration. This is useful if you need to modify an existing configuration.

```java
Taikai taikai = Taikai.builder()
    .namespace("com.company.yourproject")
    .excludeClass("com.company.yourproject.SomeClassToExclude")
    .failOnEmpty(true)
    .java(java -> java
        .fieldsShouldNotBePublic())
    .build();

// Modify the existing configuration
Taikai modifiedTaikai = taikai.toBuilder()
    .namespace("com.company.newproject")
    .excludeClass("com.company.yourproject.AnotherClassToExclude")
    .failOnEmpty(false)
    .java(java -> java
        .classesShouldImplementHashCodeAndEquals()
        .finalClassesShouldNotHaveProtectedMembers())
    .build();

// Perform the check with the modified configuration
modifiedTaikai.check();
```

## 4. Rules Overview

Taikai's architecture rules cover a wide range of categories to enforce best practices and maintain consistency.

### Java Rules

The default mode is `WITHOUT_TESTS`, which excludes test classes from the import check.

| Category | Method Name                                            | Rule Description                                                                                             |
|----------|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| General  | `classesShouldImplementHashCodeAndEquals`              | Classes should implement `hashCode` and `equals` together.                                                   |
| General  | `classesShouldResideInPackage`                         | Classes matching specific naming patterns should reside in a specified package.                              |
| General  | `classesAnnotatedWithShouldResideInPackage`            | Classes annotated with a specific annotation should reside in a specified package.                           |
| General  | `classesShouldResideOutsidePackage`                    | Classes matching specific naming patterns should reside outside a specified package.                         |
| General  | `classesShouldBeAnnotatedWith`                         | Classes matching specific naming patterns should be annotated with a specified annotation.                   |
| General  | `classesShouldNotBeAnnotatedWith`                      | Classes matching specific naming patterns should not be annotated with a specified annotation.               |
| General  | `classesShouldBeAssignableTo`                          | Classes matching specific naming patterns should be assignable to a certain type.                            |
| General  | `fieldsShouldNotBePublic`                              | Fields should not be `public`, except constants.                                                             |
| General  | `methodsShouldNotDeclareGenericExceptions`             | Methods should not declare generic exceptions, like `Exception` or `RuntimeException`.                       |
| General  | `methodsShouldNotDeclareException`                     | Methods with names matching a specified pattern should not declare a specified exception type.               |
| General  | `noUsageOf`                                            | Disallow usage of specific classes.                                                                          |
| General  | `noUsageOf`                                            | Disallow usage of specific classes by class reference.                                                       |
| General  | `noUsageOfDeprecatedAPIs`                              | No usage of deprecated APIs annotated with `@Deprecated`.                                                    |
| General  | `noUsageOfSystemOutOrErr`                              | Disallow usage of `System.out` or `System.err`.                                                              |
| General  | `utilityClassesShouldBeFinalAndHavePrivateConstructor` | Utility classes with only `static` methods (except `main`) should be `final` and have a private constructor. |
| General  | `finalClassesShouldNotHaveProtectedMembers`            | Classes declared as `final` should not contain any `protected` members.                                      |
| General  | `serialVersionUIDShouldBeStaticFinalLong`              | Fields named `serialVersionUID` should be declared as `static final long`.                                   |
| Imports  | `shouldHaveNoCycles`                                   | No cyclic dependencies in imports.                                                                           |
| Imports  | `shouldNotImport`                                      | Disallow specific imports (e.g., `..shaded..`).                                                              |
| Naming   | `packagesShouldMatch`                                  | Packages should match the specified regex pattern (e.g., `com.company.yourproject..`).                       |
| Naming   | `classesShouldNotMatch`                                | Classes should not match specific naming patterns (e.g., `.*Impl`).                                          |
| Naming   | `classesAnnotatedWithShouldMatch`                      | Classes annotated with a specific annotation should match specific naming patterns.                          |
| Naming   | `methodsShouldNotMatch`                                | Methods should not match specific naming patterns.                                                           |
| Naming   | `methodsAnnotatedWithShouldMatch`                      | Methods annotated with a specific annotation should match specific naming patterns.                          |
| Naming   | `fieldsShouldNotMatch`                                 | Fields should not match specific naming patterns.                                                            |
| Naming   | `fieldsShouldMatch`                                    | Fields should match specific naming patterns for specific classes.                                           |
| Naming   | `fieldsAnnotatedWithShouldMatch`                       | Fields annotated with a specific annotation should match specific naming patterns.                           |
| Naming   | `constantsShouldFollowConventions`                     | Constants should follow naming conventions, except `serialVersionUID`.                                       |
| Naming   | `interfacesShouldNotHavePrefixI`                       | Interfaces should not have the prefix `I`.                                                                   |

### Logging Rules

The default mode is `WITHOUT_TESTS`, which checks only test classes.

| Category | Method Name                       | Rule Description                                                                                  | 
|----------|-----------------------------------|---------------------------------------------------------------------------------------------------|
| General  | `loggersShouldFollowConventions`  | Ensure that specified loggers follow specific naming patterns and have the required modifiers.    |
| General  | `classesShouldUseLogger`          | Ensure that classes matching a given regex have a field of a specified logger type.               |

### Test Rules

The default mode is `ONLY_TESTS`, which checks only test classes.

| Category      | Method Name                                            | Rule Description                                                                                                      | 
|---------------|--------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| JUnit 5       | `classesShouldBePackagePrivate`                        | Ensure that classes whose names match a specific naming pattern are declared as package-private.                      |
| JUnit 5       | `classesShouldNotBeAnnotatedWithDisabled`              | Ensure classes are not annotated with `@Disabled`.                                                                    |
| JUnit 5       | `methodsShouldBePackagePrivate`                        | Ensure that test methods annotated with `@Test` or `@ParameterizedTest` are package-private.                          |
| JUnit 5       | `methodsShouldNotBeAnnotatedWithDisabled`              | Ensure methods are not annotated with `@Disabled`.                                                                    |
| JUnit 5       | `methodsShouldBeAnnotatedWithDisplayName`              | Ensure that test methods annotated with `@Test` or `@ParameterizedTest` are annotated with `@DisplayName`.            |
| JUnit 5       | `methodsShouldMatch`                                   | Ensure that test methods annotated with `@Test` or `@ParameterizedTest` have names matching a specific regex pattern. |
| JUnit 5       | `methodsShouldNotDeclareExceptions`                    | Ensure that test methods annotated with `@Test` or `@ParameterizedTest` do not declare any thrown exceptions.         |

### Spring Rules

The default mode is `WITHOUT_TESTS`, which excludes test classes from the import check.

| Category       | Method Name                                    | Rule Description                                                                                                                                       |
|----------------|------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------|
| General        | `noAutowiredFields`                            | Fields should not be annotated with `@Autowired`, prefer constructor injection.                                                                        |
| Boot           | `springBootApplicationShouldBeIn`              | Ensure `@SpringBootApplication` is in the default package.                                                                                             |
| Properties     | `namesShouldEndWithProperties`                 | Properties annotated with `@ConfigurationProperties` should end with `Properties`.                                                                     |
| Properties     | `namesShouldMatch`                             | Properties annotated with `@ConfigurationProperties` should match a regex pattern.                                                                     |
| Properties     | `shouldBeAnnotatedWithValidated`               | Properties annotated with `@ConfigurationProperties` should be annotated with `@Validated`.                                                            |
| Properties     | `shouldBeAnnotatedWithConfigurationProperties` | Properties ending with `Properties` should be annotated with `@ConfigurationProperties`.                                                               |
| Configurations | `namesShouldEndWithConfiguration`              | Configurations annotated with `@Configuration` should end with `Configuration`.                                                                        |
| Configurations | `namesShouldMatch`                             | Configurations annotated with `@Configuration` should match a regex pattern.                                                                           |
| Controllers    | `namesShouldEndWithController`                 | Controllers annotated with `@Controller` or `@RestController` should end with `Controller`.                                                            |
| Controllers    | `namesShouldMatch`                             | Controllers annotated with `@Controller` or `@RestController` should match a regex pattern.                                                            |
| Controllers    | `shouldBeAnnotatedWithController`              | Controllers ending with `Controller` should be annotated with `@Controller`.                                                                           |
| Controllers    | `shouldBeAnnotatedWithRestController`          | Controllers ending with `Controller` should be annotated with `@RestController`.                                                                       |
| Controllers    | `shouldBePackagePrivate`                       | Controllers annotated with `@Controller` or `@RestController` should be package-private.                                                               |
| Controllers    | `shouldNotDependOnOtherControllers`            | Controllers annotated with `@Controller` or `@RestController` should not depend on other controllers annotated with `@Controller` or `@RestController`. |
| Repositories   | `namesShouldEndWithRepository`                 | Repositories annotated with `@Repository` should end with `Repository`.                                                                                |
| Repositories   | `namesShouldMatch`                             | Repositories annotated with `@Repository` should match a regex pattern.                                                                                |
| Repositories   | `shouldBeAnnotatedWithRepository`              | Repositories ending with `Repository` should be annotated with `@Repository`.                                                                          |
| Repositories   | `shouldNotDependOnServices`                    | Repositories annotated with `@Repository` should not depend on service classes annotated with `@Service`.                                              |
| Services       | `namesShouldEndWithService`                    | Services annotated with `@Service` should end with `Service`.                                                                                          |
| Services       | `namesShouldMatch`                             | Services annotated with `@Service` should match a regex pattern.                                                                                       |
| Services       | `shouldBeAnnotatedWithService`                 | Services ending with `Service` should be annotated with `@Service`.                                                                                    |
| Services       | `shouldNotDependOnControllers`                 | Services annotated with `@Service` should not depend on controllers annotated with `@Controller` or `@RestController`.                                 |

## 5. Java Rules

Java configuration involves defining constraints related to Java language features, coding standards, and architectural patterns.

- **No Usage of Deprecated APIs**: Ensure that deprecated APIs annotated with `@Deprecated` not used in the codebase.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .noUsageOfDeprecatedAPIs())
    .build()
    .check();
```

- **Classes Should Implement `hashCode` and `equals` together**: Ensure that classes override the `hashCode` and `equals` methods together.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldImplementHashCodeAndEquals())
    .build()
    .check();
```

- **Classes Should Reside in Specified Package**: Ensure that classes matching a specific regex pattern reside in the specified package.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldResideInPackage(".*Utils", "com.company.yourproject.utils"))
    .build()
    .check();
```

- **Classes Annotated with Specified Annotation Should Reside in Specified Package**: Ensure that classes annotated with a specific annotation reside in the specified package.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesAnnotatedWithShouldResideInPackage(PublicApi.class, "com.company.yourproject.api"))
    .build()
    .check();
```

- **Classes Should Reside outside Specified Package**: Ensure that classes matching a specific regex pattern reside outside the specified package.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldResideOutsidePackage(".*Utils", "com.company.yourproject.utils"))
    .build()
    .check();
```

- **Classes Should Be Annotated with Specified Annotation**: Ensure that classes matching a specific regex pattern are annotated with the specified annotation.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldBeAnnotatedWith(".*Api", PublicApi.class))
        .classesShouldBeAnnotatedWith(".*Internal", "com.company.yourproject.Internal"))
    .build()
    .check();
```

- **Classes Should Not Be Annotated with Specified Annotation**: Ensure that classes matching a specific regex pattern are not annotated with the specified annotation.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldNotBeAnnotatedWith(".*Internal", PublicApi.class))
        .classesShouldNotBeAnnotatedWith(".*Internal", "com.company.yourproject.PublicApi"))
    .build()
    .check();
```

- **Classes Should Not Be Annotated with Specified Annotation**: Ensure that classes matching a specific regex pattern are not annotated with the specified annotation.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .classesShouldBeAssignableTo(".*Repository", SpecialCrudRepository.class))
        .classesShouldBeAssignableTo(".*Repository", "com.company.yourproject.SpecialCrudRepository"))
    .build()
    .check();
```


- **Methods Should Not Throw Generic Exception**: Ensure that methods do not throw generic exceptions like `Exception` and `RuntimeException` and use specific exception types instead.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .methodsShouldNotDeclareGenericExceptions())
    .build()
    .check();
```

- **Methods Should Not Declare Specific Exception**: Ensure that methods with names matching a specified pattern do not declare a specified exception type.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .methodsShouldNotDeclareException("should*", SpecificException.class)
        .methodsShouldNotDeclareException("should*", "com.company.yourproject.SpecificException"))
    .build()
    .check();
```

- **Utility Classes Should Be Final and Have Private Constructor**: Ensure that utility classes with only `static` methods except `main` should be declared as `final` and have `private` constructors to prevent instantiation.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .utilityClassesShouldBeFinalAndHavePrivateConstructor())
    .build()
    .check();
```

- **Fields Should Not Be Public**: Ensure that no fields in your Java classes are declared as `public`, except constants.

```java
Taikai.builder()
    .namespace("com.enofex.taikai")
    .java(java -> java
        .fieldsShouldNotBePublic())
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
            .shouldNotImport("..lombok..")
            .shouldNotImport(".*Service", "com.company.yourproject.SpecificException")))
    .build()
    .check();
```

- **Naming Configuration**: Define naming conventions for packages, classes, methods, fields, constants and interfaces.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .naming(naming -> naming
            .packagesShouldMatch("com.company.yourproject..")
            .classesShouldNotMatch(".*Impl")
            .classesAnnotatedWithShouldMatch(Annotation.class, "coolClass")   
            .methodsShouldNotMatch("coolMethod")
            .methodsAnnotatedWithShouldMatch(Annotation.class, "coolMethods")
            .fieldsShouldNotMatch("coolField")
            .fieldsShouldMatch("com.awesome.Foo", "foo")
            .fieldsShouldMatch(Foo.class, "foo")
            .fieldsAnnotatedWithShouldMatch(Annotation.class, "coolField")
            .constantsShouldFollowConventions()
            .interfacesShouldNotHavePrefixI())))
    .build()
    .check();
```

- **No Usage of Specific Classes**: Ensure that certain classes are not used in your codebase.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .noUsageOf("com.example.UnwantedClass")
        .noUsageOf("com.example.UnwantedClass", "in.specific.package"))
    .build()
    .check();
```

- **No Usage of Specific Classes by Class Reference**: Ensure that certain classes are not used in your codebase by directly referencing the class.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .noUsageOf(UnwantedClass.class)
        .noUsageOf(UnwantedClass.class, "in.specific.package"))   
    .build()
    .check();
```

- **No Usage of `System.out` or `System.err`**: Enforce disallowing the use of `System.out` and `System.err` for logging, encouraging the use of proper logging frameworks instead.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .noUsageOfSystemOutOrErr())
    .build()
    .check();
```

- **Ensure Final Classes Do Not Have Protected Members**: Ensures that classes declared as `final` do not contain any `protected` members. Since `final` classes cannot be subclassed, having `protected` members is unnecessary.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .finalClassesShouldNotHaveProtectedMembers())
    .build()
    .check();
```

## 6. Logging Rules

Logging configuration involves specifying constraints related to logging frameworks and practices.

- **Ensure Logger Field Conforms to Standards**: Ensure that classes use a logger field of the specified type, with the correct name and modifiers.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .logging(logging -> logging
        .loggersShouldFollowConventions(org.slf4j.Logger.class, "logger", EnumSet.of(PRIVATE, FINAL)))
    .build()
    .check();
```

- **Ensure Classes Use Specified Logger**: Ensure that classes matching a given regex have a field of a specified logger type.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .logging(logging -> logging
        .classesShouldUseLogger(org.slf4j.Logger.class, ".*Service")
    .build()
    .check();
```

## 7. Test Rules

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

- **Ensure Test Methods are Package-Private**: Ensure that JUnit 5 test methods annotated with `@Test` or `@ParameterizedTest` are package-private.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .methodsShouldBePackagePrivate()))
    .build()
    .check();
```

- **Ensure Test Methods are Annotated with `@DisplayName`**: Ensure that JUnit 5 test methods annotated with `@Test` or `@ParameterizedTest` are also annotated with `@DisplayName` to provide descriptive test names.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .methodsShouldBeAnnotatedWithDisplayName()))
    .build()
    .check();
```

- **Ensure Test Methods Follow Naming Convention**: Ensure that JUnit 5 test methods annotated with `@Test` or `@ParameterizedTest` have names matching a specific regex pattern.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .methodsShouldMatch("regex")))
    .build()
    .check();
```

- **Ensure Test Methods Do Not Declare Thrown Exceptions**: Ensure that JUnit 5 test methods annotated with `@Test` or `@ParameterizedTest` do not declare any thrown exceptions.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .methodsShouldNotDeclareExceptions()))
    .build()
    .check();
```

- **Ensure Classes with Matching Names are Package-Private**: Ensure that classes whose names match a specified regex pattern are declared as package-private.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .classesShouldBePackagePrivate(".*Test"))
    .build()
    .check();
```

- **Ensure `serialVersionUID` is `static final long`**: Ensure that fields named `serialVersionUID` are declared as `static final long`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .test(test -> test
        .junit5(junit5 -> junit5
            .serialVersionUIDShouldBeStaticFinalLong())
    .build()
    .check();
```

## 8. Spring Rules

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

- **Properties Configuration**: Ensure that configuration property classes end with `Properties` or match a specific regex pattern, are annotated with `@ConfigurationProperties` or annotated with `@Validated`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .properties(properties -> properties
            .shouldBeAnnotatedWithConfigurationProperties()
            .namesShouldEndWithProperties()
            .namesShouldMatch("regex")
            .shouldBeAnnotatedWithValidated()))
    .build()
    .check();
```

- **Configurations Configuration**: Ensure that configuration classes end with `Configuration` or match a specific regex pattern.

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

- **Controllers Configuration**: Ensure that controller classes end with `Controller` or match a specific regex pattern, are annotated with `@RestController`, do not depend on other controllers, and are package-private.

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

- **Services Configuration**: Ensure that service classes end with `Service` or match a specific regex pattern and are annotated with `@Service` and do not depend on other controllers.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .services(services -> services
            .shouldBeAnnotatedWithService()    
            .shouldNotDependOnControllers()
            .namesShouldMatch("regex")
            .namesShouldEndWithService()))
    .build()
    .check();
```

- **Repositories Configuration**: Ensure that repository classes end with `Repository` or match a specific regex pattern and are annotated with `@Repository` and not depend on classes annotated with `@Service`.

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .spring(spring -> spring
        .repositories(repositories -> repositories
            .shouldNotDependOnServices()
            .shouldBeAnnotatedWithRepository()
            .namesShouldMatch("regex")
            .namesShouldEndWithRepository()))
    .build()
    .check();
```

## 9. Customization

### Custom Configuration for Import Rules

For every rule, you have the flexibility to add a custom configuration. This allows you to specify the namespace, import options, and exclude specific classes from the checks.

The `Configuration` class offers various static methods to create custom configurations:
- `Configuration.of(String namespace)` to set a custom namespace.
- `Configuration.of(Namespace.IMPORT namespaceImport)` to specify import options such as `WITHOUT_TESTS`, `WITH_TESTS`, or `ONLY_TESTS`.
- `Configuration.of(String namespace, Namespace.IMPORT namespaceImport)` to set both namespace and import options.
- `Configuration.of(JavaClasses javaClasses)` to directly provide a set of [Java classes](https://www.archunit.org/userguide/html/000_Index.html#_importing_classes).
- `Configuration.of(Set<String> excludedClasses)` to exclude specific classes from the checks. 
- Additional overloaded methods to combine these options in various ways.

If a `namespaceImport` is not explicitly provided, it defaults to `Namespace.IMPORT.WITHOUT_TESTS`.

Here's an example of how you can use these methods to create a custom configuration:

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .java(java -> java
        .imports(imports -> imports
            .shouldNotImport("..shaded..", Configuration.of("com.company.yourproject.different", Namespace.IMPORT.WITHOUT_TESTS))
            .shouldNotImport("..lombok..", Configuration.of(Namespace.IMPORT.ONLY_TESTS))))
    .build()
    .check();
```

In this example, the import rule is configured to apply to classes within the specified namespace, excluding test classes.

### Adding Custom ArchUnit Rules

In addition to the predefined rules provided by Taikai, you can also add custom ArchUnit rules to tailor the architecture testing to your specific project requirements. Here's how you can integrate custom rules into your Taikai configuration:

```java
Taikai.builder()
    .namespace("com.company.yourproject")
    .addRule(TaikaiRule.of(...)) // Add custom ArchUnit rule here
    .build()
    .check();
```
By using the `addRule()` method and providing a custom ArchUnit rule, you can extend Taikai's capabilities to enforce additional architectural constraints that are not covered by the predefined rules. This flexibility allows you to adapt Taikai to suit the unique architectural needs of your Java project.

## 10. Examples

Below are some examples demonstrating the usage of Taikai to define and enforce architectural rules in Java projects, including Spring-specific configurations:

```java
class ArchitectureTest {

  @Test
  void shouldFulfilConstrains() {
    Taikai.builder()
        .namespace("com.company.yourproject")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .classesShouldImplementHashCodeAndEquals()
            .methodsShouldNotDeclareGenericExceptions()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor())
        .test(test -> test
            .junit5(junit5 -> junit5
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotBeAnnotatedWithDisabled()))        
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
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", EnumSet.of(PRIVATE, FINAL)))        
        .build()
        .check();
  }
}
```
