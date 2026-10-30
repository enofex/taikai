<p align="center">
    <img src="docs/assets/images/taikai-logo-dark.png"
        height="150">
</p>

<p align="center">
    <img src="https://github.com/enofex/taikai/actions/workflows/maven.yml/badge.svg" />
    <img src="https://img.shields.io/badge/Java%20Version-17-orange" />
    <img height="20" src="https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg">
</p>

# Taikai

Taikai extends the capabilities of the popular ArchUnit library by offering a comprehensive suite of predefined rules tailored for various technologies. It simplifies the enforcement of architectural constraints and best practices in your codebase, ensuring consistency and quality across your projects.

## Maven Usage

Add Taikai as a dependency in your `pom.xml`:

```xml
<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai</artifactId>
  <version>${taikai.version}</version>
  <scope>test</scope>
</dependency>
```

Replace `${taikai.version}` with the appropriate version defined in your project. ArchUnit comes along transitively, so you do not need to declare it yourself.

## Gradle Usage

Add Taikai as a dependency in your `build.gradle` file:

```groovy
testImplementation "com.enofex:taikai:${taikaiVersion}"
```

Replace `${taikaiVersion}` with the appropriate version defined in your project. ArchUnit comes along transitively, so you do not need to declare it yourself.

## Kotlin/JVM

Use `com.enofex:taikai-kotlin` for Kotlin-aware architecture rules and a receiver DSL.
The original Java API also works from Kotlin tests. See the [Kotlin manual](docs/kotlin.md)
and the runnable [Spring Kotlin demo](examples/spring-kotlin).

```kotlin
import com.enofex.taikai.kotlin.functionsShouldNotExceedMaxParameters
import com.enofex.taikai.kotlin.taikai

taikai {
    namespace("com.company.project")
    spring { services { it.namesShouldEndWithService() } }
    kotlin {
        classesShouldBeDataClasses(".*Response")
        dataClassesShouldBeImmutable()
    }
    metrics { functionsShouldNotExceedMaxParameters(4) }
}.checkAll()
```

## JUnit Example Test

Here's an example demonstrating the usage of some Taikai rules with JUnit. Customize rules as needed using `TaikaiRule.of()`.
The runnable [Spring Java demo](examples/spring-java) shows a complete application with its architecture tests.

```java
@Test
void architecture() {
  Taikai.builder()
      .namespace("com.company.project")
      .java(java -> java
          .noUsageOfDeprecatedAPIs()
          .methodsShouldNotDeclareGenericExceptions()
          .utilityClassesShouldBeFinalAndHavePrivateConstructor()
          .imports(imports -> imports
              .shouldHaveNoCycles()
              .shouldNotImport(ImportPatterns.shaded()))
          .naming(naming -> naming
              .classesShouldNotMatch(".*Impl")
              .methodsShouldNotMatch("^(foo$|bar$).*")
              .fieldsShouldNotMatch(".*(List|Set|Map)$")
              .fieldsShouldMatch(FieldCriteria.ofType("com.company.project.Matcher"), "matcher")
              .constantsShouldFollowConventions()
              .enumConstantsShouldFollowConventions()
              .interfacesShouldNotHavePrefixI()))
      .logging(logging -> logging
          .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL)))
      .test(test -> test
          .junit(junit -> junit
              .classesShouldNotBeAnnotatedWithDisabled()
              .methodsShouldNotBeAnnotatedWithDisabled()))
      .jpa(jpa -> jpa
          .entities(entities -> entities
              .shouldBePackagePrivate()
              .idsShouldNotBeOfType(ClassCriteria.type(UUID.class))
              .shouldHaveFieldAnnotatedWithVersion()
              .fieldsShouldNotBeOfType(
                  ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class))
                      .or(ClassCriteria.type(Calendar.class))
                      .or(ClassCriteria.assignableTo(Calendar.class)))
              .namesShouldMatch(".+Entity")))    
      .metrics(metrics -> metrics
          .classesShouldNotExceedMaxInheritanceDepth(3)
          .classesShouldNotExceedMaxDependencies(20))
      .spring(spring -> spring
          .noAutowiredFields()
          .noSelfInvocationOfProxiedMethods()
          .boot(boot -> boot
              .applicationClassShouldResideInPackage("com.company.project"))
          .configurations(configuration -> configuration
              .namesShouldEndWithConfiguration())
          .controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController()
              .namesShouldEndWithController()
              .shouldNotDependOnOtherControllers()
              .shouldNotDependOnRepositories()
              .shouldBePackagePrivate())
          .services(services -> services
              .shouldBeAnnotatedWithService()
              .shouldNotDependOnControllers()
              .namesShouldEndWithService())
          .repositories(repositories -> repositories
              .shouldBeAnnotatedWithRepository()
              .shouldNotDependOnServices()
              .namesShouldEndWithRepository())
          .transactional(transactional -> transactional
              .methodsShouldBePublic()
              .shouldNotBeSelfInvoked()
              .shouldNotBeUsedInControllers()))
      .addRule(TaikaiRule.of(...)) // Add custom ArchUnit rule here
      .build()
      .checkAll();
}
```

## One Test per Rule

Return `dynamicTests()` from a JUnit `@TestFactory` to see every rule as its own test in the IDE and
in test reports:

```java
@DisplayName("Check architecture constraints")
@TestFactory
Stream<DynamicTest> architecture() {
  return Taikai.builder()
      .namespace("com.company.project")
      .java(java -> java.noUsageOfSystemOutOrErr())
      .build()
      .dynamicTests();
}
```

## Custom Checks

A `TaikaiCheck` checks what ArchUnit cannot see, such as files in the project directory, resources
or the classpath. Ship checks in a library, or use predefined ones such as
`com.enofex.taikai.checks.RequiredFilesCheck`, register them in
`META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck`, and discover them:

```java
Taikai.builder()
    .namespace("com.company.project")
    .discoverChecks(checks -> checks
        .configure(RequiredFilesCheck.class, RequiredFilesCheck.files(
            "README.md", "LICENSE", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "SECURITY.md")))
    .build()
    .checkAll();
```

## Custom Exports

A `TaikaiExport` exports the configuration, for example into a file. Exports are registered like
checks, in `META-INF/services/com.enofex.taikai.exports.spi.TaikaiExport`, and run with
`export()`. The predefined `com.enofex.taikai.exports.AgentsMarkdownExport` writes the rules as
Markdown for coding agents, to reference from `AGENTS.md` or `CLAUDE.md`:

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(java -> java.noUsageOfSystemOutOrErr())
    .discoverExports(exports -> exports
        .configure(AgentsMarkdownExport.class, AgentsMarkdownExport.file("ARCHITECTURE.md")))
    .build()
    .export();
```

```markdown
# Architecture Rules

The architecture test enforces these rules for `com.company.project`, so code that breaks one of them fails the build.

- Classes should not use System.out or System.err
```

The predefined `com.enofex.taikai.exports.SarifExport` writes the rule violations as a SARIF
report, for example with `SarifExport.file("target/taikai.sarif")`, with the file and line of each
violation.

## User Guide

Explore the complete [documentation](https://enofex.github.io/taikai) for comprehensive information on all available rules.

## Contributing

Interested in contributing? Check out our [Contribution Guidelines](https://github.com/enofex/taikai/blob/main/CONTRIBUTING.md) for details on how to get involved. Note, that we expect everyone to follow the [Code of Conduct](https://github.com/enofex/taikai/blob/main/CODE_OF_CONDUCT.md).

### What you will need

* Git
* Java 17 or higher

### Get the Source Code

Clone the repository

```shell
git clone git@github.com:enofex/taikai.git
cd taikai
```

### Build the code

To compile, test, and build

```shell
./mvnw clean package -B
```

## Backers

The Open Source Community

<br>
<a href="https://github.com/enofex/taikai/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=enofex/taikai" />
</a>
<br>

## Website

Visit the [Taikai](https://enofex.github.io/taikai/) Website for general information and documentation.

## Sponsor

<a href="https://github.com/sponsors/mnhock">
  <img src="https://img.shields.io/badge/Sponsor%20Taikai%20on%20GitHub-4051b5?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="Sponsor Taikai on GitHub" />
</a>
