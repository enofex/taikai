![maven workflow](https://github.com/enofex/taikai/actions/workflows/maven.yml/badge.svg) [![](https://img.shields.io/badge/Java%20Version-21-orange)](/pom.xml)
<img height="20" src="https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg">
# Taikai

Taikai is a wrapper around the awesome ArchUnit and provides a set of common rules for different technologies.

Maven Usage
-------------------

```xml
<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai</artifactId>
  <version>${taikai.version}</version>
</dependency>
```

The `${taikai.version}` property should be defined as a property in your Maven project to specify the version. The library requires that the necessary dependencies are already declared.

JUnit 5 Example test
-------------------

```java
@Test
void shouldFulfilConstrains() {
  Taikai taikai = Taikai.builder()
      .namespace("com.enofex.taikai")
      .spring(spring -> spring
          .noAutowiredFields()
          .boot(boot -> boot
              .springBootApplicationShouldBeIn("com.enofex.taikai"))
          .configurations(configuration -> configuration
              .namesShouldEndWithConfiguration()
              .namesShouldMatch("regex"))
          .controllers(controllers -> controllers
              .shouldBeAnnotatedWithRestController()
              .namesShouldEndWithController()
              .namesShouldMatch("regex")
              .shouldNotDependOnOtherControllers()
              .shouldBePackagePrivate())
          .services(services -> services
              .shouldBeAnnotatedWithService()
              .namesShouldMatch("regex")
              .namesShouldEndWithService())
          .repositories(repositories -> repositories
              .shouldBeAnnotatedWithRepository()
              .namesShouldMatch("regex")
              .namesShouldEndWithRepository()))
      .test(test -> test
          .junit5(junit5 -> junit5
              .classesShouldNotBeAnnotatedWithDisabled()
              .methodsShouldNotBeAnnotatedWithDisabled()))
      .java(java -> java
          .imports(imports -> imports
              .shouldHaveNoCycles()
              .shouldNotImport("..shaded..")
              .shouldNotImport("org.junit.."))
          .naming(naming -> naming
              .classesShouldNotMatch(".*Impl")
              .interfacesShouldNotHavePrefixI()))
      .build();
}
```

## User Guide

Please refer to the complete [documentation](https://github.com/enofex/taikai/blob/main/docs/USERGUIDE.md) for detailed information.

## Contributing

If you want to contribute to this project, then follow please
these [instructions](https://github.com/enofex/taikai/blob/main/CONTRIBUTING.md).
