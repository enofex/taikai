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
  void shouldFulfillConstraints() {
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
  void shouldFulfillConstraints() {
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

| Rule group                                                     | Default mode    |
|----------------------------------------------------------------|-----------------|
| `java(...)`, `logging(...)`, `spring(...)`, `quarkus(...)`      | `WITHOUT_TESTS` |
| `test(...)` / `junit(...)`                                      | `ONLY_TESTS`    |

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

Both methods evaluate every configured rule; they differ in how they report.

| Method       | Behavior                                                                        |
|--------------|----------------------------------------------------------------------------------|
| `check()`    | Stops at the first violated rule and throws immediately.                        |
| `checkAll()` | Evaluates all rules, aggregates every violation, then throws one combined report. |

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

### 5.7 Reading the Failure Report

`check()` propagates ArchUnit's own `AssertionError` for the first failing rule.

`checkAll()` produces an aggregated report with a violation count, a rule count, and the individual
violations grouped per rule:

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
`quarkus`, `resources`, `panache` and `ai`.

`disable()` cascades to nested configurers: `java(...)` also clears `naming` and `imports`,
`spring(...)` clears all its sub-configurers, `quarkus(...)` clears `resources`, `panache` and `ai`,
and `test(...)` clears `junit`.

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

## 6. How to Read the Rule Reference

Sections 7 to 11 document every rule. To keep them readable, examples are shown as **fragments**
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
- Wherever a rule takes an annotation or a type, there are two overloads: one taking a
  `Class<?>` literal and one taking a fully qualified name as a `String`. The `String` form exists so
  you can reference types that are not on the test classpath. Only one form is shown per rule.
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
| [`methodsShouldNotExceedMaxParameters`](#methodsShouldNotExceedMaxParameters) | parameter count stays under a limit |
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

Classes whose methods are all `static` must be `final` and expose only a private constructor, so they
can neither be extended nor instantiated.

A class declaring a `main` method is not treated as a utility class and is left alone entirely, so
application entry points do not need a private constructor.

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
| [`shouldHaveNoCycles`](#imports-shouldHaveNoCycles) | no cyclic package dependencies |
| [`shouldImport`](#imports-shouldImport) | matching classes import something |
| [`shouldNotImport`](#imports-shouldNotImport) | a package or import is forbidden |

#### `shouldHaveNoCycles` { #imports-shouldHaveNoCycles }

Fails on cyclic dependencies between the slices of your namespace.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldHaveNoCycles()))
```

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
| [`fieldsShouldMatch`](#naming-fieldsShouldMatch) | fields of a type match a regex |
| [`fieldsShouldNotMatch`](#naming-fieldsShouldNotMatch) | field names do not match a regex |
| [`fieldsShouldMatchForClass`](#naming-fieldsShouldMatchForClass) | fields of matching classes match a regex |
| [`fieldsShouldNotMatchForClass`](#naming-fieldsShouldNotMatchForClass) | fields of matching classes do not match a regex |
| [`constantsShouldFollowConventions`](#naming-constantsShouldFollowConventions) | constants are `UPPER_SNAKE_CASE` |
| [`enumConstantsShouldFollowConventions`](#naming-enumConstantsShouldFollowConventions) | enum constants are `UPPER_SNAKE_CASE` |
| [`booleanMethodsShouldStartWith`](#naming-booleanMethodsShouldStartWith) | boolean getters use a prefix |
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
| [`classesShouldUseLogger`](#classesShouldUseLogger) | matching classes declare a logger |

#### `loggersShouldFollowConventions` { #loggersShouldFollowConventions }

`loggersShouldFollowConventions(Class<?> clazz, String regex)`
`loggersShouldFollowConventions(Class<?> clazz, String regex, Collection<JavaModifier> requiredModifiers)`

Fields of the given logger type must have a name matching the regex and, optionally, the listed
modifiers.

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

Classes matching the criteria must declare a field of the given logger type. This is the
complement of the rule above: one fixes *how* loggers are declared, the other *that* they are
declared.

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
| [`methodsShouldMatch`](#junit-methodsShouldMatch) | test method names match a regex |
| [`methodsShouldBePackagePrivate`](#junit-methodsShouldBePackagePrivate) | test methods are package-private |
| [`methodsShouldBeAnnotatedWithDisplayName`](#junit-methodsShouldBeAnnotatedWithDisplayName) | test methods carry `@DisplayName` |
| [`methodsShouldNotBeAnnotatedWithDisabled`](#junit-methodsShouldNotBeAnnotatedWithDisabled) | no `@Disabled` methods |
| [`methodsShouldNotDeclareExceptions`](#junit-methodsShouldNotDeclareExceptions) | test methods declare no `throws` |
| [`methodsShouldContainAssertionsOrVerifications`](#junit-methodsShouldContainAssertionsOrVerifications) | tests actually assert something |

Throughout this section, "test method" means a method meta-annotated `@Test` or
`@ParameterizedTest`, and "test class" a class containing such methods.

Three rules select differently, and the difference matters because these rules run over the
`ONLY_TESTS` import, where every imported class is a test class anyway:

- `classesShouldBePackagePrivate(classes)` selects by criteria, and skips interfaces.
- `classesShouldNotBeAnnotatedWithDisabled` applies to *all* imported classes.
- `methodsShouldNotBeAnnotatedWithDisabled` applies to *all* imported methods, so it also catches
  `@Disabled` on a `@BeforeEach` or a helper.

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
the global `namespace` otherwise:

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

Applies to classes annotated `@Configuration`.

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
parameter annotated `@PathVariable` or `@RequestParam` that also carries a validation constraint such
as `@Min` or `@NotNull`. Without `@Validated` on the class, Spring never evaluates those constraints.
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

## 12. Recommended Starter Configuration

A reasonable baseline for a new Spring Boot project. It is deliberately conservative: every rule
here is one most teams already agree on. Add the stricter rules once this passes.

```java
class ArchitectureTest {

  @Test
  void shouldFulfillConstraints() {
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
                .methodsShouldContainAssertionsOrVerifications()))
        .spring(spring -> spring
            .noAutowiredFields()
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

## 13. Complete Example

The configuration Taikai applies to its own codebase, from
[`ArchitectureTest.java`](https://github.com/enofex/taikai/blob/main/src/test/java/com/enofex/taikai/ArchitectureTest.java):

```java
import static com.enofex.taikai.java.ImportPatterns.lombok;
import static com.enofex.taikai.java.ImportPatterns.shaded;
import static com.tngtech.archunit.core.domain.JavaModifier.FINAL;
import static com.tngtech.archunit.core.domain.JavaModifier.STATIC;

class ArchitectureTest {

  @Test
  void shouldFulfillConstraints() {
    Taikai.builder()
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
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConventions()))
        .build()
        .checkAll();
  }
}
```
