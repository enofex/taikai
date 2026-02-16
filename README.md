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

Replace `${taikai.version}` with the appropriate version defined in your project. Ensure that the required dependencies like ArchUnit are already declared.

## Gradle Usage

Add Taikai as a dependency in your `build.gradle` file:

```groovy
testImplementation "com.enofex:taikai:${taikaiVersion}"
```

Replace `${taikaiVersion}` with the appropriate version defined in your project. Ensure that the required dependencies like ArchUnit are already declared.

## JUnit Example Test

Here's an example demonstrating the usage of some Taikai rules with JUnit. Customize rules as needed using `TaikaiRule.of()`.

```java
@Test
void shouldFulfillConstraints() {
  Taikai.builder()
      .namespace("com.enofex.taikai")
      .java(java -> java
          .noUsageOfDeprecatedAPIs()
          .methodsShouldNotDeclareGenericExceptions()
          .utilityClassesShouldBeFinalAndHavePrivateConstructor()
          .imports(imports -> imports
              .shouldHaveNoCycles()
              .shouldNotImport("..internal.."))
          .naming(naming -> naming
              .classesShouldNotMatch(".*Impl")
              .methodsShouldNotMatch("^(foo$|bar$).*")
              .fieldsShouldNotMatch(".*(List|Set|Map)$")
              .fieldsShouldMatch("com.enofex.taikai.Matcher", "matcher")
              .constantsShouldFollowConventions()
              .interfacesShouldNotHavePrefixI()))
      .logging(logging -> logging
          .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL)))      
      .test(test -> test
          .junit(junit -> junit
              .classesShouldNotBeAnnotatedWithDisabled()
              .methodsShouldNotBeAnnotatedWithDisabled()))
      .spring(spring -> spring
          .noAutowiredFields()
          .boot(boot -> boot
              .applicationClassShouldResideInPackage("com.enofex.taikai"))
          .configurations(configuration -> configuration
              .namesShouldEndWithConfiguration())
          .controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController()
              .namesShouldEndWithController()
              .shouldNotDependOnOtherControllers()
              .shouldBePackagePrivate())
          .services(services -> services
              .shouldBeAnnotatedWithService()
              .shouldNotDependOnControllers()
              .namesShouldEndWithService())
          .repositories(repositories -> repositories
              .shouldBeAnnotatedWithRepository()
              .shouldNotDependOnServices()
              .namesShouldEndWithRepository()))      
      .addRule(TaikaiRule.of(...)) // Add custom ArchUnit rule here
      .build()
      .checkAll();
}
```

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
