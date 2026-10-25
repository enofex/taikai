# Kotlin/JVM support

Taikai can be called from Kotlin tests and can analyze compiled Kotlin/JVM code. Add
`taikai-kotlin` for source-aware Kotlin rules and a receiver DSL. The existing
`taikai` artifact remains Java-only and keeps its existing rule semantics.
Kotlin/JS and Kotlin/Native are not supported.

## Dependencies

Use the same Taikai version for both artifacts. `taikai-kotlin` brings core in
transitively, so a separate core dependency is unnecessary.

```xml
<dependency>
  <groupId>com.enofex</groupId>
  <artifactId>taikai-kotlin</artifactId>
  <version>${taikai.version}</version>
  <scope>test</scope>
</dependency>
```

```kotlin
// build.gradle.kts
testImplementation("com.enofex:taikai-kotlin:$taikaiVersion")
```

Compile production Kotlin before running architecture tests. The Kotlin library,
compiler fixtures, and metadata reader use Kotlin 2.3.21 and target JVM 17. Metadata
from unsupported compiler versions produces an explicit failure identifying the
class; it is never silently treated as compliant. Keep your resolved Kotlin runtime
and metadata reader versions compatible with your application's compiler.

## Existing API from Kotlin

You do not need a new DSL to call the existing Java API:

```kotlin
import com.enofex.taikai.Taikai
import com.enofex.taikai.kotlin.kotlin
import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun architecture() {
        Taikai.builder()
            .namespace("com.example")
            .spring { spring ->
                spring.services { it.namesShouldEndWithService() }
            }
            .kotlin {
                classesShouldBeDataClasses(".*Response")
                dataClassesShouldBeImmutable()
            }
            .build()
            .checkAll()
    }
}
```

## Receiver DSL

The `taikai` function builds a `Taikai` instance. It does not execute rules.
Use `check()` to stop at the first failed rule or `checkAll()` to aggregate failures.

```kotlin
import com.enofex.taikai.kotlin.taikai

val architecture = taikai {
    namespace("com.example")
    failOnEmpty()
    java {
        fieldsShouldNotBePublic()
        classesShouldResideInPackage("com.example..")
    }
    spring {
        services { it.namesShouldEndWithService().shouldNotDependOnControllers() }
    }
    kotlin {
        classesShouldBeDataClasses(".*(Request|Response)")
        dataClassesShouldBeImmutable()
        functionsShouldMatch("[a-z][a-zA-Z0-9]*")
        propertiesShouldMatch("[a-z][a-zA-Z0-9]*")
        functionsShouldNotExceedMaxParameters(4)
    }
}
architecture.checkAll()
```

`java`, `spring`, `logging`, `test`, and `kotlin` blocks have configuration receivers.
Nested Java configurers retain their existing callback API, as the `services`
example shows. The DSL marker prevents accidental unqualified access to the outer
builder inside these receiver blocks.

Use `classes(MyType::class)` or `classes(importedJavaClasses)` instead of a namespace
for explicit imports. `excludeClasses("com.example.generated..")`, `addRule(...)`,
and `failOnEmpty(false)` are also available. Namespace and explicit classes remain
mutually exclusive. Kotlin blocks can be repeated, and `disable()` clears previously
registered Kotlin rules on that builder. `toBuilder()` preserves existing rules and
allows new Kotlin rules without mutating the original instance.

## Kotlin-specific rules

All rules operate on Kotlin declarations only. Regexes match the **entire simple
source name**, including names before JVM mangling; they are not package patterns.
The class regex on requirement rules selects which Kotlin declarations to check.

| Method | Behavior |
| --- | --- |
| `classesShouldNotMatch(regex)` | Forbid matching source class names, excluding file facades and synthetic helpers. |
| `classesShouldMatch(regex)` | All source class names, including objects and companions, match the pattern. File facades and synthetic helpers are excluded. |
| `objectsShouldMatch(regex)` | Object and companion-object names match the pattern; other class kinds are ignored. |
| `functionsShouldNotMatch(regex)` | Forbid matching source function names, including names before `internal` JVM mangling. |
| `functionsShouldMatch(regex)` | Declared source function names match; accessors and generated data-class methods are excluded. |
| `propertiesShouldNotMatch(regex)` | Forbid matching non-constant property names, including computed and delegated properties. |
| `propertiesShouldMatch(type, regex)` | Require matching names for non-constant properties of the selected type; accepts a fully qualified type name, Java `Class`, or Kotlin `KClass`. Includes properties without backing fields. |
| `propertiesShouldMatch(regex)` | Declared non-constant property names match. A Boolean `active` property is checked as `active`, not `getActive`. |
| `noUsageOfDeprecatedAPIs()` | Forbid bytecode dependencies on deprecated classes and accesses to deprecated Kotlin or Java members, including property-level and accessor-level deprecation. |
| `constantsShouldFollowConventions()` | Only `const val` names must use uppercase letters, digits, and underscores, starting with a letter. Ordinary `val` properties and generated fields are excluded. |
| `classesShouldBeDataClasses(regex)` | Selected source classes must be data classes. |
| `dataClassesShouldBeImmutable()` | Data classes must declare only `val` properties, including properties outside their primary constructors. This does not enforce deep immutability of referenced objects or collections. |
| `classesShouldBeObjects(regex)` | Selected source classes must be regular object declarations. Companion objects do not satisfy this requirement. |
| `classesShouldHaveVisibility(regex, visibility)` | Selected class declarations have the specified source visibility. |
| `functionsShouldHaveVisibility(regex, visibility)` | Selected declared functions have the specified source visibility. |
| `propertiesShouldHaveVisibility(regex, visibility)` | Selected declared properties have the specified source visibility. Accessor visibility is not checked separately. |
| `functionsShouldNotExceedMaxParameters(max)` | Count declared function value parameters, excluding receivers, suspend continuations, default-argument machinery, generated overloads, and constructors. |

Visibility uses `KotlinVisibility.PUBLIC`, `INTERNAL`, `PROTECTED`, or `PRIVATE`.
For example, `functionsShouldHaveVisibility("load.*", KotlinVisibility.INTERNAL)`
checks source `internal` functions despite their public, mangled JVM representation.
Only directly declared properties and functions are checked; inherited and delegated
members are excluded. Explicitly declared overrides are included.

Each rule accepts an optional final `TaikaiRule.Configuration`, following core's
namespace, import-mode, explicit-class, and exclusion behavior:

```kotlin
import com.enofex.taikai.Namespace
import com.enofex.taikai.TaikaiRule.Configuration

// Include test classes in this individual rule.
functionsShouldNotExceedMaxParameters(
    4,
    Configuration.of("com.example", Namespace.IMPORT.WITH_TESTS),
)
```

The default excludes test classes. Empty selections pass unless `failOnEmpty(true)`
is enabled. For Kotlin rules this checks the enclosing imported Kotlin-class
selection; it does not require that a member-name/type filter selects a declaration.
An unreadable metadata annotation on an included Kotlin class is a rule
violation. Application classes are analyzed through imported annotation data and are
not initialized to inspect metadata.

## Applying a shared Java architecture profile to Kotlin

Keep bytecode-level rules in `java {}` and use Kotlin declaration rules for source
names, properties, and Kotlin deprecation. This profile expresses the conventions
without treating file facades as Java utility classes or every static final field
as a Kotlin constant:

```kotlin
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.kotlin.taikai

taikai {
    namespace("com.example")
    java {
        methodsShouldNotDeclareGenericExceptions()
        imports {
            it.shouldHaveNoCycles().shouldNotImport(ImportPatterns.shaded())
        }
        naming {
            it.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI()
        }
    }
    kotlin {
        noUsageOfDeprecatedAPIs()
        classesShouldNotMatch(".*Impl")
        functionsShouldNotMatch("^(foo|bar)$")
        propertiesShouldNotMatch(".*(List|Set|Map)$")
        propertiesShouldMatch("com.enofex.taikai.Matcher", "matcher")
        constantsShouldFollowConventions()
        classesShouldBeObjects(".*(?:Utils|Utilities)")
    }
}.checkAll()
```

`methodsShouldNotDeclareGenericExceptions()` checks JVM declarations produced by
`@Throws`; it does not inspect arbitrary exceptions thrown by function bodies.
The `classesShouldBeObjects` policy applies only to selected named containers;
ordinary top-level functions do not need a private constructor.

Type-selected property naming uses exact source classifiers or erased JVM types,
not subtype assignability. Nullable reference properties and generic arguments do
not prevent matching. Kotlin `KClass` arguments also recognize the corresponding
JVM type, such as `List::class` selecting `MutableList` properties through
`java.util.List`. `Int::class` selects nullable and non-nullable Kotlin `Int`
properties; Java `Class` arguments use the exact JVM representation. String inputs
can specify either a Kotlin classifier (`kotlin.String`) or JVM name
(`java.lang.String`). Kotlin source aliases are matched by their expanded type.
Inherited and generated forwarding properties are excluded, consistently with
other Kotlin property rules. Each overload accepts the final per-rule
`Configuration` argument.

The Kotlin deprecation rule examines both `kotlin.Deprecated` and
`java.lang.Deprecated`, including annotations on property carriers and individual
accessors. It checks calls, member references, field accesses, and dependencies on
deprecated classes. It follows default-argument helpers and generated callable
reference adapters, preserving overload signatures. Companion and interface-property annotation carriers
and generated adapters are read from their exact class resources without
initializing application classes. Normal backing-field initialization and generated
forwarding to a deprecated declaration do not turn the declaration itself into an
API usage. Deprecated API calls inside default-value expressions are still checked.

The rule examines compiled bytecode. Inlined function bodies, inlined constants,
and erased type aliases cannot reliably reveal that a deprecated source API was
referenced. Kotlin's own compiler deprecation checks remain useful for these cases.
The existing Java deprecation rule retains its original semantics; use the Kotlin
rule for Kotlin callers of both Java and Kotlin APIs.

## Existing rule compatibility

These categories describe intended semantics; they do not promise that every
individual rule in a category has Kotlin-specific coverage.

| Existing rule category | Kotlin/JVM behavior |
| --- | --- |
| Package placement and dependency boundaries | Shared bytecode rules work across Java and Kotlin. The suite verifies package placement and cross-language dependencies. |
| Spring class annotations and naming | Shared rules work for Kotlin Spring classes. The demo verifies controller/service/repository names and layer dependencies. |
| Class inheritance and JVM modifiers | Operate on compiled JVM declarations. Kotlin source visibility and default finality can differ from Java conventions. |
| Fields and properties | Java field rules inspect backing fields, not source properties. Prefer Kotlin property rules for source intent. |
| Boolean method naming | Java rules can reject the generated `getActive()` accessor. Prefer Kotlin function/property naming rules. |
| Deprecation | The Java rule checks Java deprecation annotations. The Kotlin rule handles Kotlin and Java deprecation, including source-property carriers and default helpers. |
| Method parameter counts | Java rules count JVM parameters. Prefer the Kotlin counterpart for suspend, extension, and default-argument functions. |
| Utility classes and constants | Java static-member conventions do not map cleanly to file facades, companions, objects, and ordinary `val` properties. Use Kotlin object and constant rules. |
| Logging, framework methods, test methods | Retain JVM semantics; annotation use-site targets, accessors, generated methods, and suspend signatures may matter. No automatic source adaptation is applied. |
| Dependency cycles and forbidden dependencies | Include compiler-generated dependencies. Helpers are not globally removed from imported classes. |

Kotlin rules read metadata with the official
[Kotlin Metadata JVM library](https://kotlinlang.org/docs/metadata-jvm.html).
The [Kotlin/JVM interoperability documentation](https://kotlinlang.org/docs/java-to-kotlin-interop.html)
explains how source properties, companions, and top-level functions map to JVM members.

## Java callers and extensions

Java users can configure the optional rules without Kotlin extension functions:

```java
Taikai.builder()
    .namespace("com.example")
    .configure(kotlin -> kotlin
        .classesShouldBeDataClasses(".*Response")
        .dataClassesShouldBeImmutable(), KotlinConfigurer::new)
    .build()
    .checkAll();
```

Import `com.enofex.taikai.kotlin.KotlinConfigurer`. The public generic
`Builder.configure(customizer, factory)` extension point uses the same per-configurer
registration and accumulation behavior as the built-in configuration methods.

## Runnable example

See the [Spring Kotlin demo](https://github.com/enofex/taikai/tree/main/examples/spring-kotlin)
for an HTTP application and architecture tests using the receiver DSL.
