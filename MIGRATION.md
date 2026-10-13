# Migration Guide

## Migrating from 1.x to 2.0.0

Taikai 2.0.0 is a major release built around **criteria**: rules that used to select classes by a name regex now take an ArchUnit `DescribedPredicate<JavaClass>` instead. You can target classes by annotation, package, simple name or any predicate of your own. This release also removes deprecated APIs, makes naming and parameter order consistent, and tightens several rules.

**Upgrading from 1.x requires code changes.** See [Breaking changes](#breaking-changes) below.

### Highlights

#### Criteria-based selection

Three new factories build the predicates rules take. Each matches the parameter type it is used for:

| Parameter type | Factory | Criteria |
|----------------|---------|----------|
| `DescribedPredicate<? super JavaClass>` | `ClassCriteria` | `annotatedWith(Class \| String)`, `type(Class \| String)` (exactly the type), `assignableTo(Class \| String)`, `implementing(Class \| String)`, `nameMatching(regex)` (fully qualified name), `simpleNameMatching(regex)`, `inPackage(packageIdentifier)` |
| `DescribedPredicate<? super JavaField>` | `FieldCriteria` | `nameMatching(regex)` (field name), `annotatedWith(Class \| String)`, `ofType(Class \| String \| ClassCriteria)` |
| `DescribedPredicate<? super JavaMethod>` | `MethodCriteria` | `nameMatching(regex)` (method name), `annotatedWith(Class \| String)` |

You can also pass any ArchUnit predicate and combine predicates with `.and()` / `.or()`.

```java
Taikai.builder()
    .namespace("com.company.project")
    .excludeClasses(ClassCriteria.inPackage("..generated.."))
    .java(java -> java
        .classesShouldBeRecords(ClassCriteria.simpleNameMatching(".*Dto"))
        .noUsageOf(ClassCriteria.inPackage("..domain.."), "org.springframework.web.client.RestTemplate")
        .imports(imports -> imports
            .shouldNotImport(ImportPatterns.shaded())))
    .spring(spring -> spring
        .controllers(controllers -> controllers
            .methodsShouldNotReturn(ClassCriteria.simpleNameMatching(".*Entity"))))
    .build()
    .check();
```

#### New rules and criteria overloads

**Global**
- `Taikai.Builder#excludeClasses(DescribedPredicate)` excludes classes that match the criteria from every rule.

**Java**
- Class rules with criteria: `classesShouldBeAnnotatedWith`, `classesShouldNotBeAnnotatedWith`, `classesShouldBeAnnotatedWithAll`, `classesShouldBeAssignableTo`, `classesShouldImplement`, `classesShouldBeInterfaces`, `classesShouldBeRecords`, `classesShouldBePackagePrivate`, `classesShouldHaveModifiers`, `classesShouldNotHaveModifiers`, `classesShouldResideInPackage`, `classesShouldResideOutsidePackage`
- Method and field rules scoped to the declaring class (`…ForClass`): `methodsShouldBeAnnotatedWithForClass`, `methodsShouldNotDeclareExceptionForClass`, `methodsShouldReturnForClass`, `methodsShouldNotReturnForClass` (#365), `fieldsShouldHaveModifiersForClass`, `fieldsShouldNotHaveModifiersForClass`
- `methodsShouldBeAnnotatedWithAll`, `methodsShouldNotBeAnnotatedWith`, `methodsShouldReturn` and `methodsShouldNotReturn` with method criteria, and `methodsShouldBeAnnotatedWithAllForClass` / `methodsShouldNotBeAnnotatedWithForClass` with class criteria
- `methodsShouldNotDeclareException(methods, DescribedPredicate)` for criteria-based exception types
- `noUsageOf(forbiddenTypes)`, `noUsageOf(classes, forbiddenTypes)`, `noUsageOf(classes, Class | typeName)`

**Imports**
- `shouldNotImport(forbiddenImports)`, `shouldNotImport(classes, forbiddenImports)`, `shouldImport(classes, requiredImports)`
- `ImportsConfigurer` can now be disabled with `disable()`.

**Naming**
- `classesShouldMatch` / `classesShouldNotMatch` with class criteria, `fieldsShouldMatch` / `fieldsShouldNotMatch` / `methodsShouldMatch` / `methodsShouldNotMatch` with field / method criteria
- `methodsShouldMatchForClass`, `methodsShouldNotMatchForClass`, `fieldsShouldMatchForClass`, `fieldsShouldNotMatchForClass`
- `NamingConfigurer` can now be disabled with `disable()`.

**Logging**
- `classesShouldUseLogger(DescribedPredicate, Class | typeName)`

**Spring**
- Controllers: `methodsShouldReturn` / `methodsShouldNotReturn`, plus criteria overloads for `shouldBeAnnotatedWithController`, `shouldBeAnnotatedWithRestController`, `shouldBeAnnotatedWithValidated` and `shouldNotBeAnnotatedWithValidated`
- Criteria overloads for `services.shouldBeAnnotatedWithService`, `repositories.shouldBeAnnotatedWithRepository` and `properties.shouldBeAnnotatedWithConfigurationProperties`
- `boot.applicationClassShouldResideInPackage(Configuration)`, which uses the configuration's namespace when one is set

**Quarkus**
- `resources.shouldBeAnnotatedWithPath(DescribedPredicate)`

**JUnit**
- `junit.classesShouldBePackagePrivate(DescribedPredicate)`

### Breaking changes

#### Removed deprecated and legacy methods

| Removed                                                    | Use instead                                                       |
|------------------------------------------------------------|-------------------------------------------------------------------|
| `test.junit5(...)`                                         | `test.junit(...)`                                                 |
| `boot.springBootApplicationShouldBeIn(...)`                | `boot.applicationClassShouldResideInPackage(...)`                 |
| `quarkus.ai.annotatedWithApplicationScoped()`              | `quarkus.ai.shouldBeAnnotatedWithApplicationScoped()`             |
| `quarkus.ai.notUseRegisterAiServiceToDefineTools()`        | `quarkus.ai.shouldNotUseToolsAttributeInAiService()`              |
| `quarkus.panache.annotatedWithEntityWhenActiveRecordPattern()` | `quarkus.panache.shouldBeAnnotatedWithEntityWhenActiveRecordPattern()` |

#### Renamed methods

| 1.x                                                          | 2.0.0                                                          |
|--------------------------------------------------------------|----------------------------------------------------------------|
| `quarkus.ai.namesShouldEndWithAssistantOrResource()`         | `quarkus.ai.namesShouldEndWithAssistantOrService()`            |

#### Class selection by regex replaced with criteria

Rules that selected the classes to check with a `String regex` now only take a `DescribedPredicate<? super JavaClass>`. Wrap the regex in `ClassCriteria.nameMatching(...)`. It matches the fully qualified name exactly as before, except for the two rules described below:

```java
// 1.x
.classesShouldBeRecords(".*Dto")
.imports(imports -> imports.shouldNotImport(".*Service", ".*Repository"))
.logging(logging -> logging.classesShouldUseLogger(Logger.class, ".*Service"))
// 2.0.0
.classesShouldBeRecords(ClassCriteria.nameMatching(".*Dto"))
.imports(imports -> imports.shouldNotImport(ClassCriteria.nameMatching(".*Service"),
    ClassCriteria.nameMatching(".*Repository")))
.logging(logging -> logging.classesShouldUseLogger(ClassCriteria.nameMatching(".*Service"), Logger.class))
```

This affects these methods, including their `Configuration` overloads:

| Configurer | Methods |
|------------|---------|
| `java` | `classesShouldBeAnnotatedWith`, `classesShouldNotBeAnnotatedWith`, `classesShouldBeAssignableTo`, `classesShouldImplement`, `classesShouldBeInterfaces`, `classesShouldBeRecords`, `classesShouldBePackagePrivate`, `classesShouldHaveModifiers`, `classesShouldNotHaveModifiers`, `classesShouldResideInPackage(regex, packageIdentifier)`, `classesShouldResideOutsidePackage`, `methodsShouldHaveModifiersForClass`, `methodsShouldNotHaveModifiersForClass` |
| `imports` | `shouldImport(regex, regex)`, `shouldNotImport(regex, regex)`: both arguments become criteria |
| `logging` | `classesShouldUseLogger(type, regex)` becomes `classesShouldUseLogger(classes, type)`: the classes to check now come first |
| `spring.controllers` | `shouldBeAnnotatedWithController`, `shouldBeAnnotatedWithRestController`, `shouldBeAnnotatedWithValidated`, `shouldNotBeAnnotatedWithValidated` |
| `spring.services` / `spring.repositories` / `spring.properties` | `shouldBeAnnotatedWithService`, `shouldBeAnnotatedWithRepository`, `shouldBeAnnotatedWithConfigurationProperties` |
| `quarkus.resources` | `shouldBeAnnotatedWithPath` |
| `test.junit` | `classesShouldBePackagePrivate` |

**`classesShouldBeAssignableTo` and `classesShouldImplement` are the exception.** In 1.x their first argument was not a regex but a suffix of the *simple* class name, so `classesShouldImplement("Repository", CrudRepository.class)` checked every class whose simple name ends with `Repository`. Migrate them with `ClassCriteria.simpleNameMatching(".*" + suffix)`, not with `nameMatching`, which would match no class and let the rule pass silently:

```java
// 1.x
.classesShouldImplement("Repository", CrudRepository.class)
// 2.0.0
.classesShouldImplement(ClassCriteria.simpleNameMatching(".*Repository"), CrudRepository.class)
```

These keep their `String` parameters:

- Overloads without arguments, such as `shouldBeAnnotatedWithController()`, keep their default name pattern.
- Rules where the regex is the naming convention itself, such as `classesShouldMatch`, `namesShouldMatch` and `packagesShouldMatch`.
- `classesShouldResideInPackage(packageIdentifier)` with a single argument.
- Overloads that take a fully qualified type name, such as `noUsageOf("java.util.Date")`.

#### Field and method rules take member criteria

Rules that selected fields or methods with a `String regex` on the member name now take a `DescribedPredicate<? super JavaField>` or `DescribedPredicate<? super JavaMethod>`. Wrap the regex in `FieldCriteria.nameMatching(...)` or `MethodCriteria.nameMatching(...)`. It matches the member name exactly as before:

```java
// 1.x
.fieldsShouldHaveModifiers("^[A-Z][A-Z0-9_]*$", List.of(STATIC, FINAL))
.methodsShouldBeAnnotatedWith(".*Api", PublicApi.class)
// 2.0.0
.fieldsShouldHaveModifiers(FieldCriteria.nameMatching("^[A-Z][A-Z0-9_]*$"), List.of(STATIC, FINAL))
.methodsShouldBeAnnotatedWith(MethodCriteria.nameMatching(".*Api"), PublicApi.class)
```

This affects `fieldsShouldHaveModifiers`, `fieldsShouldNotHaveModifiers`, `methodsShouldHaveModifiers`, `methodsShouldNotHaveModifiers`, `methodsShouldBeAnnotatedWith` and `methodsShouldNotDeclareException`, including their `Configuration` overloads.

The `…ForClass` variants still select members by their declaring class with class criteria. The naming rules `methodsShouldMatch`, `methodsShouldNotMatch` and `fieldsShouldNotMatch` are unchanged.

#### Annotation and type variants replaced with criteria

The rule variants that selected classes, fields or methods by an annotation or a supertype are removed. Each one is the general rule with an `annotatedWith`, `assignableTo` or `implementing` criterion:

```java
// 1.x
.classesAnnotatedWithShouldBeRecords(ConfigurationProperties.class)
.methodsAnnotatedWithShouldHaveModifiers(Transactional.class, List.of(PUBLIC))
.naming(naming -> naming.classesImplementingShouldMatch(Configurer.class, ".*Configurer"))
// 2.0.0
.classesShouldBeRecords(ClassCriteria.annotatedWith(ConfigurationProperties.class))
.methodsShouldHaveModifiers(MethodCriteria.annotatedWith(Transactional.class), List.of(PUBLIC))
.naming(naming -> naming.classesShouldMatch(ClassCriteria.implementing(Configurer.class), ".*Configurer"))
```

| 1.x | 2.0.0 |
|-----|-------|
| `classesAnnotatedWithShouldX(annotation, …)` | `classesShouldX(ClassCriteria.annotatedWith(annotation), …)` for `BePackagePrivate`, `BeRecords`, `HaveModifiers`, `NotHaveModifiers`, `NotBeAnnotatedWith`, `ResideInPackage` |
| `classesAssignableToShouldBePackagePrivate(type)` | `classesShouldBePackagePrivate(ClassCriteria.assignableTo(type))` |
| `classesShouldBeAnnotatedWithAll(annotation, required)` | `classesShouldBeAnnotatedWithAll(ClassCriteria.annotatedWith(annotation), requiredNames)` |
| `fieldsAnnotatedWithShould(Not)HaveModifiers(annotation, …)` | `fieldsShould(Not)HaveModifiers(FieldCriteria.annotatedWith(annotation), …)` |
| `methodsAnnotatedWithShould(Not)HaveModifiers(annotation, …)` | `methodsShould(Not)HaveModifiers(MethodCriteria.annotatedWith(annotation), …)` |
| `methodsAnnotatedWithShouldNotBeAnnotatedWith(annotation, forbidden)` | `methodsShouldNotBeAnnotatedWith(MethodCriteria.annotatedWith(annotation), forbidden)` |
| `methodsShouldBeAnnotatedWithAll(annotation, required)` | `methodsShouldBeAnnotatedWithAll(MethodCriteria.annotatedWith(annotation), requiredNames)` |
| `naming.classesAnnotatedWithShouldMatch(annotation, regex)` | `naming.classesShouldMatch(ClassCriteria.annotatedWith(annotation), regex)` |
| `naming.classesAssignableToShouldMatch(type, regex)` | `naming.classesShouldMatch(ClassCriteria.assignableTo(type), regex)` |
| `naming.classesImplementingShouldMatch(type, regex)` | `naming.classesShouldMatch(ClassCriteria.implementing(type), regex)` |
| `naming.fieldsAnnotatedWithShouldMatch(annotation, regex)` | `naming.fieldsShouldMatch(FieldCriteria.annotatedWith(annotation), regex)` |
| `naming.fieldsShouldMatch(type, regex)` | `naming.fieldsShouldMatch(FieldCriteria.ofType(type), regex)`, same exact raw type |
| `naming.methodsAnnotatedWithShouldMatch(annotation, regex)` | `naming.methodsShouldMatch(MethodCriteria.annotatedWith(annotation), regex)` |

`classesShouldBeAnnotatedWithAll` and `methodsShouldBeAnnotatedWithAll` take the required annotations as fully qualified names only. Replace `List.of(RequestMapping.class)` with `List.of(RequestMapping.class.getName())`.

Most replacements check the same classes, fields and methods as before. Two differences can change results:

- **`annotatedWith` also matches meta-annotations.** In 1.x these variants only matched classes, fields or methods with the annotation placed *directly* on them: `classesAnnotatedWithShouldBeRecords`, `…ShouldBePackagePrivate`, `…ShouldHaveModifiers`, `…ShouldNotHaveModifiers`, `…ShouldNotBeAnnotatedWith`, `fieldsAnnotatedWithShould(Not)HaveModifiers`, `methodsAnnotatedWithShould(Not)HaveModifiers` and `methodsAnnotatedWithShouldNotBeAnnotatedWith`. Their replacements also check everything carrying a composed annotation, so `ClassCriteria.annotatedWith(Controller.class)` now includes `@RestController` classes. To keep the 1.x selection, use ArchUnit's direct-only predicate, for example `CanBeAnnotated.Predicates.annotatedWith(Controller.class)`. The other variants already matched meta-annotations in 1.x.
- **`assignableTo` does not match the type itself.** `naming.classesAssignableToShouldMatch(type, regex)` also checked `type`; `classesShouldMatch(ClassCriteria.assignableTo(type), regex)` only checks its subtypes. To include the type, use `ClassCriteria.assignableTo(type).or(JavaClass.Predicates.equivalentTo(type))`. `classesAssignableToShouldBePackagePrivate` already excluded the type, so nothing changes there.

#### Imports and `noUsageOf`

- **`ImportPatterns.*()` now return `DescribedPredicate<JavaClass>`** instead of a package string. Use them with `imports.shouldNotImport(ImportPatterns.lombok())` or `java.noUsageOf(ImportPatterns.lombok())`.
- **`imports.shouldNotImport(String packageIdentifier)` has been removed.** Use `shouldNotImport(ClassCriteria.inPackage("..internal.."))` instead.
- **`java.noUsageOf(Class | typeName, String packageIdentifier)` has been removed.** The classes to check now come first, as criteria:
  ```java
  // 1.x
  .noUsageOf(RestTemplate.class, "..domain..")
  // 2.0.0
  .noUsageOf(ClassCriteria.inPackage("..domain.."), RestTemplate.class)
  ```
- The criteria versions of `noUsageOf` and `shouldNotImport` skip classes that match the forbidden criteria themselves. Classes inside a banned package can still use each other.

#### API tightening

- `Taikai.Builder` constructors are now private. Use `Taikai.builder()` or `taikai.toBuilder()`.
- Configurer classes are now `final`, and the constructors of the nested configurers, such as `ImportsConfigurer`, `NamingConfigurer` or `ControllersConfigurer`, are no longer public. Get configurers only through the builder's customizers.
- `TaikaiRule.of(ArchRule, Configuration)` no longer accepts a `null` configuration.
- `imports.shouldHaveNoCycles(Configuration)` no longer accepts `null`.
- A missing namespace now throws `TaikaiException` instead of `NullPointerException`. `TaikaiException` is now `final`.
- Parameter names are now consistent (`forbidden…` / `required…`, `annotationTypes`). This does not affect source compatibility.

### Behaviour changes and fixes

- `ClassCriteria.annotatedWith`, `FieldCriteria.annotatedWith` and `MethodCriteria.annotatedWith` match meta-annotations, which widens what the replaced `…AnnotatedWith…` variants checked. See [Annotation and type variants replaced with criteria](#annotation-and-type-variants-replaced-with-criteria).
- `ClassCriteria.assignableTo` does not match the type itself, which narrows `classesAssignableToShouldMatch`. See the same section.
- `repositories.shouldBeAnnotatedWithRepository()` and its overloads now skip interfaces, like the controller, service and properties rules. A Spring Data repository interface without `@Repository` no longer fails the rule.
- Method and field rules now ignore compiler-generated (synthetic) members, which removes false positives from lambdas, bridge methods, enum `$VALUES` and similar members.
- Rule descriptions and modifier violation messages now match what the rules actually check. **If you assert on violation text, update those assertions.**
- Fixed `excludedClasses` and rules being shared between a `Taikai` instance and builders created from it with `toBuilder()`.
- Fixed the `loggersShouldFollowConventions` Javadoc and rule text: the regex matches the logger field name.

**Full changelog:** [v1.69.0...v2.0.0](https://github.com/enofex/taikai/compare/v1.69.0...v2.0.0)
