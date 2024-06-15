![maven workflow](https://github.com/enofex/taikai/actions/workflows/maven.yml/badge.svg) [![](https://img.shields.io/badge/Java%20Version-21-orange)](/pom.xml)
<img height="20" src="https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg">
# Taikai

Taikai is an extension of the popular ArchUnit library, offering a comprehensive suite of predefined rules tailored for various technologies. It simplifies the process of enforcing architectural constraints and best practices in your codebase, ensuring consistency and quality across your projects.

Maven Usage
-------------------

```xml
<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai</artifactId>
  <version>${taikai.version}</version>
  <scope>test</scope>
</dependency>
```

The `${taikai.version}` property should be defined as a property in your Maven project to specify the version. The library requires that the necessary dependencies in this case ArchUnit is already declared.

JUnit 5 Example test
-------------------

```java
@Test
void shouldFulfilConstrains() {
  // Only some rule examples
  Taikai.builder()
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
          .noUsageOfDeprecatedAPIs()
          .methodsShouldNotThrowGenericException()
          .utilityClassesShouldBeFinalAndHavePrivateConstructor()
          .imports(imports -> imports
              .shouldHaveNoCycles()
              .shouldNotImport("..shaded..")
              .shouldNotImport("org.junit.."))
          .naming(naming -> naming
              .classesShouldNotMatch(".*Impl")
              .interfacesShouldNotHavePrefixI()))
      .addRule(TaikaiRule.of(...)) //add custom ArchUnit rule here
      .build()
      .check();
}
```

## User Guide

Please refer to the complete [documentation](https://github.com/enofex/taikai/blob/main/docs/USERGUIDE.md) for detailed information.

## Contributing

If you want to contribute to this project, then follow please
these [instructions](https://github.com/enofex/taikai/blob/main/CONTRIBUTING.md).
