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
| ArchUnit    | 1.5.0        |

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
                .shouldNotImport("..internal.."))
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

`namespace` is the base package Taikai imports and analyses.

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

To exclude classes from a single rule only, use [`Configuration`](#58-per-rule-configuration) instead.

!!! warning
    Exclusions are only applied on the namespace path, so they take effect only when the classes
    come from `namespace(...)`. If you supply the classes yourself — globally via
    [`classes(...)`](#52-selecting-classes-directly) or per rule via
    `Configuration.of(JavaClasses)` — that set is used verbatim and `excludeClasses(...)` is
    silently ignored. Filter the `JavaClasses` before handing them over instead.

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

| Method       | Behaviour                                                                       |
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
        .shouldNotImport("..internal..",
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

Most configurers implement `disable()`, which clears all rules registered on that configurer. This
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

`disable()` is available on `java`, `logging`, `test`, `junit`, `spring`, `boot`, `properties`,
`configurations`, `controllers`, `services`, `repositories`, `transactional`, `quarkus`, `resources`,
`panache` and `ai`.

The two nested Java configurers, `naming(...)` and `imports(...)`, do **not** have `disable()` —
they are the only rule groups without it.

`disable()` cascades to nested configurers: `java(...)` also clears `naming` and `imports`,
`spring(...)` clears all its sub-configurers, `quarkus(...)` clears `resources`, `panache` and `ai`,
and `test(...)` clears `junit`. So disabling `java(...)` is how you drop naming and import rules a
profile added.

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

Two sharp edges in the current implementation:

- The derived builder shares the exclusion list with the source instance, so `excludeClasses(...)`
  on `adapted` also adds to `base`. Do not reuse `base` for a separate check afterwards.
- `addRule(...)` and `addRules(...)` throw `UnsupportedOperationException` on a builder obtained
  from `toBuilder()`, because the copied rule list is immutable. Register custom rules on the
  original builder instead.

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
        .classesShouldBePackagePrivate(".*Test")
        .classesShouldNotBeAnnotatedWithDisabled());
```

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(java -> {
      DEFAULT_JAVA_PROFILE.customize(java);          // apply the profile
      java.classesShouldBeRecords(".*Dto");          // plus a local rule
    })
    .test(DEFAULT_TEST_PROFILE)                      // use the profile directly
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

## 6. How to Read the Rule Reference

Sections 7 to 11 document every rule. To keep them readable, examples are shown as **fragments**
rather than complete tests. A fragment such as

```java
.java(java -> java
    .classesShouldBeRecords(".*Dto"))
```

belongs inside the builder chain from [Section 4](#4-quick-start):

```java
Taikai.builder()
    .namespace("com.company.project")
    .java(java -> java
        .classesShouldBeRecords(".*Dto"))
    .build()
    .check();
```

Conventions used throughout the reference:

- Rules are fluent and chainable; each returns its configurer.
- Wherever a rule takes an annotation or a type, there are two overloads: one taking a
  `Class<?>` literal and one taking a fully qualified name as a `String`. The `String` form exists so
  you can reference types that are not on the test classpath. Only one form is shown per rule.
- Every rule also has an overload with a trailing `TaikaiRule.Configuration` parameter
  (see [5.8](#58-per-rule-configuration)). It is omitted from the signatures below.
- `Collection<JavaModifier>` parameters take ArchUnit's
  `com.tngtech.archunit.core.domain.JavaModifier` constants, for example
  `List.of(PUBLIC, STATIC, FINAL)`.
- A *regex* must match the **whole** name, not merely occur inside it.
- For **classes**, a regex is matched against the **fully qualified** name, so anchor it with a
  leading `.*`: `.*Dto` matches `com.company.project.OrderDto`, while `Dto` and `[A-Z].*` match
  nothing at all.
- For **methods and fields**, a regex is matched against the member's own name, without the
  declaring class: `should.*`, `^[A-Z][A-Z0-9_]*$`.
- A *packageIdentifier* is not a regex but an ArchUnit package identifier, matched against the
  fully qualified name with `*` for one segment and `..` for any number:
  `com.company.project..`, `..internal..`.

!!! warning
    Two rules break the pattern above: [`classesShouldBeAssignableTo`](#classesShouldBeAssignableTo)
    and [`classesShouldImplement`](#classesShouldImplement) take a **literal simple-name suffix**,
    not a regex, despite the parameter being named `regex`. Pass `"Repository"`, not
    `".*Repository"` — the latter selects nothing and the rule passes silently.

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
| [`classesShouldBeAnnotatedWith`](#classesShouldBeAnnotatedWith) | matching classes carry an annotation |
| [`classesShouldNotBeAnnotatedWith`](#classesShouldNotBeAnnotatedWith) | matching classes lack an annotation |
| [`classesShouldBeAnnotatedWithAll`](#classesShouldBeAnnotatedWithAll) | annotated classes carry further annotations |
| [`classesAnnotatedWithShouldResideInPackage`](#classesAnnotatedWithShouldResideInPackage) | annotated classes live in a package |
| [`classesAnnotatedWithShouldNotBeAnnotatedWith`](#classesAnnotatedWithShouldNotBeAnnotatedWith) | annotations are mutually exclusive |
| [`classesAnnotatedWithShouldHaveModifiers`](#classesAnnotatedWithShouldHaveModifiers) | annotated classes carry modifiers |
| [`classesAnnotatedWithShouldNotHaveModifiers`](#classesAnnotatedWithShouldNotHaveModifiers) | annotated classes lack modifiers |
| [`classesAnnotatedWithShouldBeRecords`](#classesAnnotatedWithShouldBeRecords) | annotated classes are records |
| [`finalClassesShouldNotHaveProtectedMembers`](#finalClassesShouldNotHaveProtectedMembers) | `final` classes have no `protected` members |
| [`utilityClassesShouldBeFinalAndHavePrivateConstructor`](#utilityClassesShouldBeFinalAndHavePrivateConstructor) | utility classes cannot be instantiated or extended |
| [`fieldsShouldNotBePublic`](#fieldsShouldNotBePublic) | no `public` fields except `static` ones |
| [`fieldsShouldHaveModifiers`](#fieldsShouldHaveModifiers) | matching fields carry modifiers |
| [`fieldsShouldNotHaveModifiers`](#fieldsShouldNotHaveModifiers) | matching fields lack modifiers |
| [`fieldsAnnotatedWithShouldHaveModifiers`](#fieldsAnnotatedWithShouldHaveModifiers) | annotated fields carry modifiers |
| [`fieldsAnnotatedWithShouldNotHaveModifiers`](#fieldsAnnotatedWithShouldNotHaveModifiers) | annotated fields lack modifiers |
| [`serialVersionUIDFieldsShouldBeStaticFinalLong`](#serialVersionUIDFieldsShouldBeStaticFinalLong) | `serialVersionUID` is `static final long` |
| [`methodsShouldNotDeclareGenericExceptions`](#methodsShouldNotDeclareGenericExceptions) | no `throws Exception` / `RuntimeException` |
| [`methodsShouldNotDeclareException`](#methodsShouldNotDeclareException) | matching methods do not declare a type |
| [`methodsShouldBeAnnotatedWith`](#methodsShouldBeAnnotatedWith) | matching methods carry an annotation |
| [`methodsShouldBeAnnotatedWithAll`](#methodsShouldBeAnnotatedWithAll) | annotated methods carry further annotations |
| [`methodsAnnotatedWithShouldNotBeAnnotatedWith`](#methodsAnnotatedWithShouldNotBeAnnotatedWith) | annotations are mutually exclusive |
| [`methodsAnnotatedWithShouldHaveModifiers`](#methodsAnnotatedWithShouldHaveModifiers) | annotated methods carry modifiers |
| [`methodsAnnotatedWithShouldNotHaveModifiers`](#methodsAnnotatedWithShouldNotHaveModifiers) | annotated methods lack modifiers |
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
`classesShouldResideInPackage(String regex, String packageIdentifier)`

With one argument, *all* imported classes must reside in the given package identifier, which
supports the `*` and `..` wildcards. With two arguments, only classes whose name matches the regex
are constrained.

```java
.java(java -> java
    .classesShouldResideInPackage("com.company.project..")
    .classesShouldResideInPackage(".*Utils", "com.company.project.utils"))
```

#### `classesShouldResideOutsidePackage` { #classesShouldResideOutsidePackage }

`classesShouldResideOutsidePackage(String regex, String packageIdentifier)`

The inverse of the two-argument form above. Useful for keeping transport types out of the domain.

```java
.java(java -> java
    .classesShouldResideOutsidePackage(".*Dto", "com.company.project.domain"))
```

#### `classesShouldBeRecords` { #classesShouldBeRecords }

`classesShouldBeRecords(String regex)`

```java
.java(java -> java
    .classesShouldBeRecords(".*Dto"))
```

#### `classesShouldBeInterfaces` { #classesShouldBeInterfaces }

`classesShouldBeInterfaces(String regex)`

```java
.java(java -> java
    .classesShouldBeInterfaces(".*Repository"))
```

#### `classesShouldBeAssignableTo` { #classesShouldBeAssignableTo }

`classesShouldBeAssignableTo(String suffix, Class<?> clazz)`

Classes whose **simple name ends with** the given literal suffix must be assignable to the type,
whether by extending a class or implementing an interface.

```java
.java(java -> java
    .classesShouldBeAssignableTo("Repository", BaseRepository.class))
```

!!! warning
    The first parameter is declared as `regex` but is compared literally, as a simple-name suffix.
    Passing `".*Repository"` selects no class at all and the rule passes without checking anything.

#### `classesShouldImplement` { #classesShouldImplement }

`classesShouldImplement(String suffix, Class<?> clazz)`

Stricter than `classesShouldBeAssignableTo`: the class must implement the given interface directly.
The first parameter is the same literal simple-name suffix, not a regex.

```java
.java(java -> java
    .classesShouldImplement("Repository", CrudRepository.class))
```

#### `classesShouldHaveModifiers` { #classesShouldHaveModifiers }

`classesShouldHaveModifiers(String regex, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .classesShouldHaveModifiers(".*Config", List.of(PUBLIC, FINAL)))
```

#### `classesShouldNotHaveModifiers` { #classesShouldNotHaveModifiers }

`classesShouldNotHaveModifiers(String regex, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .classesShouldNotHaveModifiers(".*Internal", List.of(PUBLIC)))
```

#### `classesShouldBeAnnotatedWith` { #classesShouldBeAnnotatedWith }

`classesShouldBeAnnotatedWith(String regex, Class<? extends Annotation> annotationType)`

```java
.java(java -> java
    .classesShouldBeAnnotatedWith(".*Api", PublicApi.class)
    .classesShouldBeAnnotatedWith(".*Api", "com.company.project.PublicApi"))
```

#### `classesShouldNotBeAnnotatedWith` { #classesShouldNotBeAnnotatedWith }

`classesShouldNotBeAnnotatedWith(String regex, Class<? extends Annotation> annotationType)`

```java
.java(java -> java
    .classesShouldNotBeAnnotatedWith(".*Internal", PublicApi.class))
```

#### `classesShouldBeAnnotatedWithAll` { #classesShouldBeAnnotatedWithAll }

`classesShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType, Collection<Class<? extends Annotation>> requiredAnnotationTypes)`

Classes carrying the first annotation must also carry all the annotations in the collection.

```java
.java(java -> java
    .classesShouldBeAnnotatedWithAll(RestController.class, List.of(RequestMapping.class)))
```

#### `classesAnnotatedWithShouldResideInPackage` { #classesAnnotatedWithShouldResideInPackage }

`classesAnnotatedWithShouldResideInPackage(Class<? extends Annotation> annotationType, String packageIdentifier)`

```java
.java(java -> java
    .classesAnnotatedWithShouldResideInPackage(PublicApi.class, "com.company.project.api"))
```

#### `classesAnnotatedWithShouldNotBeAnnotatedWith` { #classesAnnotatedWithShouldNotBeAnnotatedWith }

`classesAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType, Class<? extends Annotation> notAnnotationType)`

Declares two annotations mutually exclusive.

```java
.java(java -> java
    .classesAnnotatedWithShouldNotBeAnnotatedWith(PublicApi.class, InternalApi.class))
```

#### `classesAnnotatedWithShouldHaveModifiers` { #classesAnnotatedWithShouldHaveModifiers }

`classesAnnotatedWithShouldHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .classesAnnotatedWithShouldHaveModifiers(PublicApi.class, List.of(PUBLIC, FINAL)))
```

#### `classesAnnotatedWithShouldNotHaveModifiers` { #classesAnnotatedWithShouldNotHaveModifiers }

`classesAnnotatedWithShouldNotHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .classesAnnotatedWithShouldNotHaveModifiers(InternalApi.class, List.of(PUBLIC)))
```

#### `classesAnnotatedWithShouldBeRecords` { #classesAnnotatedWithShouldBeRecords }

`classesAnnotatedWithShouldBeRecords(Class<? extends Annotation> annotationType)`

```java
.java(java -> java
    .classesAnnotatedWithShouldBeRecords(ConfigurationProperties.class))
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
    .fieldsShouldHaveModifiers("^[A-Z][A-Z0-9_]*$", List.of(STATIC, FINAL)))
```

#### `fieldsShouldHaveModifiers` { #fieldsShouldHaveModifiers }

`fieldsShouldHaveModifiers(String regex, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .fieldsShouldHaveModifiers("^[A-Z][A-Z0-9_]*$", List.of(STATIC, FINAL)))
```

#### `fieldsShouldNotHaveModifiers` { #fieldsShouldNotHaveModifiers }

`fieldsShouldNotHaveModifiers(String regex, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .fieldsShouldNotHaveModifiers(".*Cache", List.of(PUBLIC)))
```

#### `fieldsAnnotatedWithShouldHaveModifiers` { #fieldsAnnotatedWithShouldHaveModifiers }

`fieldsAnnotatedWithShouldHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .fieldsAnnotatedWithShouldHaveModifiers(Constant.class, List.of(PUBLIC, STATIC, FINAL)))
```

#### `fieldsAnnotatedWithShouldNotHaveModifiers` { #fieldsAnnotatedWithShouldNotHaveModifiers }

`fieldsAnnotatedWithShouldNotHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .fieldsAnnotatedWithShouldNotHaveModifiers(Autowired.class, List.of(STATIC)))
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

`methodsShouldNotDeclareException(String regex, Class<? extends Throwable> clazz)`

```java
.java(java -> java
    .methodsShouldNotDeclareException("should.*", SpecificException.class))
```

#### `methodsShouldBeAnnotatedWith` { #methodsShouldBeAnnotatedWith }

`methodsShouldBeAnnotatedWith(String regex, Class<? extends Annotation> annotationType)`

```java
.java(java -> java
    .methodsShouldBeAnnotatedWith(".*Api", PublicApi.class))
```

#### `methodsShouldBeAnnotatedWithAll` { #methodsShouldBeAnnotatedWithAll }

`methodsShouldBeAnnotatedWithAll(Class<? extends Annotation> annotationType, Collection<Class<? extends Annotation>> requiredAnnotationTypes)`

Methods carrying the first annotation must also carry all the annotations in the collection. A
classic use is Spring Data: `@Modifying` is only correct together with `@Transactional` and `@Query`.

```java
.java(java -> java
    .methodsShouldBeAnnotatedWithAll(Modifying.class, List.of(Transactional.class, Query.class)))
```

#### `methodsAnnotatedWithShouldNotBeAnnotatedWith` { #methodsAnnotatedWithShouldNotBeAnnotatedWith }

`methodsAnnotatedWithShouldNotBeAnnotatedWith(Class<? extends Annotation> annotationType, Class<? extends Annotation> notAnnotationType)`

```java
.java(java -> java
    .methodsAnnotatedWithShouldNotBeAnnotatedWith(PublicApi.class, InternalApi.class))
```

#### `methodsAnnotatedWithShouldHaveModifiers` { #methodsAnnotatedWithShouldHaveModifiers }

`methodsAnnotatedWithShouldHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> requiredModifiers)`

```java
.java(java -> java
    .methodsAnnotatedWithShouldHaveModifiers(Transactional.class, List.of(PUBLIC)))
```

#### `methodsAnnotatedWithShouldNotHaveModifiers` { #methodsAnnotatedWithShouldNotHaveModifiers }

`methodsAnnotatedWithShouldNotHaveModifiers(Class<? extends Annotation> annotationType, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .methodsAnnotatedWithShouldNotHaveModifiers(Test.class, List.of(PUBLIC)))
```

#### `methodsShouldHaveModifiers` { #methodsShouldHaveModifiers }

`methodsShouldHaveModifiers(String regex, Collection<JavaModifier> requiredModifiers)`

Selects methods by *method* name.

```java
.java(java -> java
    .methodsShouldHaveModifiers("create.*", List.of(PUBLIC)))
```

#### `methodsShouldNotHaveModifiers` { #methodsShouldNotHaveModifiers }

`methodsShouldNotHaveModifiers(String regex, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .methodsShouldNotHaveModifiers("internal.*", List.of(PUBLIC)))
```

#### `methodsShouldHaveModifiersForClass` { #methodsShouldHaveModifiersForClass }

`methodsShouldHaveModifiersForClass(String regex, Collection<JavaModifier> requiredModifiers)`

Selects methods by the *declaring class* name, so every method of a matching class is constrained.

```java
.java(java -> java
    .methodsShouldHaveModifiersForClass(".*Controller", List.of(PUBLIC)))
```

#### `methodsShouldNotHaveModifiersForClass` { #methodsShouldNotHaveModifiersForClass }

`methodsShouldNotHaveModifiersForClass(String regex, Collection<JavaModifier> notRequiredModifiers)`

```java
.java(java -> java
    .methodsShouldNotHaveModifiersForClass(".*Internal", List.of(PUBLIC)))
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
`noUsageOf(Class<?> clazz, String packageIdentifier)`

Forbids all usage of a type. The second parameter narrows the ban to classes inside a given package,
which lets you retire an API layer by layer.

```java
.java(java -> java
    .noUsageOf(Date.class)
    .noUsageOf(Calendar.class, "com.company.project.domain..")
    .noUsageOf("com.company.legacy.UnwantedClass"))
```

#### `noUsageOfDeprecatedAPIs` { #noUsageOfDeprecatedAPIs }

Nothing annotated `@Deprecated` may be used.

```java
.java(java -> java
    .noUsageOfDeprecatedAPIs())
```

#### `noUsageOfSystemOutOrErr` { #noUsageOfSystemOutOrErr }

Forbids `System.out` and `System.err` in favour of a logging framework.

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

`shouldImport(String regex, String importClassesRegex)`

Classes matching the first regex must import something matching the second.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldImport(".*Service", "com.company.project.BusinessException")))
```

#### `shouldNotImport` { #imports-shouldNotImport }

`shouldNotImport(String packageIdentifier)`
`shouldNotImport(String regex, String notImportClassesRegex)`

With one argument, nothing may import the given package identifier. With two, only classes matching
the first regex are constrained.

```java
.java(java -> java
    .imports(imports -> imports
        .shouldNotImport("..internal..")
        .shouldNotImport(".*Service", "com.company.project.SpecificException")))
```

#### Predefined import patterns

`com.enofex.taikai.java.ImportPatterns` provides constants for packages that are commonly banned
from production code or from a particular layer. They are plain `String` package identifiers, so they
work anywhere a package identifier is accepted.

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
| [`classesAnnotatedWithShouldMatch`](#naming-classesAnnotatedWithShouldMatch) | annotated classes match a regex |
| [`classesImplementingShouldMatch`](#naming-classesImplementingShouldMatch) | implementors match a regex |
| [`classesAssignableToShouldMatch`](#naming-classesAssignableToShouldMatch) | subtypes match a regex |
| [`methodsShouldMatch`](#naming-methodsShouldMatch) | method names match a regex |
| [`methodsShouldNotMatch`](#naming-methodsShouldNotMatch) | method names do not match a regex |
| [`methodsAnnotatedWithShouldMatch`](#naming-methodsAnnotatedWithShouldMatch) | annotated methods match a regex |
| [`fieldsShouldMatch`](#naming-fieldsShouldMatch) | fields of a type match a regex |
| [`fieldsShouldNotMatch`](#naming-fieldsShouldNotMatch) | field names do not match a regex |
| [`fieldsAnnotatedWithShouldMatch`](#naming-fieldsAnnotatedWithShouldMatch) | annotated fields match a regex |
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

Remember that the regex is matched against the fully qualified name, so it has to cover the package
too.

```java
.java(java -> java
    .naming(naming -> naming
        .classesShouldMatch("com\\.company\\.project\\..*")))
```

#### `classesShouldNotMatch` { #naming-classesShouldNotMatch }

`classesShouldNotMatch(String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .classesShouldNotMatch(".*Impl")))
```

#### `classesAnnotatedWithShouldMatch` { #naming-classesAnnotatedWithShouldMatch }

`classesAnnotatedWithShouldMatch(Class<? extends Annotation> annotationType, String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .classesAnnotatedWithShouldMatch(Entity.class, ".*Entity")))
```

#### `classesImplementingShouldMatch` { #naming-classesImplementingShouldMatch }

`classesImplementingShouldMatch(Class<?> clazz, String regex)`

Constrains the names of classes that implement the given interface directly.

```java
.java(java -> java
    .naming(naming -> naming
        .classesImplementingShouldMatch(Configurer.class, ".*Configurer")))
```

#### `classesAssignableToShouldMatch` { #naming-classesAssignableToShouldMatch }

`classesAssignableToShouldMatch(Class<?> clazz, String regex)`

Same idea, but covers the whole subtype hierarchy rather than direct implementors.

```java
.java(java -> java
    .naming(naming -> naming
        .classesAssignableToShouldMatch(AbstractConfigurer.class, ".*Configurer")))
```

#### `methodsShouldMatch` { #naming-methodsShouldMatch }

`methodsShouldMatch(String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldMatch("[a-z][a-zA-Z0-9]*")))
```

#### `methodsShouldNotMatch` { #naming-methodsShouldNotMatch }

`methodsShouldNotMatch(String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .methodsShouldNotMatch("^(foo|bar).*")))
```

#### `methodsAnnotatedWithShouldMatch` { #naming-methodsAnnotatedWithShouldMatch }

`methodsAnnotatedWithShouldMatch(Class<? extends Annotation> annotationType, String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .methodsAnnotatedWithShouldMatch(Scheduled.class, "^scheduled[A-Z].*")))
```

#### `fieldsShouldMatch` { #naming-fieldsShouldMatch }

`fieldsShouldMatch(Class<?> clazz, String regex)`
`fieldsShouldMatch(String typeName, String regex)`

Constrains the *name* of fields whose declared **type** is the given class. The example below
requires every field of type `Matcher` to be named `matcher`.

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldMatch(Matcher.class, "matcher")))
```

#### `fieldsShouldNotMatch` { #naming-fieldsShouldNotMatch }

`fieldsShouldNotMatch(String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsShouldNotMatch(".*(List|Set|Map)$")))
```

#### `fieldsAnnotatedWithShouldMatch` { #naming-fieldsAnnotatedWithShouldMatch }

`fieldsAnnotatedWithShouldMatch(Class<? extends Annotation> annotationType, String regex)`

```java
.java(java -> java
    .naming(naming -> naming
        .fieldsAnnotatedWithShouldMatch(Value.class, "^[a-z][a-zA-Z0-9]*$")))
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

`classesShouldUseLogger(Class<?> clazz, String regex)`

Classes matching the regex must declare a field of the given logger type. This is the complement of
the rule above: one fixes *how* loggers are declared, the other *that* they are declared.

```java
.logging(logging -> logging
    .classesShouldUseLogger(Logger.class, ".*Service"))
```

## 9. Test Rules

Default import mode: `ONLY_TESTS`. All rules live in the nested `junit(...)` configurer.

| Rule | Enforces |
|------|----------|
| [`classesShouldEndWithTest`](#junit-classesShouldEndWithTest) | test classes end with `Test` |
| [`classesShouldMatch`](#junit-classesShouldMatch) | test class names match a regex |
| [`classesShouldBePackagePrivate`](#junit-classesShouldBePackagePrivate) | matching classes are package-private |
| [`classesShouldNotBeAnnotatedWithDisabled`](#junit-classesShouldNotBeAnnotatedWithDisabled) | no `@Disabled` classes |
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

- `classesShouldBePackagePrivate(regex)` selects by name, and skips interfaces.
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

`classesShouldBePackagePrivate(String regex)`

Unlike the rules above, this one selects purely by name, not by the presence of test methods.

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldBePackagePrivate(".*Test")))
```

#### `classesShouldNotBeAnnotatedWithDisabled` { #junit-classesShouldNotBeAnnotatedWithDisabled }

```java
.test(test -> test
    .junit(junit -> junit
        .classesShouldNotBeAnnotatedWithDisabled()))
```

#### `methodsShouldMatch` { #junit-methodsShouldMatch }

`methodsShouldMatch(String regex)`

```java
.test(test -> test
    .junit(junit -> junit
        .methodsShouldMatch("should[A-Z].*")))
```

#### `methodsShouldBePackagePrivate` { #junit-methodsShouldBePackagePrivate }

JUnit 5 does not require `public` test methods, so `public` is noise.

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

| Framework      | Recognised call                                                              |
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
`noSelfInvocationOfProxiedMethods(Collection<String> annotations)`

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
project:

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
| `shouldBeAnnotatedWithConfigurationProperties()` | classes ending in `Properties` carry the annotation |
| `shouldBeAnnotatedWithConfigurationProperties(String regex)` | same, for classes matching a regex |
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
| `shouldBeAnnotatedWithController()` | classes ending in `Controller` carry `@Controller` |
| `shouldBeAnnotatedWithController(String regex)` | same, for classes matching a regex |
| `shouldBeAnnotatedWithRestController()` | classes ending in `Controller` carry `@RestController` |
| `shouldBeAnnotatedWithRestController(String regex)` | same, for classes matching a regex |
| `shouldBePackagePrivate()` | controllers are package-private |
| `shouldNotDependOnOtherControllers()` | no controller-to-controller dependencies |
| `shouldNotDependOnRepositories()` | the service layer is not bypassed |
| `shouldBeAnnotatedWithValidated()` | controllers needing `@Validated` carry it |
| `shouldBeAnnotatedWithValidated(String regex)` | same, for classes matching a regex |
| `shouldNotBeAnnotatedWithValidated()` | controllers do not carry `@Validated` |
| `shouldNotBeAnnotatedWithValidated(String regex)` | same, for classes matching a regex |

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

The `regex` overloads of `shouldBeAnnotatedWith...` select classes by name rather than by annotation,
which is how you enforce the annotation on classes that do not carry it yet.

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
| `shouldBeAnnotatedWithService()` | classes ending in `Service` carry `@Service` |
| `shouldBeAnnotatedWithService(String regex)` | same, for classes matching a regex |
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
| `shouldBeAnnotatedWithRepository()` | classes ending in `Repository` carry `@Repository` |
| `shouldBeAnnotatedWithRepository(String regex)` | same, for classes matching a regex |
| `shouldNotDependOnControllers()` | no repository-to-controller dependencies |
| `shouldNotDependOnServices()` | no repository-to-service dependencies |

```java
.spring(spring -> spring
    .repositories(repositories -> repositories
        .namesShouldEndWithRepository()
        .shouldBeAnnotatedWithRepository()
        .shouldNotDependOnServices()
        .shouldNotDependOnControllers()))
```

### 10.8 Transactional

These rules target the ways `@Transactional` silently does nothing at runtime. Both
`org.springframework.transaction.annotation.Transactional` and `jakarta.transaction.Transactional`
are recognised.

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

### 11.1 General

#### `noInjectionFields`

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
| `shouldBeAnnotatedWithPath()` | classes ending in `Resource` carry `@Path` |
| `shouldBeAnnotatedWithPath(String regex)` | same, for classes matching a regex |
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
| `namesShouldEndWithAssistantOrResource()` | name matches `.+(Assistant\|Service)` |
| `namesShouldMatch(String regex)` | name matches a regex |
| `shouldBeAnnotatedWithApplicationScoped()` | AI services carry `@ApplicationScoped` |
| `shouldNotUseToolsAttributeInAiService()` | tools are not declared via the `tools` attribute |

```java
.quarkus(quarkus -> quarkus
    .ai(ai -> ai
        .namesShouldEndWithAssistantOrResource()
        .shouldBeAnnotatedWithApplicationScoped()
        .shouldNotUseToolsAttributeInAiService()))
```

!!! warning
    Despite its name, `namesShouldEndWithAssistantOrResource` accepts names ending in `Assistant` or
    **`Service`**, not `Resource`. Use `namesShouldMatch` if you need a different pattern.

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
                .shouldNotImport("..internal.."))
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
                .classesShouldBePackagePrivate(".*Test")
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

## 13. Deprecated and Legacy Methods

| Deprecated                                                     | Use instead                                              | Status                      |
|----------------------------------------------------------------|----------------------------------------------------------|-----------------------------|
| `test.junit5(...)`                                             | `test.junit(...)`                                        | `@Deprecated(forRemoval)`   |
| `boot.springBootApplicationShouldBeIn(...)`                    | `boot.applicationClassShouldResideInPackage(...)`        | `@Deprecated(forRemoval)`   |
| `quarkus.ai.annotatedWithApplicationScoped()`                  | `quarkus.ai.shouldBeAnnotatedWithApplicationScoped()`    | legacy alias                |
| `quarkus.ai.notUseRegisterAiServiceToDefineTools()`            | `quarkus.ai.shouldNotUseToolsAttributeInAiService()`     | legacy alias                |
| `quarkus.panache.annotatedWithEntityWhenActiveRecordPattern()` | `quarkus.panache.shouldBeAnnotatedWithEntityWhenActiveRecordPattern()` | legacy alias |

The legacy aliases delegate to their replacement and behave identically. They are not yet marked
`@Deprecated`, but the `should...` names are the supported spelling.

## 14. Complete Example

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
            .fieldsShouldHaveModifiers("^[A-Z][A-Z0-9_]*$", List.of(STATIC, FINAL))
            .classesShouldImplementHashCodeAndEquals()
            .finalClassesShouldNotHaveProtectedMembers()
            .utilityClassesShouldBeFinalAndHavePrivateConstructor()
            .methodsShouldNotDeclareGenericExceptions()
            .fieldsShouldNotBePublic()
            .serialVersionUIDFieldsShouldBeStaticFinalLong()
            .classesShouldResideInPackage("com.enofex.taikai..")
            .imports(imports -> imports
                .shouldHaveNoCycles()
                .shouldNotImport("org.springframework.core.annotation..")
                .shouldNotImport("jakarta.annotation..")
                .shouldNotImport("javax.annotation..")
                .shouldNotImport("org.jetbrains.annotations..")
                .shouldNotImport(shaded())
                .shouldNotImport(lombok()))
            .naming(naming -> naming
                .packagesShouldMatchDefault()
                .fieldsShouldNotMatch(".*(List|Set|Map)$")
                .classesShouldNotMatch(".*Impl")
                .classesAssignableToShouldMatch(AbstractConfigurer.class, ".*Configurer")
                .classesImplementingShouldMatch(Configurer.class, ".*Configurer")
                .interfacesShouldNotHavePrefixI()
                .constantsShouldFollowConventions()))
        .build()
        .checkAll();
  }
}
```
