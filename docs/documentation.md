# Taikai

## 1. Introduction

Taikai is an automated architecture testing tool for Java projects. It builds on
[ArchUnit](https://www.archunit.org) and adds a large set of ready-made, fluently configurable
architectural rules, so that enforcing conventions does not require writing ArchUnit predicates and
conditions by hand.

A Taikai check is an ordinary unit test. It imports your compiled classes, evaluates every
configured rule against them, and fails the build when a rule is violated.

```java
class ArchitectureTest {

  @Test
  void architecture() {
    Taikai.builder()
        .namespace("com.company.project")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .fieldsShouldNotBePublic())
        .build()
        .checkAll();
  }
}
```

## 2. Requirements

| Requirement | Version      |
|-------------|--------------|
| Java        | 17 or higher |
| ArchUnit    | 1.5.1        |

ArchUnit is a regular (compile-scope) dependency of Taikai, so it arrives transitively and you do
not need to declare it yourself. Taikai only relies on the stable ArchUnit core API, so a newer
patch or minor ArchUnit release normally works unchanged. If you pin a different ArchUnit version
in your own build, verify it after upgrading Taikai.

## 3. Installation

Taikai belongs on the test classpath only.

### 3.1 Maven

```xml
<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai</artifactId>
  <version>${taikai.version}</version>
  <scope>test</scope>
</dependency>
```

### 3.2 Gradle

```groovy
testImplementation "com.enofex:taikai:${taikaiVersion}"
```

ArchUnit comes along transitively; no separate declaration is required.

## 4. Quick Start

The example below is a complete, runnable test. Every other code sample in this document is a
*fragment* that plugs into this same skeleton (see [Section 6](#6-how-to-read-the-rule-reference)).

```java
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.PRIVATE;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.java.ImportPatterns;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class ArchitectureTest {

  @Test
  void architecture() {
    Taikai.builder()
        .namespace("com.company.project")
        .failOnEmpty(true)
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic()
            .methodsShouldNotDeclareGenericExceptions()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport(ImportPatterns.shaded()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .classesShouldNotMatch(".*Impl")
                .constantsShouldFollowConventions()
                .interfacesShouldNotHavePrefixI()))
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL)))
        .test(test -> test
            .junit(junit -> junit
                .classesShouldEndWithTest()
                .methodsShouldBePackagePrivate()
                .methodsShouldContainAssertionsOrVerifications()))
        .build()
        .checkAll();
  }
}
```

## 5. Core Concepts

### 5.1 Selecting Classes with a Namespace

`namespace` is the base package Taikai imports and analyzes.

```java
Taikai.builder()
    .namespace("com.company.project")
    .build()
    .check();
```

If the namespace contains no classes to check, for example because it is misspelled, the rules
check nothing and pass. Taikai does not fail in that case, but logs a warning through
`System.Logger` before the rules run:

```text
WARNING: Namespace com.compnay.project contains no production classes, so the rules for them check nothing. Is the namespace correct?
```

### 5.2 Selecting Classes Directly

Instead of a namespace you can hand Taikai an explicit set of
[JavaClasses](https://www.archunit.org/userguide/html/000_Index.html#_importing_classes), or a list
of class literals.

```java
JavaClasses classes = new ClassFileImporter()
    .importClasses(ClassToCheck.class);

Taikai.builder()
    .classes(classes)
    .build()
    .check();
```

```java
Taikai.builder()
    .classes(ClassToCheck.class, AnotherClassToCheck.class)
    .build()
    .check();
```

!!! warning
    Setting both `namespace` and `classes` is not supported and throws an
    `IllegalArgumentException` at `build()` time. Choose one.

### 5.3 Namespace Import Modes

`Namespace.IMPORT` controls whether test classes take part in a check.

| Mode             | Imports                              |
|------------------|--------------------------------------|
| `WITHOUT_TESTS`  | production classes only              |
| `WITH_TESTS`     | production and test classes          |
| `ONLY_TESTS`     | test classes only                    |

Each rule group has a default mode, so in practice you rarely set this yourself:

| Rule group                                                                | Default mode    |
|---------------------------------------------------------------------------|-----------------|
| `java(...)`, `logging(...)`, `spring(...)`, `quarkus(...)`, `jpa(...)`     | `WITHOUT_TESTS` |
| `test(...)` / `junit(...)`                                                 | `ONLY_TESTS`    |

Override it per rule through [`Configuration`](#58-per-rule-configuration):

```java
.java(java -> java
    .fieldsShouldNotBePublic(Configuration.of(Namespace.IMPORT.WITH_TESTS)))
```

### 5.4 Excluding Classes Globally

`excludeClasses(...)` removes classes from *every* rule. It accepts class literals or string
patterns.

Supported string patterns:

- **Fully qualified class name** — `com.company.project.foo.ClassToExclude`
- **Package wildcard (`*`)** — classes directly in that package: `com.company.project.foo.*`
- **Recursive package wildcard (`..`)** — the package and all subpackages: `com.company.project.internal..`

```java
Taikai.builder()
    .namespace("com.company.project")
    .excludeClasses(
        "com.company.project.foo.ClassToExclude",
        "com.company.project.bar.*",
        "com.company.project.internal..")
    .excludeClasses(GeneratedMapper.class, LegacyFacade.class)
    .build()
    .check();
```

`excludeClasses(...)` also accepts [criteria](#513-selecting-with-criteria), which can
exclude classes by annotation or any other property instead of by name. A class matching any of the
registered criteria is excluded. This is the typical way to skip generated code everywhere:

```java
Taikai.builder()
    .namespace("com.company.project")
    .excludeClasses(ClassCriteria.inPackage("com.company.project.generated.."))
    .excludeClasses(ClassCriteria.annotatedWith(Legacy.class))
    .build()
    .check();
```

!!! warning
    Class criteria only see annotations that are kept in the class files. `jakarta.annotation.Generated`
    and `javax.annotation.processing.Generated`, which code generators commonly add, have `SOURCE`
    retention, so `ClassCriteria.annotatedWith(...)` never matches them. Exclude generated code by
    package, or by an annotation with `CLASS` or `RUNTIME` retention.

An exclusion that matches no class has no effect, typically because the class was renamed or
deleted. Taikai logs a warning for such an exclusion, whether it is global or belongs to a single
rule, so stale exclusions can be removed:

```text
WARNING: Exclusion 'com.company.project.foo.ClassToExclude' matches no class, so it can be removed
```

To exclude classes from a single rule only, use [`Configuration`](#58-per-rule-configuration) instead.

!!! warning
    Exclusions, whether by name or by criteria, are only applied on the namespace path, so they take
    effect only when the classes come from `namespace(...)`. If you supply the classes yourself — globally via
    [`classes(...)`](#52-selecting-classes-directly) or per rule via
    `Configuration.of(JavaClasses)` — `excludeClasses(...)` is silently ignored. Filter the
    `JavaClasses` before handing them over, or pass the exclusions together with a rule's classes
    in `Configuration.of(JavaClasses, Collection<T>)`.

### 5.5 Failing on Empty Rule Results

By default an ArchUnit rule that matches no classes passes silently. That hides typos in regexes and
rules that quietly stopped applying after a refactoring. `failOnEmpty(true)` turns those into
failures.

```java
Taikai.builder()
    .namespace("com.company.project")
    .failOnEmpty(true)
    .build()
    .check();
```

The default is `false`. Turning it on is recommended for any long-lived configuration.

### 5.6 Running the Checks

All three methods evaluate every configured rule and [custom check](#514-custom-checks); they
differ in how they report.

| Method           | Behavior                                                                                    |
|------------------|---------------------------------------------------------------------------------------------|
| `check()`        | Stops at the first violated rule or check and throws immediately.                           |
| `checkAll()`     | Evaluates all rules and checks, aggregates every violation, then throws one combined report. |
| `dynamicTests()` | Returns one JUnit dynamic test per rule and check, each passing or failing on its own.      |

```java
Taikai.builder()
    .namespace("com.company.project")
    .build()
    .check();      // fail fast
```

```java
Taikai.builder()
    .namespace("com.company.project")
    .build()
    .checkAll();   // full report
```

Prefer `checkAll()` when introducing Taikai to an existing codebase, so you see the whole backlog in
one run. `check()` gives a shorter, more focused failure once the codebase is clean.

`dynamicTests()` turns every rule into its own JUnit Jupiter test, named after the rule's
description. The IDE and the test reports then show which rules pass and which fail, and a single
rule can be rerun from the IDE. Return the stream from a `@TestFactory` method:

```java
@DisplayName("Check architecture constraints")
@TestFactory
Stream<DynamicTest> architecture() {
  return Taikai.builder()
      .namespace("com.company.project")
      .java(java -> java
          .noUsageOfSystemOutOrErr()
          .fieldsShouldNotBePublic())
      .build()
      .dynamicTests();
}
```

```text
Check architecture constraints
├─ ✔ Classes should not use System.out or System.err
└─ ✘ Fields should not be public unless they are static
```

### 5.7 Reading the Failure Report

`check()` propagates ArchUnit's own `AssertionError` for the first failing rule.

`checkAll()` produces an aggregated report with a violation count, a rule count, and the individual
violations grouped per rule. Violated [custom checks](#514-custom-checks) are counted separately,
as in `Found 3 Taikai violations for 1 rules and 1 checks!`, and listed with `Check:` instead of
`Rule:`:

```text
java.lang.AssertionError: Found 2 Taikai violations for 2 rules!

Rule: Classes should not use System.out or System.err
	Method com.company.project.OrderService.process() calls java.lang.System.out

Rule: Fields should not be public unless they are static
	Field total in class com.company.project.Order is public
```

The rule line is the rule's own description, which is also what you grep for when you want to find
the configuration that produced a violation.

Detail lines that come from stock ArchUnit conditions end with the source file and line number, so
they are one click away in an IDE:

```text
Rule: Classes should not have names matching .*Service
	Class <com.company.project.OrderService> has name matching '.*Service' in (OrderService.java:0)
```

Rules built on Taikai's own conditions — among them `noUsageOfSystemOutOrErr`,
`fieldsShouldNotBePublic`, `classesShouldImplementHashCodeAndEquals` and the logger rules — name the
offending element without a location, as in the report above.

### 5.8 Per-Rule Configuration

Every rule method has an overload taking a `TaikaiRule.Configuration`. It overrides the global
settings for that one rule: a different namespace, a different import mode, an explicit class set, or
additional excluded classes.

| Factory                                                                | Purpose                                      |
|------------------------------------------------------------------------|----------------------------------------------|
| `Configuration.defaultConfiguration()`                                 | Global namespace, `WITHOUT_TESTS`            |
| `Configuration.of(String namespace)`                                   | Different namespace                          |
| `Configuration.of(Namespace.IMPORT namespaceImport)`                   | Different import mode                        |
| `Configuration.of(String namespace, Namespace.IMPORT namespaceImport)` | Both                                         |
| `Configuration.of(JavaClasses javaClasses)`                            | An explicit class set                        |
| `Configuration.of(Collection<T> excludedClasses)`                      | Exclusions (`String` patterns or `Class<?>`) |
| `Configuration.of(String, Collection<T>)`                              | Namespace plus exclusions                    |
| `Configuration.of(Namespace.IMPORT, Collection<String>)`               | Import mode plus exclusions                  |
| `Configuration.of(String, Namespace.IMPORT, Collection<T>)`            | Namespace, import mode and exclusions        |
| `Configuration.of(JavaClasses, Collection<T>)`                         | Class set plus exclusions                    |

`Collection<T>` accepts either `String` patterns or `Class<?>` literals, but not both in the same
collection — the type is decided from the first element. The `Namespace.IMPORT` plus exclusions
overload is the one exception: it takes `Collection<String>` only, so pass class *names* there
rather than class literals.

!!! warning
    A `Configuration` that does not name an import mode always means `WITHOUT_TESTS`. It does *not*
    inherit the rule group's default. This matters for the JUnit rules, which are the only ones
    defaulting to `ONLY_TESTS`: attaching a `Configuration` for some unrelated reason silently
    switches them to production classes, where they match nothing and pass.

    ```java
    // ONLY_TESTS - checks every test method
    .test(test -> test
        .junit(junit -> junit
            .methodsShouldMatch("should.*")))

    // WITHOUT_TESTS - silently checks nothing
    .test(test -> test
        .junit(junit -> junit
            .methodsShouldMatch("should.*", Configuration.of(List.of("com.company.project.Legacy")))))

    // ONLY_TESTS restored explicitly
    .test(test -> test
        .junit(junit -> junit
            .methodsShouldMatch("should.*",
                Configuration.of(Namespace.IMPORT.ONLY_TESTS, List.of("com.company.project.Legacy")))))
    ```

    Pairing this with [`failOnEmpty(true)`](#55-failing-on-empty-rule-results) turns such a silent
    pass into a failure.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldNotImport(ClassCriteria.inPackage("com.company.project.legacy.."),
            Configuration.of("com.company.project.different", Namespace.IMPORT.WITHOUT_TESTS))
        .shouldNotImport(lombok(),
            Configuration.of(Namespace.IMPORT.ONLY_TESTS))))
```

```java
.spring(spring -> spring
    .noSelfInvocationOfProxiedMethods(
        Configuration.of(List.of(OrderService.class))))
```

### 5.9 Disabling a Rule Group

Every configurer implements `disable()`, which clears all rules registered on that configurer. This
is mainly useful when a shared [profile](#511-reusable-rule-profiles) enables a group that a
particular module must not run.

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(DEFAULT_JAVA_PROFILE)
    .spring(spring -> spring
        .controllers(controllers -> controllers
            .disable()))         // drop every controller rule the profile added
    .build()
    .checkAll();
```

Every rule group has `disable()`: `java`, `imports`, `naming`, `logging`, `test`, `junit`, `spring`,
`boot`, `properties`, `configurations`, `controllers`, `services`, `repositories`, `transactional`,
`quarkus`, `resources`, `panache`, `ai`, `jpa` and `entities`.

`disable()` cascades to nested configurers: `java(...)` also clears `naming` and `imports`,
`spring(...)` clears all its sub-configurers, `quarkus(...)` clears `resources`, `panache` and `ai`,
`jpa(...)` clears `entities`, and `test(...)` clears `junit`.

### 5.10 Modifying an Existing Configuration

`toBuilder()` returns a builder pre-populated from an existing `Taikai` instance, so a shared base
configuration can be adapted per module.

```java
Taikai base = Taikai.builder()
    .namespace("com.company.project")
    .excludeClasses("com.company.project.SomeClassToExclude")
    .failOnEmpty(true)
    .java(java -> java
        .fieldsShouldNotBePublic())
    .build();

Taikai adapted = base.toBuilder()
    .namespace("com.company.newproject")
    .excludeClasses("com.company.project.AnotherClassToExclude")
    .java(java -> java
        .classesShouldImplementHashCodeAndEquals()
        .finalClassesShouldNotHaveProtectedMembers())
    .build();

adapted.check();
```

`toBuilder()` copies the rules of the instance. They stay in the new configuration even if you call
`disable()` on the new builder, which only removes the rules added through that builder. To drop a
rule group per module, share a [profile](#511-reusable-rule-profiles) instead of a built instance.

!!! warning
    Two rules read the namespace when their rule group is first configured, rather than when they
    run: [`imports.shouldHaveNoCycles()`](#imports-shouldHaveNoCycles) and
    [`boot.applicationClassShouldResideInPackage()`](#spring-boot-applicationClassShouldResideInPackage).
    They throw a `TaikaiException` if no namespace is set yet, and they keep that namespace if it
    changes later, also through `toBuilder().namespace(...)`. Call `namespace(...)` before
    configuring them, or give them a namespace with `Configuration.of(String namespace)`.

### 5.11 Reusable Rule Profiles

A profile is a `Customizer<T>`, where `T` is a configurer type such as `JavaConfigurer` or
`TestConfigurer`. It packages a set of rules that can be applied to many modules or repositories,
and it can be combined with project-specific rules.

```java
private static final Customizer<JavaConfigurer> DEFAULT_JAVA_PROFILE = java -> java
    .noUsageOf(Date.class)
    .fieldsShouldNotBePublic();

private static final Customizer<TestConfigurer> DEFAULT_TEST_PROFILE = test -> test
    .junit(junit -> junit
        .methodsShouldBePackagePrivate()
        .methodsShouldMatch("should.*")
        .methodsShouldContainAssertionsOrVerifications()
        .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
        .classesShouldNotBeAnnotatedWithDisabled());
```

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(java -> {
      DEFAULT_JAVA_PROFILE.customize(java);                         // apply the profile
      java.classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto"));  // plus a local rule
    })
    .test(DEFAULT_TEST_PROFILE)                                     // use the profile directly
    .build()
    .checkAll();
```

### 5.12 Adding Custom ArchUnit Rules

Anything Taikai does not cover can be expressed as a plain ArchUnit rule and registered with
`addRule` or `addRules`. Custom rules participate in `check()` and `checkAll()` like built-in ones,
and accept the same `Configuration`.

```java
ArchRule rule = classes()
    .that().resideInAPackage("..domain..")
    .should().onlyBeAccessed().byAnyPackage("..domain..", "..application..");

Taikai.builder()
    .namespace("com.company.project")
    .addRule(TaikaiRule.of(rule))
    .addRule(TaikaiRule.of(rule, Configuration.of(Namespace.IMPORT.WITH_TESTS)))
    .addRules(List.of(TaikaiRule.of(rule), TaikaiRule.of(anotherRule)))
    .build()
    .checkAll();
```

### 5.13 Selecting with Criteria

Rules that apply to a selection of classes, fields or methods take an ArchUnit `DescribedPredicate`,
so the selection can be expressed precisely and combined freely. The parameter type tells you which
factory builds it:

| Parameter type | Factory | Listed as |
|----------------|---------|-----------|
| `DescribedPredicate<? super JavaClass>` | `com.enofex.taikai.ClassCriteria` | `classes`, and types such as `forbiddenTypes` |
| `DescribedPredicate<? super JavaField>` | `com.enofex.taikai.FieldCriteria` | `fields` |
| `DescribedPredicate<? super JavaMethod>` | `com.enofex.taikai.MethodCriteria` | `methods` |

Passing criteria of the wrong kind does not compile, so a `FieldCriteria` can never end up in a rule
that selects methods.

#### Class criteria

`ClassCriteria` selects the classes a rule applies to. Rules that restrict sets of types, such as
[`noUsageOf`](#noUsageOf), [`shouldNotImport`](#imports-shouldNotImport) or
[`methodsShouldNotReturnForClass`](#methodsShouldNotReturnForClass) or
[`fieldsShouldNotBeOfType`](#fieldsShouldNotBeOfType), take class criteria for those
types too, and `FieldCriteria.ofType(...)` selects fields by class criteria on their type.

| Criterion | Matches |
|-----------|---------|
| `ClassCriteria.annotatedWith(Class<? extends Annotation> annotationType)` | classes directly or meta-annotated with the annotation; `SOURCE`-retention annotations never match |
| `ClassCriteria.annotatedWith(String annotationType)` | same, by fully qualified name without `@` |
| `ClassCriteria.type(Class<?> clazz)` | exactly the type, without its subtypes, for example `Optional` |
| `ClassCriteria.assignableTo(Class<?> clazz)` | classes extending or implementing the type, directly or indirectly; the type itself does not match |
| `ClassCriteria.implementing(Class<?> clazz)` | classes implementing the interface, directly or indirectly; interfaces never match |
| `ClassCriteria.nameMatching(String regex)` | the entire **fully qualified** class name, for example `.*Dto` |
| `ClassCriteria.simpleNameMatching(String regex)` | the entire **simple** class name, for example `OrderDto` |
| `ClassCriteria.inPackage(String packageIdentifier)` | classes in an ArchUnit package identifier, for example `com.company.internal..` |

Combine criteria with `.and(...)` and `.or(...)`, and negate them with `DescribedPredicate.not(...)`.
Any other ArchUnit class predicate works as well, for example
`JavaClass.Predicates.assignableTo(Repository.class)`.

```java
.java(java -> java
    .classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto")
        .and(ClassCriteria.inPackage("..api..")))
    .classesShouldBePackagePrivate(ClassCriteria.annotatedWith("jakarta.persistence.Entity")
        .or(ClassCriteria.inPackage("..internal..")))
    .noUsageOf(ClassCriteria.inPackage("..domain.."),
        ClassCriteria.inPackage("com.company.legacy..")))
```

The description of the criteria becomes part of the rule name shown in failure reports.

Class criteria can also [exclude classes from every rule](#54-excluding-classes-globally) with
`excludeClasses(criteria)`, which also covers the rules that take no parameters, such as
`noUsageOfSystemOutOrErr()` or `fieldsShouldNotBePublic()`.

#### Field and method criteria

Rules that select fields or methods, such as
[`fieldsShouldHaveModifiers`](#fieldsShouldHaveModifiers) or
[`methodsShouldBeAnnotatedWith`](#methodsShouldBeAnnotatedWith), take field or method criteria:

| Criterion | Matches |
|-----------|---------|
| `FieldCriteria.nameMatching(String regex)` | the entire field name, without the declaring class, for example `logger` |
| `FieldCriteria.annotatedWith(Class<? extends Annotation> annotationType)` | fields directly or meta-annotated with the annotation |
| `FieldCriteria.ofType(Class<?> clazz)` | fields whose declared raw type is exactly the type; `List<String>` has the type `List` |
| `FieldCriteria.ofType(DescribedPredicate<? super JavaClass> types)` | fields whose declared raw type matches class criteria |
| `MethodCriteria.nameMatching(String regex)` | the entire method name, without the declaring class, for example `find.*` |
| `MethodCriteria.annotatedWith(Class<? extends Annotation> annotationType)` | methods directly or meta-annotated with the annotation |

Every `annotatedWith`, `type`, `assignableTo`, `implementing` and `ofType(Class)` criterion also
has an overload taking the fully qualified name as a `String`. Field and method criteria combine like class criteria, and with
ArchUnit member predicates such as `HasModifiers.Predicates.modifier(PUBLIC)`. This allows
selections that no single rule variant offers:

```java
.java(java -> java
    .fieldsShouldHaveModifiers(FieldCriteria.ofType(Logger.class), List.of(PRIVATE, STATIC, FINAL))
    .methodsShouldHaveModifiers(MethodCriteria.annotatedWith(GetMapping.class)
        .and(MethodCriteria.nameMatching("find.*")), List.of(PUBLIC)))
```

To select fields or methods by their declaring class, use the `…ForClass` variants, such as
[`methodsShouldBeAnnotatedWithForClass`](#methodsShouldBeAnnotatedWithForClass) or
[`fieldsShouldHaveModifiersForClass`](#fieldsShouldHaveModifiersForClass), which take class
criteria.

### 5.14 Custom Checks

Not every architecture or setup convention is visible in the compiled classes. A `TaikaiCheck`
checks anything else a test run can see, such as files in the project directory, resources, or the
classpath. Checks are shipped in a library, for example with the conventions of a whole company,
and discovered with the Java `ServiceLoader`. They run after the rules and are part of `check()`,
`checkAll()` and `dynamicTests()`, where each check becomes a test of its own.

```java
public final class LicenseCheck implements TaikaiCheck {

  @Override
  public String description() {
    return "Project should have a LICENSE";
  }

  @Override
  public List<String> violations(CheckContext context) {
    return Files.exists(context.projectDirectory().resolve("LICENSE"))
        ? List.of()
        : List.of("LICENSE is missing in " + context.projectDirectory());
  }
}
```

`TaikaiCheck` and `CheckContext` are in the package `com.enofex.taikai.checks.spi`. A check
returns one message per violation, or an empty list if it passes. Violations are reported like
those of a rule, but labeled and counted as checks:

```text
java.lang.AssertionError: Found 1 Taikai violations for 1 checks!

Check: Project should have a LICENSE
	LICENSE is missing in /home/dev/project
```

The `CheckContext` describes the project that is checked:

| Method | Returns |
|--------|---------|
| `check()` | The class of the check that receives the context. |
| `projectDirectory()` | The working directory of the test run, which Maven and Gradle set to the directory of the module under test, unless [configured otherwise](#configuring-the-checks). |
| `namespace()` | The configured namespace, or `null` if classes are configured instead. |
| `classes()` | The configured classes. Classes imported from the namespace exclude test classes and the [globally excluded classes](#54-excluding-classes-globally); classes set with [`classes(...)`](#52-selecting-classes-directly) are used as given, as for the rules. The import of a namespace is cached and shared with the rules. |
| `classLoader()` | The class loader of the test run, for example to find resources with `getResources(String)`. |
| `configuration(Class<T>)` | The [configuration](#configuring-a-single-check) of the check as an `Optional`, for example its settings. |
| `requireConfiguration(Class<T>)` | The configuration of the check, or an `IllegalStateException` naming the check if it is not configured. |

#### Registering and discovering checks

The library lists the fully qualified class names of its checks in the file
`META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck`, one per line:

```text
com.company.checks.LicenseCheck
com.company.checks.CodeownersCheck
```

A project that has the library on its test classpath runs these checks with `discoverChecks()`:

```java
Taikai.builder()
    .namespace("com.company.project")
    .discoverChecks()
    .build()
    .checkAll();
```

Discovery is opt-in, so adding a library never changes what a test checks on its own. The rules
for discovered checks are:

- A check must be a public class with a public constructor without parameters.
- Checks are discovered when `build()` is called, and run sorted by their description, so that the
  order is the same in Maven, Gradle and the IDE.
- A check that throws an exception, fails an assertion or misses a class does not escape as an
  error of its own. The failure is reported as a violation of that check, like any other:
  `checkAll()` and `dynamicTests()` still run the other rules and checks, while `check()` stops
  there, as it does at every violation.

#### Predefined checks

Taikai ships ready-made checks for common project conventions, such as required or forbidden
files, the Maven or Gradle Wrapper, or the Java version of the classes. They are documented in
[Section 13](#13-predefined-checks).

#### Configuring the checks

`discoverChecks` also takes a customizer for a `ChecksConfigurer`, in the same style as the rule
configurers:

```java
Taikai.builder()
    .namespace("com.company.project")
    .discoverChecks(checks -> checks
        .projectDirectory(Path.of(".."))   // the repository root instead of the module directory
        .exclude(LicenseCheck.class))      // switch off a single check
    .build()
    .checkAll();
```

| Method | Behavior |
|--------|----------|
| `projectDirectory(Path)` | Sets the project directory of the context. |
| `exclude(Class<? extends TaikaiCheck>...)` | Switches off single checks. An excluded check is not even instantiated. |
| `configure(Class<C>, Object)` | [Configures a single check](#configuring-a-single-check), for example with its settings. |
| `context(UnaryOperator<CheckContext>)` | [Changes the context](#changing-the-context) of every check. |
| `context(Class<C>, UnaryOperator<CheckContext>)` | Changes the context of a single check. |

`discoverChecks()` is the same as `discoverChecks(checks -> {})`. Calling `discoverChecks` several
times customizes the same `ChecksConfigurer`, so the settings add up, and `toBuilder()` keeps them.

`build()` fails with a `TaikaiException` if a registered check cannot be loaded or instantiated,
if `exclude(...)`, `configure(...)` or `context(...)` names a check that is not registered, or if
`configure(...)` or `context(...)` names an excluded check. A misspelled or outdated configuration
therefore does not go unnoticed.

#### Configuring a single check

A check is created through its constructor without parameters, so a project cannot pass settings
to it directly. Instead, the project configures the check by its class, and the check reads its
configuration from the context. A check in a library can define its settings as a record and fall
back to defaults when the project does not configure it:

```java
public record ForbiddenDependencies(List<String> artifacts) {
}

public final class ForbiddenDependenciesCheck implements TaikaiCheck {

  private static final ForbiddenDependencies DEFAULTS =
      new ForbiddenDependencies(List.of("log4j:log4j"));

  @Override
  public String description() {
    return "Forbidden dependencies should not be on the classpath";
  }

  @Override
  public List<String> violations(CheckContext context) {
    List<String> forbidden = context.configuration(ForbiddenDependencies.class)
        .orElse(DEFAULTS)
        .artifacts();
    // ...
  }
}
```

The project configures the check:

```java
Taikai.builder()
    .namespace("com.company.project")
    .discoverChecks(checks -> checks
        .configure(ForbiddenDependenciesCheck.class,
            new ForbiddenDependencies(List.of("log4j:log4j", "commons-logging"))))
    .build()
    .checkAll();
```

- Every check only sees its own configuration, so two checks can use the same type, such as
  `List<String>`, without getting each other's settings.
- A check has one configuration, typically a record with all its settings. Configuring a check
  again replaces its configuration.
- `configuration(...)` also accepts a supertype of the configured object. It throws an
  `IllegalStateException` if the check is configured with an object of another type, instead of
  silently falling back to the defaults.
- A check that cannot run without configuration uses `requireConfiguration(...)`; a missing
  configuration is then reported as a violation of that check.

#### Changing the context

A `context(...)` function receives the context of a check, which already uses the configured
project directory and contains the configuration of the check, and returns the context the check
gets instead. Extend `ForwardingCheckContext` from `com.enofex.taikai.checks.spi` to override only
the methods you want to change; it forwards all others to the wrapped context:

```java
.discoverChecks(checks -> checks
    .context(context -> new ForwardingCheckContext(context) {                      // every check
      @Override
      public ClassLoader classLoader() {
        return pluginClassLoader;
      }
    })
    .context(PomCheck.class, context -> new ForwardingCheckContext(context) {      // one check
      @Override
      public Path projectDirectory() {
        return super.projectDirectory().resolve("parent");
      }
    }))
```

The functions are applied in the order they are configured, each wrapping the result of the
previous one; a function for a single check is skipped for all other checks. To give a function the
last word, configure it last. A function can override every method of the context, including
`configuration(...)`, although `configure(...)` is the usual way to configure a check.

The functions are applied once per check and `Taikai` instance, and the resulting context is used
by `check()`, `checkAll()` and `dynamicTests()` alike. Anything expensive, such as creating a class
loader, belongs outside of the function. The context only changes what the checks see; checks are
always discovered with the class loader of the test run.

## 6. How to Read the Rule Reference

Sections 7 to 12 document every rule, and Section 13 the predefined checks. To keep them readable,
examples are shown as **fragments**
rather than complete tests. A fragment such as

```java
.java(java -> java
    .classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto")))
```

belongs inside the builder chain from [Section 4](#4-quick-start):

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(java -> java
        .classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto")))
    .build()
    .check();
```

Conventions used throughout the reference:

- Rules are fluent and chainable; each returns its configurer.
- Wherever a rule takes a single annotation or type, there are two overloads: one taking a
  `Class<?>` literal and one taking a fully qualified name as a `String`. The `String` form exists so
  you can reference types that are not on the test classpath. Only one form is shown per rule. Rules
  that take several annotations, such as
  [`classesShouldBeAnnotatedWithAll`](#classesShouldBeAnnotatedWithAll) or
  [`noSelfInvocationOfProxiedMethods`](#spring-noSelfInvocationOfProxiedMethods), take their fully
  qualified names only.
- Rules that apply to a selection of classes take a `DescribedPredicate<? super JavaClass>`,
  usually built with [`ClassCriteria`](#513-selecting-with-criteria), listed as `classes`.
  Rules that select fields or methods by name take `FieldCriteria` or `MethodCriteria`, listed as
  `fields` or `methods`.
- Every rule also has an overload with a trailing `TaikaiRule.Configuration` parameter
  (see [5.8](#58-per-rule-configuration)). It is omitted from the signatures below.
- `Collection<JavaModifier>` parameters take ArchUnit's
  `com.tngtech.archunit.core.domain.JavaModifier` constants, for example
  `List.of(PUBLIC, STATIC, FINAL)`.
- A *regex* must match the **whole** name, not merely occur inside it.
- For **classes**, a regex, including the one in `ClassCriteria.nameMatching`, is matched against the
  **fully qualified** name, so anchor it with a leading `.*`: `.*Dto` matches
  `com.company.project.OrderDto`, while `Dto` and `[A-Z].*` match nothing at all.
- For **methods and fields**, a regex, including the ones in `FieldCriteria.nameMatching` and
  `MethodCriteria.nameMatching`, is matched against the member's own name, without the declaring
  class: `should.*`, `^[A-Z][A-Z0-9_]*$`.
- A *packageIdentifier* is not a regex but an ArchUnit package identifier, matched against the
  fully qualified name with `*` for one segment and `..` for any number:
  `com.company.project..`, `..internal..`.
- An annotation matches whether it is placed directly on the class or member or inherited through
  another annotation, so a composed annotation such as `@GetMapping` counts as `@RequestMapping`.
  Only annotations kept in the class files can match, so `RetentionPolicy.SOURCE` annotations never
  do.
- Rules that check methods or fields ignore *synthetic* members generated by the compiler, which
  cannot be changed in source code: the `$values()` method of every enum, bridge methods of
  generic overrides with erased signatures, and fields such as `$assertionsDisabled`. Calls made
  by bridge methods do not count as self-invocations either.

## 7. Java Rules

Default import mode: `WITHOUT_TESTS`.

| Rule | Enforces |
|------|----------|
| [`classesShouldImplementHashCodeAndEquals`](#classesShouldImplementHashCodeAndEquals) | `hashCode` and `equals` overridden together |
| [`classesShouldResideInPackage`](#classesShouldResideInPackage) | classes live in a given package |
| [`classesShouldResideOutsidePackage`](#classesShouldResideOutsidePackage) | classes stay out of a given package |
| [`classesShouldBeRecords`](#classesShouldBeRecords) | matching classes are records |
| [`classesShouldBeInterfaces`](#classesShouldBeInterfaces) | matching classes are interfaces |
| [`classesShouldBeAssignableTo`](#classesShouldBeAssignableTo) | matching classes are assignable to a type |
| [`classesShouldImplement`](#classesShouldImplement) | matching classes implement an interface |
| [`classesShouldHaveModifiers`](#classesShouldHaveModifiers) | matching classes carry modifiers |
| [`classesShouldNotHaveModifiers`](#classesShouldNotHaveModifiers) | matching classes lack modifiers |
| [`classesShouldBePackagePrivate`](#classesShouldBePackagePrivate) | matching classes are package-private |
| [`classesShouldBeAnnotatedWith`](#classesShouldBeAnnotatedWith) | matching classes carry an annotation |
| [`classesShouldNotBeAnnotatedWith`](#classesShouldNotBeAnnotatedWith) | matching classes lack an annotation |
| [`classesShouldBeAnnotatedWithAll`](#classesShouldBeAnnotatedWithAll) | matching classes carry all given annotations |
| [`classesShouldHaveField`](#classesShouldHaveField) | matching classes declare or inherit a matching field |
| [`finalClassesShouldNotHaveProtectedMembers`](#finalClassesShouldNotHaveProtectedMembers) | `final` classes have no `protected` members |
| [`utilityClassesShouldBeFinalAndHavePrivateConstructor`](#utilityClassesShouldBeFinalAndHavePrivateConstructor) | utility classes cannot be instantiated or extended |
| [`fieldsShouldNotBePublic`](#fieldsShouldNotBePublic) | no `public` fields except `static` ones |
| [`fieldsShouldHaveModifiers`](#fieldsShouldHaveModifiers) | matching fields carry modifiers |
| [`fieldsShouldNotHaveModifiers`](#fieldsShouldNotHaveModifiers) | matching fields lack modifiers |
| [`fieldsShouldHaveModifiersForClass`](#fieldsShouldHaveModifiersForClass) | fields of matching classes carry modifiers |
| [`fieldsShouldNotHaveModifiersForClass`](#fieldsShouldNotHaveModifiersForClass) | fields of matching classes lack modifiers |
| [`fieldsShouldNotBeOfType`](#fieldsShouldNotBeOfType) | matching fields do not use forbidden types in their declared type |
| [`fieldsShouldNotBeOfTypeForClass`](#fieldsShouldNotBeOfTypeForClass) | fields of matching classes do not use forbidden types in their declared type |
| [`serialVersionUIDFieldsShouldBeStaticFinalLong`](#serialVersionUIDFieldsShouldBeStaticFinalLong) | `serialVersionUID` is `static final long` |
| [`methodsShouldNotDeclareGenericExceptions`](#methodsShouldNotDeclareGenericExceptions) | no `throws Exception` / `RuntimeException` |
| [`methodsShouldNotDeclareException`](#methodsShouldNotDeclareException) | matching methods do not declare a type |
| [`methodsShouldNotDeclareExceptionForClass`](#methodsShouldNotDeclareExceptionForClass) | methods of matching classes do not declare a type |
| [`methodsShouldNotReturn`](#methodsShouldNotReturn) | matching methods do not expose forbidden types in their return signatures |
| [`methodsShouldNotReturnForClass`](#methodsShouldNotReturnForClass) | selected classes do not expose forbidden types in method return signatures |
| [`methodsShouldReturn`](#methodsShouldReturn) | each matching method has a matching type in its return signature |
| [`methodsShouldReturnForClass`](#methodsShouldReturnForClass) | each selected method has a matching type in its return signature |
| [`methodsShouldBeAnnotatedWith`](#methodsShouldBeAnnotatedWith) | matching methods carry an annotation |
| [`methodsShouldBeAnnotatedWithForClass`](#methodsShouldBeAnnotatedWithForClass) | methods of matching classes carry an annotation |
| [`methodsShouldBeAnnotatedWithAll`](#methodsShouldBeAnnotatedWithAll) | matching methods carry several annotations |
| [`methodsShouldBeAnnotatedWithAllForClass`](#methodsShouldBeAnnotatedWithAllForClass) | methods of matching classes carry several annotations |
| [`methodsShouldNotBeAnnotatedWith`](#methodsShouldNotBeAnnotatedWith) | matching methods do not carry an annotation |
| [`methodsShouldNotBeAnnotatedWithForClass`](#methodsShouldNotBeAnnotatedWithForClass) | methods of matching classes do not carry an annotation |
| [`methodsShouldHaveModifiers`](#methodsShouldHaveModifiers) | matching methods carry modifiers |
| [`methodsShouldNotHaveModifiers`](#methodsShouldNotHaveModifiers) | matching methods lack modifiers |
| [`methodsShouldHaveModifiersForClass`](#methodsShouldHaveModifiersForClass) | methods of matching classes carry modifiers |
| [`methodsShouldNotHaveModifiersForClass`](#methodsShouldNotHaveModifiersForClass) | methods of matching classes lack modifiers |
| [`methodsShouldNotExceedMaxParameters`](#methodsShouldNotExceedMaxParameters) | methods have at most a given number of parameters |
| [`noUsageOf`](#noUsageOf) | a type is not used |
| [`noUsageOfDeprecatedAPIs`](#noUsageOfDeprecatedAPIs) | nothing `@Deprecated` is used |
| [`noUsageOfSystemOutOrErr`](#noUsageOfSystemOutOrErr) | no `System.out` / `System.err` |

Import and naming rules live in nested configurers, documented in [7.5](#75-imports) and
[7.6](#76-naming).

### 7.1 Classes

#### `classesShouldImplementHashCodeAndEquals` { #classesShouldImplementHashCodeAndEquals }

A class that overrides one of `hashCode` or `equals` must override both. Overriding only one breaks
the contract and produces objects that misbehave in hash-based collections.

```java
.java(java -> java
    .classesShouldImplementHashCodeAndEquals())
```

#### `classesShouldResideInPackage` { #classesShouldResideInPackage }

`classesShouldResideInPackage(String packageIdentifier)`
`classesShouldResideInPackage(DescribedPredicate<? super JavaClass> classes, String packageIdentifier)`

With one argument, *all* imported classes must reside in the given package identifier, which
supports the `*` and `..` wildcards. With criteria, only the matching classes are constrained.

```java
.java(java -> java
    .classesShouldResideInPackage("com.company.project..")
    .classesShouldResideInPackage(ClassCriteria.nameMatching(".*Utils"),
        "com.company.project.utils")
    .classesShouldResideInPackage(ClassCriteria.annotatedWith(Entity.class),
        "com.company.project.domain.."))
```

#### `classesShouldResideOutsidePackage` { #classesShouldResideOutsidePackage }

`classesShouldResideOutsidePackage(DescribedPredicate<? super JavaClass> classes, String packageIdentifier)`

The inverse of the two-argument form above. Useful for keeping transport types out of the domain.

```java
.java(java -> java
    .classesShouldResideOutsidePackage(ClassCriteria.nameMatching(".*Dto"),
        "com.company.project.domain..")
    .classesShouldResideOutsidePackage(ClassCriteria.annotatedWith(RestController.class),
        "com.company.project.domain.."))
```

#### `classesShouldBeRecords` { #classesShouldBeRecords }

`classesShouldBeRecords(DescribedPredicate<? super JavaClass> classes)`

```java
.java(java -> java
    .classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto"))
    .classesShouldBeRecords(ClassCriteria.annotatedWith(ConfigurationProperties.class))
    .classesShouldBeRecords(ClassCriteria.nameMatching(".*Event")
        .and(ClassCriteria.inPackage("..events.."))))
```

#### `classesShouldBeInterfaces` { #classesShouldBeInterfaces }

`classesShouldBeInterfaces(DescribedPredicate<? super JavaClass> classes)`

```java
.java(java -> java
    .classesShouldBeInterfaces(ClassCriteria.nameMatching(".*Repository"))
    .classesShouldBeInterfaces(ClassCriteria.inPackage("..port..")))
```

#### `classesShouldBeAssignableTo` { #classesShouldBeAssignableTo }

`classesShouldBeAssignableTo(DescribedPredicate<? super JavaClass> classes, Class<?> clazz)`

Classes matching the criteria must be assignable to the type, whether by extending a
class or implementing an interface.

```java
.java(java -> java
    .classesShouldBeAssignableTo(ClassCriteria.nameMatching(".*Repository"), BaseRepository.class)
    .classesShouldBeAssignableTo(ClassCriteria.annotatedWith(Repository.class),
        BaseRepository.class))
```

#### `classesShouldImplement` { #classesShouldImplement }

`classesShouldImplement(DescribedPredicate<? super JavaClass> classes, Class<?> clazz)`

Unlike `classesShouldBeAssignableTo`, an interface never satisfies this rule: the class itself must
implement the interface, directly or through a superclass or another interface.

```java
.java(java -> java
    .classesShouldImplement(ClassCriteria.nameMatching(".*Repository"), CrudRepository.class)
    .classesShouldImplement(ClassCriteria.annotatedWith(Repository.class), CrudRepository.class))
```

#### `classesShouldHaveModifiers` { #classesShouldHaveModifiers }

`classesShouldHaveModifiers(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .classesShouldHaveModifiers(ClassCriteria.nameMatching(".*Config"), List.of(PUBLIC, FINAL))
    .classesShouldHaveModifiers(ClassCriteria.annotatedWith(PublicApi.class), List.of(PUBLIC)))
```

#### `classesShouldNotHaveModifiers` { #classesShouldNotHaveModifiers }

`classesShouldNotHaveModifiers(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> forbiddenModifiers)`

```java
.java(java -> java
    .classesShouldNotHaveModifiers(ClassCriteria.nameMatching(".*Internal"), List.of(PUBLIC))
    .classesShouldNotHaveModifiers(ClassCriteria.inPackage("..internal.."), List.of(PUBLIC))
    .classesShouldNotHaveModifiers(ClassCriteria.annotatedWith(InternalApi.class), List.of(PUBLIC)))
```

#### `classesShouldBePackagePrivate` { #classesShouldBePackagePrivate }

`classesShouldBePackagePrivate(DescribedPredicate<? super JavaClass> classes)`

Keeps module internals such as mappers hidden from other packages. With
`ClassCriteria.assignableTo`, the subtypes are constrained but not the type itself, for example
Spring Data repositories but not the `Repository` interface.

```java
.java(java -> java
    .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Mapper"))
    .classesShouldBePackagePrivate(
        ClassCriteria.assignableTo("org.springframework.data.repository.Repository"))
    .classesShouldBePackagePrivate(ClassCriteria.annotatedWith("jakarta.persistence.Entity")
        .or(ClassCriteria.inPackage("..internal.."))))
```

#### `classesShouldBeAnnotatedWith` { #classesShouldBeAnnotatedWith }

`classesShouldBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType)`

```java
.java(java -> java
    .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"), PublicApi.class)
    .classesShouldBeAnnotatedWith(ClassCriteria.nameMatching(".*Api"),
        "com.company.project.PublicApi")
    .classesShouldBeAnnotatedWith(ClassCriteria.inPackage("..api.."), PublicApi.class))
```

#### `classesShouldNotBeAnnotatedWith` { #classesShouldNotBeAnnotatedWith }

`classesShouldNotBeAnnotatedWith(DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType)`

Selecting the classes by another annotation makes two annotations mutually exclusive.

```java
.java(java -> java
    .classesShouldNotBeAnnotatedWith(ClassCriteria.annotatedWith(PublicApi.class),
        InternalApi.class)
    .classesShouldNotBeAnnotatedWith(ClassCriteria.nameMatching(".*Internal"), PublicApi.class)
    .classesShouldNotBeAnnotatedWith(ClassCriteria.inPackage("..internal.."), PublicApi.class))
```

#### `classesShouldBeAnnotatedWithAll` { #classesShouldBeAnnotatedWithAll }

`classesShouldBeAnnotatedWithAll(DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes)`

Classes matching the criteria must carry all the annotations in the collection, given as fully
qualified annotation names.

```java
.java(java -> java
    .classesShouldBeAnnotatedWithAll(ClassCriteria.nameMatching(".*Controller"),
        List.of(RestController.class.getName(), RequestMapping.class.getName()))
    .classesShouldBeAnnotatedWithAll(ClassCriteria.annotatedWith(RestController.class),
        List.of(RequestMapping.class.getName())))
```

#### `classesShouldHaveField` { #classesShouldHaveField }

`classesShouldHaveField(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaField> requiredFields)`

Classes matching the criteria must have at least one field matching the field criteria. Select the
field by annotation, type or name with `FieldCriteria`, and combine criteria with `.and(...)` when a
single field must satisfy all of them. The example requires an optimistic-locking `@Version` field
in every entity, and a `Long` version in aggregates:

```java
.java(java -> java
    .classesShouldHaveField(ClassCriteria.annotatedWith("jakarta.persistence.Entity"),
        FieldCriteria.annotatedWith("jakarta.persistence.Version"))
    .classesShouldHaveField(ClassCriteria.inPackage("..aggregate.."),
        FieldCriteria.annotatedWith("jakarta.persistence.Version")
            .and(FieldCriteria.ofType(Long.class))))
```

#### `finalClassesShouldNotHaveProtectedMembers` { #finalClassesShouldNotHaveProtectedMembers }

A `final` class cannot be subclassed, so `protected` members are misleading and should be `private`
or package-private.

```java
.java(java -> java
    .finalClassesShouldNotHaveProtectedMembers())
```

#### `utilityClassesShouldBeFinalAndHavePrivateConstructor` { #utilityClassesShouldBeFinalAndHavePrivateConstructor }

Classes whose methods are all `static` must be `final` and have only private constructors, so they
can neither be extended nor instantiated.

A class declaring a `main` method is not treated as a utility class and is left alone entirely, so
application entry points do not need a private constructor. Interfaces are not utility classes
either, and neither are classes that extend a class other than `Object`, such as enums, records
and exceptions with static factory methods.

```java
.java(java -> java
    .utilityClassesShouldBeFinalAndHavePrivateConstructor())
```

### 7.2 Fields

#### `fieldsShouldNotBePublic` { #fieldsShouldNotBePublic }

No instance field may be `public`. **Every** `static` field is exempt, whether or not it is `final`,
so a mutable `public static` field passes this rule. Combine it with
[`fieldsShouldHaveModifiers`](#fieldsShouldHaveModifiers) if you also want constants pinned down:

```java
.java(java -> java
    .fieldsShouldNotBePublic()
    .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"),
        List.of(STATIC, FINAL)))
```

#### `fieldsShouldHaveModifiers` { #fieldsShouldHaveModifiers }

`fieldsShouldHaveModifiers(DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> requiredModifiers)`

Fields matching the criteria, selected by name, annotation or type with `FieldCriteria`, must have
all the modifiers. To select fields by their declaring class, use
[`fieldsShouldHaveModifiersForClass`](#fieldsShouldHaveModifiersForClass).

```java
.java(java -> java
    .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"),
        List.of(STATIC, FINAL))
    .fieldsShouldHaveModifiers(FieldCriteria.annotatedWith(Constant.class),
        List.of(PUBLIC, STATIC, FINAL)))
```

#### `fieldsShouldNotHaveModifiers` { #fieldsShouldNotHaveModifiers }

`fieldsShouldNotHaveModifiers(DescribedPredicate<? super JavaField> fields, Collection<JavaModifier> forbiddenModifiers)`

```java
.java(java -> java
    .fieldsShouldNotHaveModifiers(FieldCriteria.nameMatching(".*Cache"), List.of(PUBLIC))
    .fieldsShouldNotHaveModifiers(FieldCriteria.annotatedWith(Autowired.class), List.of(STATIC)))
```

#### `fieldsShouldHaveModifiersForClass` { #fieldsShouldHaveModifiersForClass }

`fieldsShouldHaveModifiersForClass(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers)`

Selects fields by their *declaring class*, so every field of a matching class is constrained, for
example to enforce constructor injection in services or encapsulated entities.

```java
.java(java -> java
    .fieldsShouldHaveModifiersForClass(ClassCriteria.annotatedWith(Service.class),
        List.of(PRIVATE, FINAL))
    .fieldsShouldHaveModifiersForClass(ClassCriteria.annotatedWith("jakarta.persistence.Entity"),
        List.of(PRIVATE)))
```

#### `fieldsShouldNotHaveModifiersForClass` { #fieldsShouldNotHaveModifiersForClass }

`fieldsShouldNotHaveModifiersForClass(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> forbiddenModifiers)`

Fails for a field that has any of the given modifiers.

```java
.java(java -> java
    .fieldsShouldNotHaveModifiersForClass(ClassCriteria.annotatedWith(RestController.class),
        List.of(STATIC)))
```

#### `fieldsShouldNotBeOfType` { #fieldsShouldNotBeOfType }

`fieldsShouldNotBeOfType(DescribedPredicate<? super JavaField> fields, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Like [`fieldsShouldNotBeOfTypeForClass`](#fieldsShouldNotBeOfTypeForClass), with the same checks
and limitations, but the fields are selected with field criteria, for example by name or
annotation. The example keeps entity identifiers away from `UUID`:

```java
.java(java -> java
    .fieldsShouldNotBeOfType(FieldCriteria.annotatedWith("jakarta.persistence.Id"),
        ClassCriteria.type(UUID.class)))
```

#### `fieldsShouldNotBeOfTypeForClass` { #fieldsShouldNotBeOfTypeForClass }

`fieldsShouldNotBeOfTypeForClass(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Keeps types out of the fields of selected classes while allowing them to be used elsewhere, for
example legacy date types in entities. `ClassCriteria.type` matches exactly the type, so combine it
with `ClassCriteria.assignableTo` to ban subtypes such as `java.sql.Timestamp` as well:

```java
.java(java -> java
    .fieldsShouldNotBeOfTypeForClass(ClassCriteria.annotatedWith("jakarta.persistence.Entity"),
        ClassCriteria.type(Date.class).or(ClassCriteria.assignableTo(Date.class))
            .or(ClassCriteria.type(Calendar.class))
            .or(ClassCriteria.assignableTo(Calendar.class))))
```

#### `serialVersionUIDFieldsShouldBeStaticFinalLong` { #serialVersionUIDFieldsShouldBeStaticFinalLong }

Fields named `serialVersionUID` must be declared `static final long`. Any other declaration is
ignored by Java serialization and silently fails to pin the class version.

```java
.java(java -> java
    .serialVersionUIDFieldsShouldBeStaticFinalLong())
```

### 7.3 Methods

#### `methodsShouldNotDeclareGenericExceptions` { #methodsShouldNotDeclareGenericExceptions }

Methods must not declare `Exception` or `RuntimeException`; use specific types instead.

```java
.java(java -> java
    .methodsShouldNotDeclareGenericExceptions())
```

#### `methodsShouldNotDeclareException` { #methodsShouldNotDeclareException }

`methodsShouldNotDeclareException(DescribedPredicate<? super JavaMethod> methods, Class<? extends Throwable> clazz)`
`methodsShouldNotDeclareException(DescribedPredicate<? super JavaMethod> methods, DescribedPredicate<? super JavaClass> forbiddenTypes)`

The member criteria select methods, usually by name with `MethodCriteria.nameMatching`. The second
overload forbids every exception type that matches class criteria, such as all exceptions of a
technical package.

```java
.java(java -> java
    .methodsShouldNotDeclareException(MethodCriteria.nameMatching("should.*"),
        SpecificException.class)
    .methodsShouldNotDeclareException(MethodCriteria.nameMatching(".*"),
        ClassCriteria.inPackage("java.sql")))
```

#### `methodsShouldNotDeclareExceptionForClass` { #methodsShouldNotDeclareExceptionForClass }

`methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes, Class<? extends Throwable> clazz)`
`methodsShouldNotDeclareExceptionForClass(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Selects methods by their *declaring class* rather than by name, so no method of a matching class
may declare the exception type. The second overload forbids every exception type that matches the
criteria.

```java
.java(java -> java
    .methodsShouldNotDeclareExceptionForClass(ClassCriteria.annotatedWith(RestController.class),
        SQLException.class)
    .methodsShouldNotDeclareExceptionForClass(ClassCriteria.inPackage("..domain.."),
        ClassCriteria.inPackage("java.sql")))
```

#### `methodsShouldNotReturn` { #methodsShouldNotReturn }

`methodsShouldNotReturn(DescribedPredicate<? super JavaMethod> methods, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Like [`methodsShouldNotReturnForClass`](#methodsShouldNotReturnForClass), with the same checks and
limitations, but the methods are selected with method criteria, for example by name or annotation.

```java
.java(java -> java
    .methodsShouldNotReturn(MethodCriteria.nameMatching("find.*"),
        ClassCriteria.type(Optional.class)))
```

#### `methodsShouldNotReturnForClass` { #methodsShouldNotReturnForClass }

`methodsShouldNotReturnForClass(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Keeps implementation types out of a layer's return signatures while allowing them to be used
internally. Both class selectors can be created with `com.enofex.taikai.ClassCriteria` or supplied as
ArchUnit predicates:

```java
.java(java -> java
    .methodsShouldNotReturnForClass(
        ClassCriteria.inPackage("com.company.controller.."),
        ClassCriteria.annotatedWith("jakarta.persistence.Entity"))
    .methodsShouldNotReturnForClass(
        ClassCriteria.nameMatching(".*Controller"),
        ClassCriteria.inPackage("com.company.internal..")
            .or(ClassCriteria.inPackage("com.collaborator.client.."))))
```

See [5.13](#513-selecting-with-criteria) for the available criteria and how to combine
them.

The rule checks all methods declared in selected classes, including private methods. It checks the
declared return type, nested generic arguments, wildcard and type-variable bounds, and array
components. For example, banning entities rejects `Entity`, `Entity[]`, `List<Entity>`, and
`ResponseEntity<List<Entity>>`. It does not inspect fields, parameters, method bodies, or inherited
methods declared in other classes. Types hidden by an erased signature such as `Object` or a raw
`List` cannot be detected. A final `Configuration` parameter supports the usual customization.

#### `methodsShouldReturn` { #methodsShouldReturn }

`methodsShouldReturn(DescribedPredicate<? super JavaMethod> methods, DescribedPredicate<? super JavaClass> requiredTypes)`

Like [`methodsShouldReturnForClass`](#methodsShouldReturnForClass), with the same checks and
limitations, but the methods are selected with method criteria.

```java
.java(java -> java
    .methodsShouldReturn(MethodCriteria.annotatedWith(GetMapping.class),
        ClassCriteria.nameMatching(".*DTO")))
```

#### `methodsShouldReturnForClass` { #methodsShouldReturnForClass }

`methodsShouldReturnForClass(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> requiredTypes)`

Requires every method declared in the selected classes to have at least one matching type in its
return signature. For example, require controllers to return objects whose class names end in
`DTO`:

```java
.java(java -> java
    .methodsShouldReturnForClass(ClassCriteria.nameMatching(".*Controller"),
        ClassCriteria.nameMatching(".*DTO")))
```

`OrderDTO`, `OrderDTO[]`, `List<OrderDTO>`, and `ResponseEntity<List<OrderDTO>>` satisfy this rule.
Nested generic arguments, wildcard and type-variable bounds, and array components are considered.
`String`, `Object`, primitives, and `void` fail the DTO criterion. Both `ClassCriteria.annotatedWith`
overloads and `ClassCriteria.inPackage` also work with this rule.

Matching one type satisfies the requirement even if the signature contains other types, such as
`Map<Entity, OrderDTO>`. Combine `methodsShouldReturnForClass` with
`methodsShouldNotReturnForClass` to prohibit entities or internal types as well. The
[same scope and limitations](#methodsShouldNotReturnForClass) apply: all visibilities are checked,
inherited declarations are excluded, and only declared types are inspected. A final
`Configuration` parameter supports customization.

#### `methodsShouldBeAnnotatedWith` { #methodsShouldBeAnnotatedWith }

`methodsShouldBeAnnotatedWith(DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType)`

Every method matching the criteria, usually selected by name with `MethodCriteria.nameMatching`,
must carry the annotation.

```java
.java(java -> java
    .methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching(".*Api"), PublicApi.class))
```

#### `methodsShouldBeAnnotatedWithForClass` { #methodsShouldBeAnnotatedWithForClass }

`methodsShouldBeAnnotatedWithForClass(DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType)`

Selects methods by their *declaring class*, so every method of a matching class must carry the
annotation. Meta-annotations count, so in the example below `@GetMapping` satisfies
`@RequestMapping` and every method of a Feign client must map to an endpoint.

```java
.java(java -> java
    .methodsShouldBeAnnotatedWithForClass(ClassCriteria.annotatedWith(FeignClient.class),
        RequestMapping.class))
```

The rule checks methods of every visibility, including private and static helpers, so it suits
classes whose methods all form an API, such as client interfaces. It does not suit controllers:
their private helpers would have to carry the annotation too, for example a `@PreAuthorize` that
Spring ignores on private methods. Select the endpoint methods by name with
[`methodsShouldBeAnnotatedWith`](#methodsShouldBeAnnotatedWith) instead.

#### `methodsShouldBeAnnotatedWithAll` { #methodsShouldBeAnnotatedWithAll }

`methodsShouldBeAnnotatedWithAll(DescribedPredicate<? super JavaMethod> methods, Collection<String> requiredAnnotationTypes)`

Methods matching the criteria must carry all the annotations in the collection, given as fully
qualified annotation names. A classic use is Spring Data: `@Modifying` is only correct together
with `@Transactional` and `@Query`.

```java
.java(java -> java
    .methodsShouldBeAnnotatedWithAll(MethodCriteria.annotatedWith(Modifying.class),
        List.of(Transactional.class.getName(), Query.class.getName())))
```

#### `methodsShouldBeAnnotatedWithAllForClass` { #methodsShouldBeAnnotatedWithAllForClass }

`methodsShouldBeAnnotatedWithAllForClass(DescribedPredicate<? super JavaClass> classes, Collection<String> requiredAnnotationTypes)`

Every method declared in a matching class must carry all the annotations, given as fully
qualified names.

```java
.java(java -> java
    .methodsShouldBeAnnotatedWithAllForClass(ClassCriteria.annotatedWith(FeignClient.class),
        List.of(RequestMapping.class.getName())))
```

#### `methodsShouldNotBeAnnotatedWith` { #methodsShouldNotBeAnnotatedWith }

`methodsShouldNotBeAnnotatedWith(DescribedPredicate<? super JavaMethod> methods, Class<? extends Annotation> annotationType)`

Selecting the methods by another annotation makes two annotations mutually exclusive.

```java
.java(java -> java
    .methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(PublicApi.class),
        InternalApi.class))
```

#### `methodsShouldNotBeAnnotatedWithForClass` { #methodsShouldNotBeAnnotatedWithForClass }

`methodsShouldNotBeAnnotatedWithForClass(DescribedPredicate<? super JavaClass> classes, Class<? extends Annotation> annotationType)`

No method declared in a matching class may carry the annotation.

```java
.java(java -> java
    .methodsShouldNotBeAnnotatedWithForClass(ClassCriteria.annotatedWith(RestController.class),
        Transactional.class))
```

#### `methodsShouldHaveModifiers` { #methodsShouldHaveModifiers }

`methodsShouldHaveModifiers(DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> requiredModifiers)`

Selects methods with method criteria, by *method* name or annotation.

```java
.java(java -> java
    .methodsShouldHaveModifiers(MethodCriteria.nameMatching("create.*"), List.of(PUBLIC))
    .methodsShouldHaveModifiers(MethodCriteria.annotatedWith(Transactional.class),
        List.of(PUBLIC)))
```

#### `methodsShouldNotHaveModifiers` { #methodsShouldNotHaveModifiers }

`methodsShouldNotHaveModifiers(DescribedPredicate<? super JavaMethod> methods, Collection<JavaModifier> forbiddenModifiers)`

```java
.java(java -> java
    .methodsShouldNotHaveModifiers(MethodCriteria.nameMatching("internal.*"), List.of(PUBLIC))
    .methodsShouldNotHaveModifiers(MethodCriteria.annotatedWith(Test.class), List.of(PUBLIC)))
```

#### `methodsShouldHaveModifiersForClass` { #methodsShouldHaveModifiersForClass }

`methodsShouldHaveModifiersForClass(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> requiredModifiers)`

Selects methods by the *declaring class*, so every method of a matching class is constrained.

```java
.java(java -> java
    .methodsShouldHaveModifiersForClass(ClassCriteria.nameMatching(".*Controller"), List.of(PUBLIC))
    .methodsShouldHaveModifiersForClass(ClassCriteria.annotatedWith(RestController.class),
        List.of(PUBLIC)))
```

#### `methodsShouldNotHaveModifiersForClass` { #methodsShouldNotHaveModifiersForClass }

`methodsShouldNotHaveModifiersForClass(DescribedPredicate<? super JavaClass> classes, Collection<JavaModifier> forbiddenModifiers)`

```java
.java(java -> java
    .methodsShouldNotHaveModifiersForClass(ClassCriteria.nameMatching(".*Internal"),
        List.of(PUBLIC))
    .methodsShouldNotHaveModifiersForClass(ClassCriteria.inPackage("..internal.."),
        List.of(PUBLIC)))
```

#### `methodsShouldNotExceedMaxParameters` { #methodsShouldNotExceedMaxParameters }

`methodsShouldNotExceedMaxParameters(int maxMethodParameters)`

Methods must not declare more parameters than the given maximum; a method with exactly that many
passes. Constructors are not checked.

```java
.java(java -> java
    .methodsShouldNotExceedMaxParameters(6))
```

### 7.4 Usage Restrictions

#### `noUsageOf` { #noUsageOf }

`noUsageOf(Class<?> clazz)`
`noUsageOf(DescribedPredicate<? super JavaClass> forbiddenTypes)`
`noUsageOf(DescribedPredicate<? super JavaClass> classes, Class<?> clazz)`
`noUsageOf(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> forbiddenTypes)`

Forbids all usage of a type. With two arguments, the first one selects the classes to check, which
lets you retire an API layer by layer, for example with `ClassCriteria.inPackage(...)`. A type passed as
`Class` or name is banned together with its subtypes; with `forbiddenTypes`, every type that matches
the criteria is banned, for example a whole package. Classes that match `forbiddenTypes` themselves
are not checked, so the classes of a banned package may still use each other.

```java
.java(java -> java
    .noUsageOf(Date.class)
    .noUsageOf(ClassCriteria.inPackage("com.company.project.domain.."), Calendar.class)
    .noUsageOf("com.company.legacy.UnwantedClass")
    .noUsageOf(ClassCriteria.annotatedWith(Entity.class), Date.class)
    .noUsageOf(ClassCriteria.inPackage("com.company.legacy.."))
    .noUsageOf(ClassCriteria.inPackage("..domain.."),
        ClassCriteria.inPackage("org.springframework.web..")))
```

Use this rule to keep HTTP exceptions out of service and repository packages, including unchecked
exceptions constructed without a `throws` declaration:

```java
.java(java -> java
    .noUsageOf(ClassCriteria.inPackage("com.company.service.."),
        "org.springframework.web.server.ResponseStatusException")
    .noUsageOf(ClassCriteria.inPackage("com.company.repository.."),
        "org.springframework.web.server.ResponseStatusException"))
```

The restriction includes subclasses and bans every dependency on the exception: construction,
declarations, catches, fields, and other references. It is a layer dependency rule, not an analysis
of which exceptions might propagate from called methods. `methodsShouldNotDeclareException` only
checks `throws` declarations and does not catch unchecked exceptions thrown without one.

#### `noUsageOfDeprecatedAPIs` { #noUsageOfDeprecatedAPIs }

Nothing annotated `@Deprecated` may be used.

```java
.java(java -> java
    .noUsageOfDeprecatedAPIs())
```

#### `noUsageOfSystemOutOrErr` { #noUsageOfSystemOutOrErr }

Forbids `System.out` and `System.err` in favor of a logging framework.

```java
.java(java -> java
    .noUsageOfSystemOutOrErr())
```

### 7.5 Imports

Import rules live in the nested `imports(...)` configurer.

| Rule | Enforces |
|------|----------|
| [`shouldHaveNoCycles`](#imports-shouldHaveNoCycles) | no cycles between the first-level subpackages of the namespace |
| [`shouldImport`](#imports-shouldImport) | matching classes import something |
| [`shouldNotImport`](#imports-shouldNotImport) | a package or import is forbidden |

#### `shouldHaveNoCycles` { #imports-shouldHaveNoCycles }

Fails on cyclic dependencies between the slices of your namespace. Each first-level subpackage
forms one slice together with all its subpackages: for `com.company.project`, everything in
`com.company.project.order..` is one slice and everything in `com.company.project.billing..`
another. Cycles inside a slice, such as between `order.api` and `order.internal`, are not reported,
and neither are cycles involving classes directly in `com.company.project`.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldHaveNoCycles()))
```

The rule needs a namespace, the global one or one given with `Configuration.of(String namespace)`.
It throws a `TaikaiException` if the classes are [set directly](#52-selecting-classes-directly)
without a namespace, and it reads the global namespace when `java(...)` is first configured, see
[5.10](#510-modifying-an-existing-configuration).

#### `shouldImport` { #imports-shouldImport }

`shouldImport(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> requiredImports)`

Classes matching the first criteria must import something matching the second.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldImport(ClassCriteria.nameMatching(".*Service"),
            ClassCriteria.nameMatching("com.company.project.BusinessException"))
        .shouldImport(ClassCriteria.annotatedWith(Service.class),
            ClassCriteria.nameMatching(".*BusinessException"))))
```

#### `shouldNotImport` { #imports-shouldNotImport }

`shouldNotImport(DescribedPredicate<? super JavaClass> forbiddenImports)`
`shouldNotImport(DescribedPredicate<? super JavaClass> classes, DescribedPredicate<? super JavaClass> forbiddenImports)`

With one argument, nothing may import classes matching the criteria; use
`ClassCriteria.inPackage(...)` to ban a package. With two, only classes matching the first criteria
are constrained. As with [`noUsageOf`](#noUsageOf), classes that match the disallowed criteria
themselves are not checked, so the classes of a banned package may still import each other.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldNotImport(ClassCriteria.inPackage("com.company.project.legacy.."))
        .shouldNotImport(ClassCriteria.nameMatching(".*Service"),
            ClassCriteria.nameMatching("com.company.project.SpecificException"))
        .shouldNotImport(ClassCriteria.annotatedWith(Deprecated.class))
        .shouldNotImport(ClassCriteria.inPackage("..domain.."),
            ClassCriteria.inPackage("org.springframework.web.."))))
```

A one-argument ban applies to every class outside the banned package, including the module that
owns it. To keep a module's `internal` package private to that module, constrain only the classes
outside the module:

```java
.java(java -> java
    .imports(imports -> imports
        .shouldNotImport(
            DescribedPredicate.not(ClassCriteria.inPackage("com.company.project.order..")),
            ClassCriteria.inPackage("com.company.project.order.internal.."))))
```

#### Predefined import patterns

`com.enofex.taikai.java.ImportPatterns` provides criteria for packages that are commonly banned
from production code or from a particular layer. Each one is `ClassCriteria.inPackage(...)` for the
package identifier below, so they work anywhere criteria are accepted, such as `shouldNotImport`,
`noUsageOf`, or `excludeClasses`.

```java
import static com.enofex.taikai.java.ImportPatterns.lombok;
import static com.enofex.taikai.java.ImportPatterns.shaded;
```

```java
.java(java -> java
    .imports(imports -> imports
        .shouldNotImport(lombok())
        .shouldNotImport(shaded())))
```

| Method               | Package identifier          |
|----------------------|-----------------------------|
| `apacheCommons()`    | `org.apache.commons..`      |
| `assertJ()`          | `org.assertj..`             |
| `hamcrest()`         | `org.hamcrest..`            |
| `hibernate()`        | `org.hibernate..`           |
| `jspecify()`         | `org.jspecify..`            |
| `junit()`            | `org.junit.jupiter..`       |
| `logback()`          | `ch.qos.logback..`          |
| `lombok()`           | `lombok..`                  |
| `mockito()`          | `org.mockito..`             |
| `shaded()`           | `..shaded..`                |
| `springBoot()`       | `org.springframework.boot..` |
| `springData()`       | `org.springframework.data..` |
| `springFramework()`  | `org.springframework..`     |
| `springSecurity()`   | `org.springframework.security..` |
| `testcontainers()`   | `org.testcontainers..`      |

### 7.6 Naming

Naming rules live in the nested `naming(...)` configurer.

| Rule | Enforces |
|------|----------|
| [`packagesShouldMatchDefault`](#naming-packagesShouldMatchDefault) | Taikai's default package convention |
| [`packagesShouldMatch`](#naming-packagesShouldMatch) | packages match a regex |
| [`classesShouldMatch`](#naming-classesShouldMatch) | class names match a regex |
| [`classesShouldNotMatch`](#naming-classesShouldNotMatch) | class names do not match a regex |
| [`methodsShouldMatch`](#naming-methodsShouldMatch) | method names match a regex |
| [`methodsShouldNotMatch`](#naming-methodsShouldNotMatch) | method names do not match a regex |
| [`methodsShouldMatchForClass`](#naming-methodsShouldMatchForClass) | methods of matching classes match a regex |
| [`methodsShouldNotMatchForClass`](#naming-methodsShouldNotMatchForClass) | methods of matching classes do not match a regex |
| [`fieldsShouldMatch`](#naming-fieldsShouldMatch) | names of matching fields match a regex |
| [`fieldsShouldNotMatch`](#naming-fieldsShouldNotMatch) | field names do not match a regex |
| [`fieldsShouldMatchForClass`](#naming-fieldsShouldMatchForClass) | fields of matching classes match a regex |
| [`fieldsShouldNotMatchForClass`](#naming-fieldsShouldNotMatchForClass) | fields of matching classes do not match a regex |
| [`constantsShouldFollowConventions`](#naming-constantsShouldFollowConventions) | constants are `UPPER_SNAKE_CASE` |
| [`enumConstantsShouldFollowConventions`](#naming-enumConstantsShouldFollowConventions) | enum constants are `UPPER_SNAKE_CASE` |
| [`booleanMethodsShouldStartWith`](#naming-booleanMethodsShouldStartWith) | methods returning `boolean` use a prefix |
| [`interfacesShouldNotHavePrefixI`](#naming-interfacesShouldNotHavePrefixI) | no `IFoo` interfaces |

#### `packagesShouldMatchDefault` { #naming-packagesShouldMatchDefault }

Applies Taikai's default package convention, `^[a-z_]+(\.[a-z_][a-z0-9_]*)*$`: lowercase segments,
no digits in the first character of a segment, no camel case.

```java
.java(java -> java
    .naming(naming -> naming
        .packagesShouldMatchDefault()))
```

#### `packagesShouldMatch` { #naming-packagesShouldMatch }

`packagesShouldMatch(String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .packagesShouldMatch("^com\\.company\\.project(\\.[a-z]+)*$")))
```

#### `classesShouldMatch` { #naming-classesShouldMatch }

`classesShouldMatch(String regex)`
`classesShouldMatch(DescribedPredicate<? super JavaClass> classes, String regex)`

Remember that the regex is matched against the fully qualified name, so it has to cover the package
too. The criteria overload restricts the rule to matching classes, for example annotated classes,
implementations of an interface or subtypes of a class.

```java
.java(java -> java
    .naming(naming -> naming
        .classesShouldMatch("com\\.company\\.project\\..*")
        .classesShouldMatch(ClassCriteria.inPackage("..web.."), ".*(Controller|Dto)")
        .classesShouldMatch(ClassCriteria.annotatedWith(Entity.class), ".*Entity")
        .classesShouldMatch(ClassCriteria.implementing(Configurer.class), ".*Configurer")
        .classesShouldMatch(ClassCriteria.assignableTo(AbstractConfigurer.class), ".*Configurer")))
```

#### `classesShouldNotMatch` { #naming-classesShouldNotMatch }

`classesShouldNotMatch(String regex)`
`classesShouldNotMatch(DescribedPredicate<? super JavaClass> classes, String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .classesShouldNotMatch(".*Impl")
        .classesShouldNotMatch(ClassCriteria.inPackage("..domain.."), ".*(Dto|Entity)")))
```

#### `methodsShouldMatch` { #naming-methodsShouldMatch }

`methodsShouldMatch(String regex)`
`methodsShouldMatch(DescribedPredicate<? super JavaMethod> methods, String regex)`

With one argument, all methods must match. The criteria overload restricts the rule to matching
methods. Synthetic methods generated by the compiler are ignored.

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldMatch("[a-z][a-zA-Z0-9]*")
        .methodsShouldMatch(MethodCriteria.annotatedWith(Scheduled.class), "^scheduled[A-Z].*")))
```

#### `methodsShouldNotMatch` { #naming-methodsShouldNotMatch }

`methodsShouldNotMatch(String regex)`
`methodsShouldNotMatch(DescribedPredicate<? super JavaMethod> methods, String regex)`

With one argument, no method may match. The criteria overload restricts the rule to matching
methods.

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldNotMatch("^(foo|bar).*")
        .methodsShouldNotMatch(MethodCriteria.annotatedWith(Test.class), "^test.*")))
```

#### `methodsShouldMatchForClass` { #naming-methodsShouldMatchForClass }

`methodsShouldMatchForClass(DescribedPredicate<? super JavaClass> classes, String regex)`

Selects methods by their *declaring class*, so every method of a matching class must have a name
matching the regex. Synthetic methods generated by the compiler are ignored.

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldMatchForClass(ClassCriteria.annotatedWith(Repository.class),
            "(find|save|delete).*")))
```

#### `methodsShouldNotMatchForClass` { #naming-methodsShouldNotMatchForClass }

`methodsShouldNotMatchForClass(DescribedPredicate<? super JavaClass> classes, String regex)`

Selects methods by their *declaring class*, so no method of a matching class may have a name
matching the regex. Synthetic methods generated by the compiler are ignored.

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldNotMatchForClass(ClassCriteria.annotatedWith(RestController.class), "get.*")))
```

#### `fieldsShouldMatch` { #naming-fieldsShouldMatch }

`fieldsShouldMatch(DescribedPredicate<? super JavaField> fields, String regex)`

Constrains the *name* of fields matching the criteria, for example by their declared **type** with
`FieldCriteria.ofType`. The example below requires every field of type `Matcher` to be named
`matcher`, every field whose type is a repository to end with `Repository`, and every `@Value`
field to be camel case.

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldMatch(FieldCriteria.ofType(Matcher.class), "matcher")
        .fieldsShouldMatch(FieldCriteria.ofType(ClassCriteria.annotatedWith(Repository.class)),
            ".*Repository")
        .fieldsShouldMatch(FieldCriteria.annotatedWith(Value.class), "^[a-z][a-zA-Z0-9]*$")))
```

To select fields by their *declaring class* instead, use
[`fieldsShouldMatchForClass`](#naming-fieldsShouldMatchForClass).

#### `fieldsShouldNotMatch` { #naming-fieldsShouldNotMatch }

`fieldsShouldNotMatch(String regex)`
`fieldsShouldNotMatch(DescribedPredicate<? super JavaField> fields, String regex)`

With one argument, no field may match. The criteria overload restricts the rule to matching fields.

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldNotMatch(".*(List|Set|Map)$")
        .fieldsShouldNotMatch(FieldCriteria.ofType(Logger.class), "^log$")))
```

#### `fieldsShouldMatchForClass` { #naming-fieldsShouldMatchForClass }

`fieldsShouldMatchForClass(DescribedPredicate<? super JavaClass> classes, String regex)`

Selects fields by their *declaring class*, so every field of a matching class must have a name
matching the regex. Unlike [`fieldsShouldMatch`](#naming-fieldsShouldMatch), the criteria select the
class the field is declared in, not the field's type. Synthetic fields generated by the compiler are
ignored.

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldMatchForClass(ClassCriteria.annotatedWith("jakarta.persistence.Entity"),
            "[a-z][a-zA-Z0-9]*")))
```

#### `fieldsShouldNotMatchForClass` { #naming-fieldsShouldNotMatchForClass }

`fieldsShouldNotMatchForClass(DescribedPredicate<? super JavaClass> classes, String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldNotMatchForClass(ClassCriteria.inPackage("..api.."), ".*(Entity|Dao)$")))
```

#### `constantsShouldFollowConventions` { #naming-constantsShouldFollowConventions }

`constantsShouldFollowConventions()`
`constantsShouldFollowConventions(Collection<String> excludedFields)`

`static final` fields must match `^[A-Z][A-Z0-9_]*$`. `serialVersionUID` is excluded by default.
Passing a collection **replaces** that default exclusion list, so include `serialVersionUID`
yourself if you still want it exempt.

```java
.java(java -> java
    .naming(naming -> naming
        .constantsShouldFollowConventions()))
```

```java
.java(java -> java
    .naming(naming -> naming
        .constantsShouldFollowConventions(List.of("serialVersionUID", "log"))))
```

#### `enumConstantsShouldFollowConventions` { #naming-enumConstantsShouldFollowConventions }

Enum constants must match `^[A-Z][A-Z0-9_]*$`.

```java
.java(java -> java
    .naming(naming -> naming
        .enumConstantsShouldFollowConventions()))
```

#### `booleanMethodsShouldStartWith` { #naming-booleanMethodsShouldStartWith }

`booleanMethodsShouldStartWith()`
`booleanMethodsShouldStartWith(Collection<String> prefixes)`

Methods returning `boolean` or `Boolean` must start with one of the given prefixes. The default set
is `is`, `has`, `can`, `should`; passing a collection replaces it. A prefix has to be followed by an
uppercase letter or nothing at all, so `is` and `isValid` pass but `issue` does not.

`equals(Object)`, record component accessors and synthetic methods are exempt.

```java
.java(java -> java
    .naming(naming -> naming
        .booleanMethodsShouldStartWith()))
```

```java
.java(java -> java
    .naming(naming -> naming
        .booleanMethodsShouldStartWith(List.of("is", "was"))))
```

#### `interfacesShouldNotHavePrefixI` { #naming-interfacesShouldNotHavePrefixI }

Rejects Hungarian-style interface names such as `IOrderService`.

```java
.java(java -> java
    .naming(naming -> naming
        .interfacesShouldNotHavePrefixI()))
```

## 8. Logging Rules

Default import mode: `WITHOUT_TESTS`.

| Rule | Enforces |
|------|----------|
| [`loggersShouldFollowConventions`](#loggersShouldFollowConventions) | logger fields have a fixed name and modifiers |
| [`classesShouldUseLogger`](#classesShouldUseLogger) | matching classes have a logger field |

#### `loggersShouldFollowConventions` { #loggersShouldFollowConventions }

`loggersShouldFollowConventions(Class<?> clazz, String regex)`
`loggersShouldFollowConventions(Class<?> clazz, String regex, Collection<JavaModifier> requiredModifiers)`

Fields declared with the given logger type or a subtype of it must have a name matching the regex
and, optionally, the listed modifiers.

```java
.logging(logging -> logging
    .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, STATIC, FINAL)))
```

The `String` overload avoids a compile-time dependency on the logging API:

```java
.logging(logging -> logging
    .loggersShouldFollowConventions("org.slf4j.Logger", "logger", List.of(PRIVATE, FINAL)))
```

#### `classesShouldUseLogger` { #classesShouldUseLogger }

`classesShouldUseLogger(DescribedPredicate<? super JavaClass> classes, Class<?> clazz)`

Classes matching the criteria must have a field of exactly the given logger type. A field inherited
from a superclass counts, a field of a subtype of the logger type does not. The rule complements the
one above: one fixes *how* loggers are declared, the other *that* a class has one.

```java
.logging(logging -> logging
    .classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), Logger.class)
    .classesShouldUseLogger(ClassCriteria.annotatedWith(Service.class), Logger.class))
```

## 9. Test Rules

Default import mode: `ONLY_TESTS`. All rules live in the nested `junit(...)` configurer.

| Rule | Enforces |
|------|----------|
| [`classesShouldEndWithTest`](#junit-classesShouldEndWithTest) | test classes end with `Test` |
| [`classesShouldMatch`](#junit-classesShouldMatch) | test class names match a regex |
| [`classesShouldBePackagePrivate`](#junit-classesShouldBePackagePrivate) | matching classes are package-private |
| [`classesShouldNotBeAnnotatedWithDisabled`](#junit-classesShouldNotBeAnnotatedWithDisabled) | no `@Disabled` classes |
| [`nestedClassesShouldBeAnnotatedWithNested`](#junit-nestedClassesShouldBeAnnotatedWithNested) | inner test classes carry `@Nested` |
| [`testsShouldBeExecutable`](#junit-testsShouldBeExecutable) | JUnit does not skip invalid test classes or methods |
| [`methodsShouldMatch`](#junit-methodsShouldMatch) | test method names match a regex |
| [`methodsShouldBePackagePrivate`](#junit-methodsShouldBePackagePrivate) | test methods are package-private |
| [`methodsShouldBeAnnotatedWithDisplayName`](#junit-methodsShouldBeAnnotatedWithDisplayName) | test methods carry `@DisplayName` |
| [`methodsShouldNotBeAnnotatedWithDisabled`](#junit-methodsShouldNotBeAnnotatedWithDisabled) | no `@Disabled` methods |
| [`methodsShouldNotDeclareExceptions`](#junit-methodsShouldNotDeclareExceptions) | test methods declare no `throws` |
| [`methodsShouldContainAssertionsOrVerifications`](#junit-methodsShouldContainAssertionsOrVerifications) | tests actually assert something |

Throughout this section, "test method" means a method meta-annotated `@Test` or
`@ParameterizedTest`, and "test class" a class containing such methods.

Four rules select differently. This matters because the `ONLY_TESTS` import contains every class of
the test sources, including helpers and fixtures without test methods:

- `classesShouldBePackagePrivate(classes)` selects by criteria, and skips interfaces.
- `classesShouldNotBeAnnotatedWithDisabled` applies to *all* imported classes.
- `methodsShouldNotBeAnnotatedWithDisabled` applies to *all* imported methods, so it also catches
  `@Disabled` on a `@BeforeEach` or a helper.
- [`testsShouldBeExecutable`](#junit-testsShouldBeExecutable) applies to all imported classes and
  follows JUnit's own definition of a test method, which also includes `@TestTemplate` and
  `@TestFactory` methods.

#### `classesShouldEndWithTest` { #junit-classesShouldEndWithTest }

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldEndWithTest()))
```

!!! note
    For `@Nested` classes the enclosing top-level class is checked, so inner classes do not need to
    match themselves.

#### `classesShouldMatch` { #junit-classesShouldMatch }

`classesShouldMatch(String regex)`

Same selection as above, with your own pattern. The `@Nested` handling is identical.

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldMatch(".*(Test|IT)")))
```

#### `classesShouldBePackagePrivate` { #junit-classesShouldBePackagePrivate }

`classesShouldBePackagePrivate(DescribedPredicate<? super JavaClass> classes)`

Unlike the rules above, this one selects by criteria, not by the presence of test methods.
Interfaces are excluded.

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
        .classesShouldBePackagePrivate(
            ClassCriteria.annotatedWith("org.springframework.boot.test.context.SpringBootTest"))))
```

#### `classesShouldNotBeAnnotatedWithDisabled` { #junit-classesShouldNotBeAnnotatedWithDisabled }

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldNotBeAnnotatedWithDisabled()))
```

#### `nestedClassesShouldBeAnnotatedWithNested` { #junit-nestedClassesShouldBeAnnotatedWithNested }

JUnit does not run an inner (non-static) class without `@Nested`. It only reports a discovery
warning, so the build stays green while the tests inside are silently skipped:

```java
class OrderServiceTest {

  class WhenCancelled {   // missing @Nested, never runs

    @Test
    void shouldRefundPayment() { ... }
  }
}
```

The rule checks inner classes containing test methods. Static nested classes are not inner classes,
and abstract inner classes are skipped because JUnit allows them as base classes for `@Nested`
classes. Meta-annotations carrying `@Nested` are accepted.

```java
.test(test -> test
    .junit(junit -> junit
        .nestedClassesShouldBeAnnotatedWithNested()))
```

!!! note
    A project without inner test classes matches nothing, so this rule fails under
    `failOnEmpty(true)`.

#### `testsShouldBeExecutable` { #junit-testsShouldBeExecutable }

JUnit does not execute invalid test declarations. It only reports a discovery warning, so the build
stays green while the affected tests never run:

```java
class OrderServiceTest {

  @Test
  private void shouldRejectEmptyOrder() { ... }   // private, never runs

  @Test
  boolean shouldAcceptOrder() { ... }              // returns a value, never runs

  @Nested
  static class WhenCancelled { ... }               // static, only runs if discovered on its own
}
```

The rule follows the discovery validation of JUnit Jupiter and reports:

- methods annotated with `@Test`, `@TestTemplate` or `@TestFactory` that are `private` or
  `static`, including meta-annotations such as `@ParameterizedTest`, `@RepeatedTest` or your own
  composed annotations
- such methods that return a value, except `@TestFactory` methods, which must return a
  `DynamicNode` or a `Stream`, `Collection`, `Iterable`, `Iterator` or array of `DynamicNode`
- static classes annotated with `@Nested`
- private classes annotated with `@Nested` or containing test methods, unless they are abstract
- local and anonymous classes containing test methods

```java
.test(test -> test
    .junit(junit -> junit
        .testsShouldBeExecutable()))
```

!!! note
    Inner classes containing test methods without `@Nested` are skipped by JUnit as well. They are
    reported by [`nestedClassesShouldBeAnnotatedWithNested`](#junit-nestedClassesShouldBeAnnotatedWithNested).

!!! tip
    JUnit can fail the build on these warnings itself. Set the critical severity for discovery
    issues to `WARNING` in `src/test/resources/junit-platform.properties`:

    ```properties
    junit.platform.discovery.issue.severity.critical=WARNING
    ```

    The rule is still useful for projects that do not set this property, and it fails the
    architecture test directly with a precise message.

#### `methodsShouldMatch` { #junit-methodsShouldMatch }

`methodsShouldMatch(String regex)`

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldMatch("should[A-Z].*")))
```

#### `methodsShouldBePackagePrivate` { #junit-methodsShouldBePackagePrivate }

JUnit Jupiter does not require `public` test methods, so `public` is noise.

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldBePackagePrivate()))
```

#### `methodsShouldBeAnnotatedWithDisplayName` { #junit-methodsShouldBeAnnotatedWithDisplayName }

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldBeAnnotatedWithDisplayName()))
```

#### `methodsShouldNotBeAnnotatedWithDisabled` { #junit-methodsShouldNotBeAnnotatedWithDisabled }

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldNotBeAnnotatedWithDisabled()))
```

#### `methodsShouldNotDeclareExceptions` { #junit-methodsShouldNotDeclareExceptions }

Test methods must not declare `throws`; assert on the exception instead.

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldNotDeclareExceptions()))
```

#### `methodsShouldContainAssertionsOrVerifications` { #junit-methodsShouldContainAssertionsOrVerifications }

Every test method must contain at least one assertion or verification. A call to any of the
following counts:

| Framework      | Recognized call                                                              |
|----------------|-------------------------------------------------------------------------------|
| JUnit          | any method on `org.junit.jupiter.api.Assertions`                             |
| AssertJ        | any method on `org.assertj.core.api.Assertions`                              |
| Hamcrest       | any method on `org.hamcrest.MatcherAssert`                                   |
| Truth          | any method on `com.google.common.truth.Truth`                                |
| Mockito        | `org.mockito.Mockito.verify*`, `inOrder`, `capture`                          |
| Cucumber       | any method on `io.cucumber.java.en.Then` or `io.cucumber.java.en.Given`      |
| Spring MockMvc | `org.springframework.test.web.servlet.ResultActions.andExpect` / `andExpectAll` |
| ArchUnit       | `com.tngtech.archunit.lang.ArchRule.check`                                   |
| Taikai         | `com.enofex.taikai.Taikai.check` / `checkAll`                                |

Calls made indirectly through helper methods also count, e.g. a private `assertValid(...)` method in
the same test class, a method inherited from a base test class, or a shared assertion utility. Helper
methods are followed transitively as long as their classes are part of the imported classes.

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldContainAssertionsOrVerifications()))
```

## 10. Spring Rules

Default import mode: `WITHOUT_TESTS`.

| Rule | Enforces |
|------|----------|
| [`noAutowiredFields`](#spring-noAutowiredFields) | constructor injection instead of `@Autowired` fields |
| [`noInjectionIntoStaticFields`](#spring-noInjectionIntoStaticFields) | no injection annotations on static fields or methods |
| [`noSelfInvocationOfProxiedMethods`](#spring-noSelfInvocationOfProxiedMethods) | proxied annotations are not bypassed |
| [`boot.applicationClassShouldResideInPackage`](#spring-boot-applicationClassShouldResideInPackage) | `@SpringBootApplication` sits in the base package |
| [`properties.*`](#103-properties) | `@ConfigurationProperties` conventions |
| [`configurations.*`](#104-configurations) | `@Configuration` naming |
| [`controllers.*`](#105-controllers) | controller naming, visibility and dependencies |
| [`services.*`](#106-services) | service naming and dependencies |
| [`repositories.*`](#107-repositories) | repository naming and dependencies |
| [`transactional.*`](#108-transactional) | `@Transactional` is actually effective |

### 10.1 General

#### `noAutowiredFields` { #spring-noAutowiredFields }

Fields must not be annotated `@Autowired`; use constructor injection, which keeps dependencies
explicit and objects testable without a container.

```java
.spring(spring -> spring
    .noAutowiredFields())
```

#### `noInjectionIntoStaticFields` { #spring-noInjectionIntoStaticFields }

Spring injects into bean instances only. It skips static fields and methods annotated with
`@Autowired`, `@Value` or `@Inject` and merely logs this at info level, so the application starts,
a static field stays `null` and a static setter is never called. The rule reports these fields and
methods, including those annotated with a composed annotation such as `@LocalServerPort`, which is
meta-annotated with `@Value`.

```java
@Component
class PaymentClient {

  @Value("${payment.timeout}")
  private static int timeout; // never injected, stays 0
}
```

```java
.spring(spring -> spring
    .noInjectionIntoStaticFields())
```

These annotations are checked. None of them has to be on the classpath:

- `org.springframework.beans.factory.annotation.Autowired`
- `org.springframework.beans.factory.annotation.Value`
- `jakarta.inject.Inject`
- `javax.inject.Inject`

!!! note
    Only annotations on the fields and methods themselves are considered. A static `@Bean` method
    whose parameters are annotated with `@Value` works as expected and is not reported.

#### `noSelfInvocationOfProxiedMethods` { #spring-noSelfInvocationOfProxiedMethods }

`noSelfInvocationOfProxiedMethods()`
`noSelfInvocationOfProxiedMethods(Collection<String> annotationTypes)`

Spring implements `@Transactional`, `@Async`, caching, method security and retries by wrapping the
bean in a proxy. A call such as `this.save()` targets the bean instance directly rather than the
proxy, so the annotation on the called method is silently ignored: no transaction is started, no
thread is switched, no cache is consulted, no security check runs. The proxy is only involved when
the call arrives from another bean.

```java
@Service
class OrderService {

  void process(Order order) {
    save(order); // the proxy is bypassed, @Transactional has no effect
  }

  @Transactional
  public void save(Order order) {
  }
}
```

```java
.spring(spring -> spring
    .noSelfInvocationOfProxiedMethods())
```

By default these annotations are checked. None of them has to be on the classpath:

- `org.springframework.transaction.annotation.Transactional`
- `jakarta.transaction.Transactional`
- `org.springframework.scheduling.annotation.Async`
- `org.springframework.cache.annotation.Cacheable`
- `org.springframework.cache.annotation.CacheEvict`
- `org.springframework.cache.annotation.CachePut`
- `org.springframework.security.access.prepost.PreAuthorize`
- `org.springframework.security.access.prepost.PostAuthorize`
- `org.springframework.retry.annotation.Retryable`

Pass a collection of fully qualified names to replace that list:

```java
.spring(spring -> spring
    .noSelfInvocationOfProxiedMethods(List.of(
        "org.springframework.transaction.annotation.Transactional",
        "org.springframework.scheduling.annotation.Async")))
```

!!! note
    Only annotations declared on the *called method* are considered; class-level annotations are
    ignored. Because the receiver of a call cannot be resolved statically, calls on another instance
    of the same class, such as the self-injection workaround, are reported too. Exclude those
    classes with a `Configuration`:

    ```java
    .spring(spring -> spring
        .noSelfInvocationOfProxiedMethods(
            Configuration.of(List.of(OrderService.class))))
    ```

### 10.2 Boot

#### `applicationClassShouldResideInPackage` { #spring-boot-applicationClassShouldResideInPackage }

`applicationClassShouldResideInPackage()`
`applicationClassShouldResideInPackage(String packageIdentifier)`

The `@SpringBootApplication` class must reside in the given package. Component scanning starts from
that package, so a misplaced application class silently changes which beans are discovered.

The no-argument overload uses the configured `namespace`, which is what you want in almost every
project. Its `Configuration` overload uses the namespace of that configuration if one is set, and
the global `namespace` otherwise. The global namespace is read when `spring(...)` is first
configured, see [5.10](#510-modifying-an-existing-configuration):

```java
.spring(spring -> spring
    .boot(boot -> boot
        .applicationClassShouldResideInPackage()))
```

```java
.spring(spring -> spring
    .boot(boot -> boot
        .applicationClassShouldResideInPackage("com.company.project")))
```

### 10.3 Properties

Applies to classes annotated `@ConfigurationProperties`, except
`shouldBeAnnotatedWithConfigurationProperties`, which works the other way round.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithProperties()` | name ends with `Properties` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithConfigurationProperties()` | classes ending in `Properties` carry the annotation (interfaces excluded) |
| `shouldBeAnnotatedWithConfigurationProperties(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldBeAnnotatedWithValidated()` | classes carry `@Validated` |
| `shouldBeRecords()` | classes are records |

```java
.spring(spring -> spring
    .properties(properties -> properties
        .namesShouldEndWithProperties()
        .shouldBeAnnotatedWithConfigurationProperties()
        .shouldBeAnnotatedWithValidated()
        .shouldBeRecords()))
```

### 10.4 Configurations

Applies to classes annotated `@Configuration`, except the `@SpringBootApplication` class: it is a
configuration class too, but its placement is covered by [Boot](#102-boot) instead.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithConfiguration()` | name ends with `Configuration` |
| `namesShouldMatch(String regex)` | name matches a regex |

```java
.spring(spring -> spring
    .configurations(configurations -> configurations
        .namesShouldEndWithConfiguration()))
```

### 10.5 Controllers

Applies to classes annotated `@Controller` or `@RestController`.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithController()` | name ends with `Controller` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithController()` | classes ending in `Controller` carry `@Controller` (interfaces excluded) |
| `shouldBeAnnotatedWithController(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldBeAnnotatedWithRestController()` | classes ending in `Controller` carry `@RestController` (interfaces excluded) |
| `shouldBeAnnotatedWithRestController(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldBePackagePrivate()` | controllers are package-private |
| `shouldNotDependOnOtherControllers()` | no controller-to-controller dependencies |
| `shouldNotDependOnRepositories()` | the service layer is not bypassed |
| `methodsShouldNotReturn(DescribedPredicate<? super JavaClass> forbiddenTypes)` | return signatures do not expose forbidden types |
| `methodsShouldReturn(DescribedPredicate<? super JavaClass> requiredTypes)` | each method has a matching type in its return signature |
| `shouldBeAnnotatedWithValidated()` | controllers needing `@Validated` carry it |
| `shouldBeAnnotatedWithValidated(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldNotBeAnnotatedWithValidated()` | controllers do not carry `@Validated` |
| `shouldNotBeAnnotatedWithValidated(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |

```java
.spring(spring -> spring
    .controllers(controllers -> controllers
        .namesShouldEndWithController()
        .shouldBeAnnotatedWithRestController()
        .shouldBePackagePrivate()
        .shouldNotDependOnOtherControllers()
        .shouldNotDependOnRepositories()
        .shouldBeAnnotatedWithValidated()))
```

The criteria overloads of `shouldBeAnnotatedWith...` select classes by name, package
or another criterion rather than by the controller annotation, which is how you enforce the
annotation on classes that do not carry it yet:

```java
.spring(spring -> spring
    .controllers(controllers -> controllers
        .shouldBeAnnotatedWithRestController(ClassCriteria.inPackage("com.company.project.web.."))
        .shouldBeAnnotatedWithValidated(ClassCriteria.nameMatching(".*Controller"))))
```

Use `methodsShouldNotReturn` to prevent controllers from returning persistence entities or types
from internal and collaborator packages:

```java
.spring(spring -> spring
    .controllers(controllers -> controllers
        .methodsShouldNotReturn(ClassCriteria.annotatedWith("jakarta.persistence.Entity"))
        .methodsShouldNotReturn(ClassCriteria.inPackage("com.company.internal..")
            .or(ClassCriteria.inPackage("com.collaborator.client..")))))
```

Composed controller annotations are recognized. The signature checks and limitations are the
same as the [Java return-type rule](#methodsShouldNotReturnForClass); a `Configuration` overload
is available.

Use `methodsShouldReturn` to require a matching return type, including inside wrappers. For example,
require class names ending in `DTO` and also forbid entities:

```java
.spring(spring -> spring
    .controllers(controllers -> controllers
        .methodsShouldReturn(ClassCriteria.nameMatching(".*DTO"))
        .methodsShouldNotReturn(ClassCriteria.annotatedWith("jakarta.persistence.Entity"))))
```

The requirement applies to each declared controller method, including private helpers and `void`
methods. It has the [same semantics as the Java rule](#methodsShouldReturnForClass) and a
`Configuration` overload.

`shouldBeAnnotatedWithValidated` only flags controllers that actually need it: those with a method
parameter annotated `@PathVariable` or `@RequestParam` that also carries one of the constraints
`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max` or `@Pattern`, from `jakarta.validation` or
`javax.validation`, or an annotation that is annotated with one of them. Other constraints, such as
`@Positive` or `@Email`, and `@Valid` are not recognized. Without `@Validated` on the class, Spring
never evaluates those constraints.
Use `shouldNotBeAnnotatedWithValidated` for the opposite convention, where validation is handled
elsewhere.

`shouldNotDependOnRepositories` completes the layering rules together with
`services.shouldNotDependOnControllers`, `repositories.shouldNotDependOnServices` and
`repositories.shouldNotDependOnControllers`. It reports controllers that inject a `@Repository`
directly and thereby skip the service layer.

### 10.6 Services

Applies to classes annotated `@Service`.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithService()` | name ends with `Service` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithService()` | classes ending in `Service` carry `@Service` (interfaces excluded) |
| `shouldBeAnnotatedWithService(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldNotDependOnControllers()` | no service-to-controller dependencies |
| `shouldNotDependOnOtherServices()` | no service-to-service dependencies |

```java
.spring(spring -> spring
    .services(services -> services
        .namesShouldEndWithService()
        .shouldBeAnnotatedWithService()
        .shouldNotDependOnControllers()))
```

!!! tip
    `shouldNotDependOnOtherServices` is strict, and deliberately so, but many codebases legitimately
    compose services. Adopt it only if that is a convention you actually want to hold.

### 10.7 Repositories

Applies to classes annotated `@Repository`.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithRepository()` | name ends with `Repository` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithRepository()` | classes ending in `Repository` carry `@Repository` (interfaces, such as Spring Data repositories, excluded) |
| `shouldBeAnnotatedWithRepository(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldNotDependOnControllers()` | no repository-to-controller dependencies |
| `shouldNotDependOnServices()` | no repository-to-service dependencies |
| `shouldBePackagePrivate()` | `@Repository` classes and Spring Data repositories are package-private |

```java
.spring(spring -> spring
    .repositories(repositories -> repositories
        .namesShouldEndWithRepository()
        .shouldBeAnnotatedWithRepository()
        .shouldNotDependOnServices()
        .shouldNotDependOnControllers()
        .shouldBePackagePrivate()))
```

### 10.8 Transactional

These rules target the ways `@Transactional` silently does nothing at runtime. Both
`org.springframework.transaction.annotation.Transactional` and `jakarta.transaction.Transactional`
are recognized.

| Rule | Enforces |
|------|----------|
| `methodsShouldBePublic()` | `@Transactional` methods are `public` |
| `shouldNotBeSelfInvoked()` | `@Transactional` methods are not called from within their own class |
| `shouldNotBeUsedInControllers()` | transaction boundaries live in the service layer |

```java
.spring(spring -> spring
    .transactional(transactional -> transactional
        .methodsShouldBePublic()
        .shouldNotBeSelfInvoked()
        .shouldNotBeUsedInControllers()))
```

`methodsShouldBePublic` exists because Spring's proxy-based transaction management ignores
non-public methods outright.

`shouldNotBeSelfInvoked` is the `@Transactional`-specific variant of
[`noSelfInvocationOfProxiedMethods`](#spring-noSelfInvocationOfProxiedMethods) and reports the call
site with its line number:

```java
@Service
class OrderService {

  void process(Order order) {
    save(order); // no transaction is created, the proxy is bypassed
  }

  @Transactional
  public void save(Order order) {
  }
}
```

`shouldNotBeUsedInControllers` reports controllers annotated `@Transactional` as well as controllers
declaring `@Transactional` methods.

## 11. Quarkus Rules

Default import mode: `WITHOUT_TESTS`.

| Rule | Enforces |
|------|----------|
| [`noInjectionFields`](#quarkus-noInjectionFields) | constructor injection instead of `@Inject` fields |
| [`resources.*`](#112-resources) | `@Path` resource naming, annotation, visibility and dependencies |
| [`panache.*`](#113-panache) | Panache entity and repository conventions |
| [`ai.*`](#114-ai-services) | `@RegisterAiService` naming, scope and tools |

### 11.1 General

#### `noInjectionFields` { #quarkus-noInjectionFields }

Fields must not be annotated `@Inject`; use constructor injection.

```java
.quarkus(quarkus -> quarkus
    .noInjectionFields())
```

### 11.2 Resources

Applies to classes annotated `@Path`.

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithResource()` | name ends with `Resource` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithPath()` | classes ending in `Resource` carry `@Path` (interfaces excluded) |
| `shouldBeAnnotatedWithPath(DescribedPredicate<? super JavaClass> classes)` | same, for classes matching criteria |
| `shouldBePublic()` | resource classes are `public` |
| `shouldNotDependOnOtherResources()` | no resource-to-resource dependencies |

```java
.quarkus(quarkus -> quarkus
    .resources(resources -> resources
        .namesShouldEndWithResource()
        .shouldBeAnnotatedWithPath()
        .shouldBePublic()
        .shouldNotDependOnOtherResources()))
```

### 11.3 Panache

| Rule | Enforces |
|------|----------|
| `shouldBeAnnotatedWithEntityWhenActiveRecordPattern()` | classes extending `PanacheEntity` carry `@Entity` |
| `namesShouldEndWithRepository()` | classes implementing `PanacheRepository` end with `Repository` |
| `namesShouldMatch(String regex)` | those classes match a regex |

```java
.quarkus(quarkus -> quarkus
    .panache(panache -> panache
        .shouldBeAnnotatedWithEntityWhenActiveRecordPattern()
        .namesShouldEndWithRepository()))
```

### 11.4 AI Services

Applies to classes annotated `@RegisterAiService` (Quarkus LangChain4j).

| Rule | Enforces |
|------|----------|
| `namesShouldEndWithAssistantOrService()` | name ends with `Assistant` or `Service` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithApplicationScoped()` | AI services carry `@ApplicationScoped` |
| `shouldNotUseToolsAttributeInAiService()` | tools are not declared via the `tools` attribute |

```java
.quarkus(quarkus -> quarkus
    .ai(ai -> ai
        .namesShouldEndWithAssistantOrService()
        .shouldBeAnnotatedWithApplicationScoped()
        .shouldNotUseToolsAttributeInAiService()))
```

`shouldNotUseToolsAttributeInAiService` steers you towards `@Toolbox` on the method, which scopes
tools per call instead of per service.

## 12. JPA Rules

Default import mode: `WITHOUT_TESTS`.

The rules only depend on the `jakarta.persistence` annotations, so they work with any JPA provider
or framework, such as Spring Data JPA, Quarkus Hibernate ORM or plain Jakarta EE. Annotations are
referenced by name, so Taikai does not need the JPA API at compile time. The older
`javax.persistence` annotations are not supported.

| Rule | Enforces |
|------|----------|
| [`entities.*`](#121-entities) | entity visibility, identifier and field types, optimistic locking and naming |

### 12.1 Entities

Applies to classes annotated `@Entity`, including composed annotations.

| Rule | Enforces |
|------|----------|
| `shouldBePackagePrivate()` | entities are package-private |
| `idsShouldNotBeOfType(DescribedPredicate<? super JavaClass> forbiddenTypes)` | `@Id` and `@EmbeddedId` fields of entities and mapped superclasses do not use forbidden types |
| `shouldHaveFieldAnnotatedWithVersion()` | entities declare or inherit a `@Version` field |
| `fieldsShouldNotBeOfType(DescribedPredicate<? super JavaClass> forbiddenTypes)` | fields of entities and mapped superclasses do not use forbidden types |
| `namesShouldMatch(String regex)` | entity names match a regex |

```java
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
```

## 13. Predefined Checks

Taikai ships [custom checks](#514-custom-checks) for common project conventions in the package
`com.enofex.taikai.checks`. Taikai does not register them itself, so that a new Taikai version
never adds checks on its own. To run them, list the checks you want in the file
`src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck` of the project,
next to any other checks:

```text
com.enofex.taikai.checks.ClassFileVersionCheck
com.enofex.taikai.checks.ForbiddenFilesCheck
com.enofex.taikai.checks.GradleWrapperCheck
com.enofex.taikai.checks.MavenWrapperCheck
com.enofex.taikai.checks.RequiredFilesCheck
com.enofex.taikai.checks.ServiceRegistrationsCheck
```

Then discover them with `discoverChecks()`. Checks with settings are
[configured](#configuring-a-single-check) by their class:

```java
Taikai.builder()
    .namespace("com.company.project")
    .discoverChecks(checks -> checks
        .configure(RequiredFilesCheck.class, RequiredFilesCheck.files("README.md", "LICENSE"))
        .configure(ForbiddenFilesCheck.class,
            ForbiddenFilesCheck.files("src/main/**/*.key", "src/main/**/.env"))
        .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
    .build()
    .checkAll();
```

| Check | Checks that | Configuration |
|-------|-------------|---------------|
| [`ClassFileVersionCheck`](#ClassFileVersionCheck) | classes are not compiled for a newer Java version than allowed | required |
| [`ForbiddenFilesCheck`](#ForbiddenFilesCheck) | the project contains no forbidden files, such as private keys | required |
| [`GradleWrapperCheck`](#GradleWrapperCheck) | the project has the Gradle Wrapper | none |
| [`MavenWrapperCheck`](#MavenWrapperCheck) | the project has the Maven Wrapper | none |
| [`RequiredFilesCheck`](#RequiredFilesCheck) | the project has the required files, none of them empty | required |
| [`ServiceRegistrationsCheck`](#ServiceRegistrationsCheck) | the `META-INF/services` registrations refer to valid providers | optional |

The checks that look at files resolve them against the [project directory](#configuring-the-checks),
which is the directory of the module under test. In a multi-module build, files such as the README
or the Maven or Gradle Wrapper usually lie in the root directory instead. Point the checks there
with `discoverChecks(checks -> checks.projectDirectory(Path.of("..")))`, or a single check with
[`context(RequiredFilesCheck.class, ...)`](#changing-the-context).

The registrations of [`ServiceRegistrationsCheck`](#ServiceRegistrationsCheck) lie in the module
itself, so when all checks point at the root directory,
[configure](#ServiceRegistrationsCheck) its directories relative to the root.

#### `ClassFileVersionCheck` { #ClassFileVersionCheck }

*Classes should not be compiled for a newer Java version than configured*

Reads the Java version from the header of every class file of the configuration and reports the
classes compiled for a newer Java version than allowed. Classes imported from the namespace exclude
test classes and the [globally excluded classes](#54-excluding-classes-globally); classes set with
[`classes(...)`](#52-selecting-classes-directly) are checked as given. Use it to keep a library usable on
older Java versions, even when the build runs on a newer JDK. The check needs the newest allowed
Java version:

```java
.discoverChecks(checks -> checks
    .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
```

```text
Check: Classes should not be compiled for a newer Java version than configured
	Class com.company.project.OrderService is compiled for Java 21, but at most Java 17 is allowed
```

#### `ForbiddenFilesCheck` { #ForbiddenFilesCheck }

*Project should not contain forbidden files*

Reports files that must not be in the project, for example private keys or `.env` files. The files
are glob patterns relative to the project directory, as in `FileSystem.getPathMatcher(String)`. As
in `.gitignore`, `**/` also matches no directory, so `**/.env` also finds the `.env` in the project
directory itself.

The check needs the patterns of the forbidden files. Patterns in `src/main` are a good start,
because everything in there ends up in the artifact, whether it is committed or not: a key that
`.gitignore` keeps out of the repository would still be packaged from there. `src/test`, on the
other hand, often contains test keystores on purpose. A configuration for private keys and
certificates, `.env` files, and compiled or packaged files could look like this:

```java
.discoverChecks(checks -> checks
    .configure(ForbiddenFilesCheck.class, ForbiddenFilesCheck.files(
        "src/main/**/*.pem", "src/main/**/*.key", "src/main/**/*.p12", "src/main/**/*.jks",
        "src/main/**/.env", "src/main/**/*.class", "src/main/**/*.jar")))
```

Only the directory before the first wildcard of a pattern is searched, and only as deep as the
pattern reaches, so `src/main/**/*.key` does not look into `target` or `.git`. A pattern such as
`**/.env` searches the whole project directory, including files that `.gitignore` keeps out of the
repository, such as a local `.env`.

```text
Check: Project should not contain forbidden files
	src/main/resources/server.key is forbidden (matches src/main/**/*.key)
```

#### `GradleWrapperCheck` { #GradleWrapperCheck }

*Project should have the Gradle Wrapper*

Checks that the project can be built without a local Gradle installation: the scripts `gradlew`
and `gradlew.bat` and the files `gradle/wrapper/gradle-wrapper.jar` and
`gradle/wrapper/gradle-wrapper.properties` must exist in the project directory and must not be
empty.

```text
Check: Project should have the Gradle Wrapper
	gradle/wrapper/gradle-wrapper.jar is missing in /home/dev/project
```

#### `MavenWrapperCheck` { #MavenWrapperCheck }

*Project should have the Maven Wrapper*

Checks that the project can be built without a local Maven installation: the scripts `mvnw` and
`mvnw.cmd` and the file `.mvn/wrapper/maven-wrapper.properties` must exist in the project
directory and must not be empty.

```text
Check: Project should have the Maven Wrapper
	mvnw.cmd is missing in /home/dev/project
```

#### `RequiredFilesCheck` { #RequiredFilesCheck }

*Project should have the required files*

Reports required files that are missing in the project directory or empty; a directory with the
name of a required file counts as missing. The check needs the required files; their paths are
relative to the project directory and may point into subdirectories:

```java
.discoverChecks(checks -> checks
    .configure(RequiredFilesCheck.class,
        RequiredFilesCheck.files("README.md", "LICENSE", "SECURITY.md", ".github/CODEOWNERS")))
```

```text
Check: Project should have the required files
	/home/dev/project/LICENSE is empty
	SECURITY.md is missing in /home/dev/project
```

#### `ServiceRegistrationsCheck` { #ServiceRegistrationsCheck }

*Service registrations should refer to valid providers*

Checks the `ServiceLoader` registrations of the project, the files in
`src/main/resources/META-INF/services` and `src/test/resources/META-INF/services` of the project
directory. Every registered service must exist, and every provider must exist, implement the
service, and be a public class with a public constructor without parameters, as the
`ServiceLoader` requires. Comments and blank lines are ignored, and the classes are loaded with the
class loader of the context without being initialized. Taikai uses it to validate its own
registration of the predefined checks.

The check needs no configuration. A project directory without these directories has nothing to
check, so the check passes. When the registrations lie elsewhere, for example in a module while
the [project directory](#configuring-the-checks) is the root of a multi-module build, configure
the directories instead, relative to the project directory. Unlike the default directories, a
configured directory must exist, so a wrong path is reported rather than silently checking nothing:

```java
.discoverChecks(checks -> checks
    .projectDirectory(Path.of(".."))
    .configure(ServiceRegistrationsCheck.class, ServiceRegistrationsCheck.directories(
        "my-module/src/test/resources/META-INF/services")))
```

```text
Check: Service registrations should refer to valid providers
	Provider com.company.checks.LicenceCheck registered in src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck does not exist
```

## 14. Recommended Starter Configuration

A reasonable baseline for a new Spring Boot project. It is deliberately conservative: every rule
here is one most teams already agree on. Add the stricter rules once this passes.

```java
class ArchitectureTest {

  @Test
  void architecture() {
    Taikai.builder()
        .namespace("com.company.project")
        .failOnEmpty(true)
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .fieldsShouldNotBePublic()
            .finalClassesShouldNotHaveProtectedMembers()
            .methodsShouldNotDeclareGenericExceptions()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldImplementHashCodeAndEquals()
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport(ImportPatterns.shaded()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .classesShouldNotMatch(".*Impl")
                .constantsShouldFollowConventions()
                .enumConstantsShouldFollowConventions()
                .interfacesShouldNotHavePrefixI()))
        .logging(logging -> logging
            .loggersShouldFollowConventions(Logger.class, "logger", List.of(PRIVATE, FINAL)))
        .test(test -> test
            .junit(junit -> junit
                .classesShouldEndWithTest()
                .classesShouldBePackagePrivate(ClassCriteria.nameMatching(".*Test"))
                .classesShouldNotBeAnnotatedWithDisabled()
                .methodsShouldBePackagePrivate()
                .methodsShouldNotBeAnnotatedWithDisabled()
                .methodsShouldNotDeclareExceptions()
                .methodsShouldContainAssertionsOrVerifications()
                .testsShouldBeExecutable()))
        .spring(spring -> spring
            .noAutowiredFields()
            .noInjectionIntoStaticFields()
            .noSelfInvocationOfProxiedMethods()
            .boot(boot -> boot
                .applicationClassShouldResideInPackage())
            .configurations(configurations -> configurations
                .namesShouldEndWithConfiguration())
            .controllers(controllers -> controllers
                .namesShouldEndWithController()
                .shouldBeAnnotatedWithRestController()
                .shouldBePackagePrivate()
                .shouldNotDependOnOtherControllers()
                .shouldNotDependOnRepositories())
            .services(services -> services
                .namesShouldEndWithService()
                .shouldBeAnnotatedWithService()
                .shouldNotDependOnControllers())
            .repositories(repositories -> repositories
                .namesShouldEndWithRepository()
                .shouldBeAnnotatedWithRepository()
                .shouldNotDependOnServices()
                .shouldNotDependOnControllers())
            .transactional(transactional -> transactional
                .methodsShouldBePublic()
                .shouldNotBeSelfInvoked()
                .shouldNotBeUsedInControllers()))
        .build()
        .checkAll();
  }
}
```

Adopting this on an existing codebase will usually produce violations. Two ways to stage the
rollout:

- Run `checkAll()` to get the full list, then add the offenders to
  [`excludeClasses`](#54-excluding-classes-globally) or a per-rule
  [`Configuration`](#58-per-rule-configuration) and work the list down.
- Enable the rule groups one at a time, committing each as it goes green.

## 15. Complete Example

The configuration Taikai applies to its own codebase, from
[`ArchitectureTest.java`](https://github.com/enofex/taikai/blob/main/taikai-core/src/test/java/com/enofex/taikai/ArchitectureTest.java).
Every rule and check runs as a test of its own:

```java
import static com.enofex.taikai.java.ImportPatterns.lombok;
import static com.enofex.taikai.java.ImportPatterns.shaded;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

class ArchitectureTest {

  @DisplayName("Should fulfill architecture constraints")
  @TestFactory
  Stream<DynamicTest> architecture() {
    return Taikai.builder()
        .namespace("com.enofex.taikai")
        .java(java -> java
            .noUsageOfDeprecatedAPIs()
            .noUsageOfSystemOutOrErr()
            .noUsageOf(Date.class)
            .noUsageOf(Calendar.class)
            .noUsageOf(SimpleDateFormat.class)
            .fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"),
                List.of(STATIC, FINAL))
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions()
            .fieldsShouldNotBePublic()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldResideInPackage("com.enofex.taikai..")
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport(ClassCriteria.inPackage("org.springframework.core.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("jakarta.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("javax.annotation.."))
                .shouldNotImport(ClassCriteria.inPackage("org.jetbrains.annotations.."))
                .shouldNotImport(shaded())
                .shouldNotImport(lombok()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                .classesShouldNotMatch(".*Impl")
                .classesShouldMatch(ClassCriteria.assignableTo(AbstractConfigurer.class),
                    ".*Configurer")
                .classesShouldMatch(ClassCriteria.implementing(Configurer.class), ".*Configurer")
                .classesShouldMatch(ClassCriteria.implementing(TaikaiCheck.class), ".*Check")
                .classesShouldMatch(ClassCriteria.implementing(CheckContext.class),
                    ".*CheckContext")
                .interfacesShouldNotHavePrefixI()
                .enumConstantsShouldFollowConventions()
                .constantsShouldFollowConventions()))
        .discoverChecks(checks -> checks
            .projectDirectory(Path.of(".."))
            .configure(ServiceRegistrationsCheck.class, ServiceRegistrationsCheck.directories(
                "taikai-core/src/test/resources/META-INF/services"))
            .configure(RequiredFilesCheck.class, RequiredFilesCheck.files(
                "README.md", "LICENSE", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "SECURITY.md"))
            .configure(ForbiddenFilesCheck.class, ForbiddenFilesCheck.files(
                "taikai-core/src/main/**/*.pem", "taikai-core/src/main/**/*.key",
                "taikai-core/src/main/**/*.p12", "taikai-core/src/main/**/*.jks",
                "taikai-core/src/main/**/.env", "taikai-core/src/main/**/*.class",
                "taikai-core/src/main/**/*.jar"))
            .configure(ClassFileVersionCheck.class, ClassFileVersionCheck.javaVersion(17)))
        .build()
        .dynamicTests();
  }
}
```

The [predefined checks](#13-predefined-checks) are registered in
`src/test/resources/META-INF/services/com.enofex.taikai.checks.spi.TaikaiCheck`:

```text
com.enofex.taikai.checks.ClassFileVersionCheck
com.enofex.taikai.checks.ForbiddenFilesCheck
com.enofex.taikai.checks.MavenWrapperCheck
com.enofex.taikai.checks.RequiredFilesCheck
com.enofex.taikai.checks.ServiceRegistrationsCheck
```

See the [Spring Java demo](https://github.com/enofex/taikai/tree/main/examples/spring-java)
for an HTTP application with its architecture tests and project checks.


## Kotlin/JVM

Taikai supports Kotlin callers and compiled Kotlin/JVM applications. The optional
`taikai-kotlin` artifact adds source-aware rules and a receiver DSL without adding
Kotlin dependencies to core. See the [Kotlin manual](kotlin.md) for installation,
all rules, Java rule compatibility, and the Spring Kotlin demo.
