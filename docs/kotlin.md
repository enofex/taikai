# Kotlin/JVM Support

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
compiler fixtures and metadata reader use Kotlin 2.4.21 and target JVM 17. Metadata
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
            .namespace("com.company.project")
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
import com.enofex.taikai.kotlin.functionsShouldNotExceedMaxParameters
import com.enofex.taikai.kotlin.taikai

val architecture = taikai {
    namespace("com.company.project")
    failOnEmpty()
    java {
        fieldsShouldNotBePublic()
        classesShouldResideInPackage("com.company.project..")
    }
    spring {
        services { it.namesShouldEndWithService().shouldNotDependOnControllers() }
    }
    kotlin {
        classesShouldBeDataClasses(".*(Request|Response)")
        dataClassesShouldBeImmutable()
        functionsShouldMatch("[a-z][a-zA-Z0-9]*")
        propertiesShouldMatch("[a-z][a-zA-Z0-9]*")
    }
    metrics {
        functionsShouldNotExceedMaxParameters(4)
    }
}
architecture.checkAll()
```

`java`, `spring`, `logging`, `test`, `quarkus`, `jpa`, `metrics` and `kotlin` blocks have
configuration receivers. Nested Java configurers retain their existing callback API, as
the `services` example shows. The DSL marker prevents accidental unqualified access to
the outer builder inside these receiver blocks.

`discoverChecks` runs the [project checks](documentation.md#514-custom-checks), with
`ChecksConfigurer` as the receiver:

```kotlin
discoverChecks {
    configure(RequiredFilesCheck::class.java, RequiredFilesCheck.files("README.md", "LICENSE"))
}
```

`discoverExports` discovers the [exports](documentation.md#515-custom-exports), with
`ExportsConfigurer` as the receiver; `export()` on the built instance runs them:

```kotlin
taikai {
    namespace("com.company.project")
    discoverExports {
        configure(AgentsMarkdownExport::class.java, AgentsMarkdownExport.file("ARCHITECTURE.md"))
    }
}.export()
```

Use `classes(MyType::class)` or `classes(importedJavaClasses)` instead of a namespace
for explicit imports. `excludeClasses` takes patterns such as
`"com.company.project.generated.."`, class literals such as `Generated::class` or
[criteria](documentation.md#513-selecting-with-criteria). `addRule(...)`,
`addRules(...)` and `failOnEmpty(false)` are also available. Namespace and explicit classes remain
mutually exclusive. Kotlin blocks can be repeated, and `disable()` clears previously
registered Kotlin rules on that builder. `toBuilder()` preserves existing rules and
allows new Kotlin rules without mutating the original instance; `disable()` on such a
builder only clears the Kotlin rules added after `toBuilder()`, not the preserved ones.

## Kotlin-Specific Rules

All rules operate on Kotlin declarations only. Regexes match the **entire simple
source name**, including names before JVM mangling; they are not package patterns.
The class regex on requirement rules selects which Kotlin declarations to check.

| Method | Behavior |
|--------|----------|
| `classesShouldNotMatch(String regex)` | Source class names must not match the regex, excluding file facades and synthetic helpers. |
| `classesShouldMatch(String regex)` | All source class names, including objects and companions, must match the regex. File facades and synthetic helpers are excluded. |
| `objectsShouldMatch(String regex)` | Object and companion-object names must match the regex; other class kinds are ignored. |
| `functionsShouldNotMatch(String regex)` | Source function names, including names before `internal` JVM mangling, must not match the regex. |
| `functionsShouldMatch(String regex)` | Declared source function names must match the regex; accessors and generated data-class methods are excluded. |
| `propertiesShouldNotMatch(String regex)` | Non-constant property names, including computed and delegated properties, must not match the regex. |
| `propertiesShouldMatch(Class<?> type, String regex)` | Non-constant properties of the selected type must have names matching the regex; accepts a fully qualified type name, Java `Class` or Kotlin `KClass`. Includes properties without backing fields. |
| `propertiesShouldMatch(String regex)` | Declared non-constant property names must match the regex. A Boolean `active` property is checked as `active`, not `getActive`. |
| `noUsageOfDeprecatedAPIs()` | Classes must not have bytecode dependencies on deprecated classes or access deprecated Kotlin or Java members, including property-level and accessor-level deprecation. |
| `constantsShouldFollowConventions()` | Only `const val` names must use uppercase letters, digits and underscores, starting with a letter. Ordinary `val` properties and generated fields are excluded. |
| `classesShouldBeDataClasses(String regex)` | Selected source classes must be data classes. |
| `dataClassesShouldBeImmutable()` | Data classes must declare only `val` properties, including properties outside their primary constructors. This does not enforce deep immutability of referenced objects or collections. |
| `classesShouldBeObjects(String regex)` | Selected source classes must be regular object declarations. Companion objects do not satisfy this requirement. |
| `classesShouldHaveVisibility(String regex, KotlinVisibility visibility)` | Selected class declarations must have the given source visibility. |
| `functionsShouldHaveVisibility(String regex, KotlinVisibility visibility)` | Selected declared functions must have the given source visibility. |
| `propertiesShouldHaveVisibility(String regex, KotlinVisibility visibility)` | Selected declared properties must have the given source visibility. Accessor visibility is not checked separately. |

`functionsShouldNotExceedMaxParameters(int max)` in `metrics { }` counts declared function value
parameters, excluding receivers, suspend continuations, default-argument machinery, generated
overloads and constructors. Import it from `com.enofex.taikai.kotlin`.

Visibility uses `KotlinVisibility.PUBLIC`, `INTERNAL`, `PROTECTED` or `PRIVATE`.
For example, `functionsShouldHaveVisibility("load.*", KotlinVisibility.INTERNAL)`
checks source `internal` functions despite their public, mangled JVM representation.
Only directly declared properties and functions are checked; inherited and delegated
members are excluded. Explicitly declared overrides are included.

Each rule accepts an optional final `TaikaiRule.Configuration`, following core's
namespace, import-mode, explicit-class and exclusion behavior:

```kotlin
import com.enofex.taikai.core.Namespace
import com.enofex.taikai.core.TaikaiRule.Configuration

// Include test classes in this individual rule.
functionsShouldMatch(
    "[a-z][a-zA-Z0-9]*",
    Configuration.of("com.company.project", Namespace.Import.WITH_TESTS),
)
```

The default excludes test classes. Empty selections pass unless `failOnEmpty(true)`
is enabled. For Kotlin rules this checks the enclosing imported Kotlin-class
selection; it does not require that a member-name/type filter selects a declaration.
An unreadable metadata annotation on an included Kotlin class is a rule
violation. Application classes are analyzed through imported annotation data and are
not initialized to inspect metadata.

## Applying a Shared Java Architecture Profile to Kotlin

Keep bytecode-level rules in `java {}` and use Kotlin declaration rules for source
names, properties and Kotlin deprecation. This profile expresses the conventions
without treating file facades as Java utility classes or every static final field
as a Kotlin constant:

```kotlin
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.kotlin.taikai

taikai {
    namespace("com.company.project")
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
        propertiesShouldMatch("com.company.project.Matcher", "matcher")
        constantsShouldFollowConventions()
        classesShouldBeObjects(".*(?:Utils|Utilities)")
    }
}.checkAll()
```

`methodsShouldNotDeclareGenericExceptions()` checks JVM declarations produced by
`@Throws`; it does not inspect arbitrary exceptions thrown by function bodies.
The `classesShouldBeObjects` policy applies only to selected named containers;
ordinary top-level functions do not need a private constructor.

The type overloads of `propertiesShouldMatch` select properties of exactly that type, not of its
subtypes; nullability and type arguments do not matter, and type aliases count as their expanded
type. A type name can be a Kotlin classifier such as `kotlin.String` or a JVM name such as
`java.lang.String`. A `KClass` matches both, so `List::class` also selects `MutableList` properties
and `Int::class` nullable and non-nullable `Int` properties, while a Java `Class` matches only its
exact JVM type.

The Kotlin `noUsageOfDeprecatedAPIs()` checks `kotlin.Deprecated` and `java.lang.Deprecated`, also
on properties and individual accessors. It reports calls, including those with default arguments,
member references, field accesses and dependencies on deprecated classes, also inside default-value
expressions. A deprecated property initializing its backing field, or generated code forwarding to
a deprecated declaration, is not a usage. The rule examines compiled bytecode, so inlined function
bodies, inlined constants and erased type aliases do not reveal a deprecated API; the Kotlin
compiler's own deprecation checks cover these.

## Existing Rule Compatibility

| Existing rule category | Kotlin/JVM behavior |
|------------------------|---------------------|
| Package placement and dependency boundaries | Shared bytecode rules work across Java and Kotlin. The suite verifies package placement and cross-language dependencies. |
| Spring class annotations and naming | Shared rules work for Kotlin Spring classes. The demo verifies controller/service/repository names and layer dependencies. |
| Class inheritance and JVM modifiers | Operate on compiled JVM declarations. Kotlin source visibility and default finality can differ from Java conventions. |
| Fields and properties | Java field rules inspect backing fields, not source properties. Prefer Kotlin property rules for source intent. |
| Boolean method naming | Java rules can reject the generated `getActive()` accessor. Prefer Kotlin function/property naming rules. |
| Deprecation | The Java rule checks Java deprecation annotations. The Kotlin rule handles Kotlin and Java deprecation, including source-property carriers and default helpers. |
| Method parameter counts | The `metrics` rule `methodsShouldNotExceedMaxParameters` counts JVM parameters. Prefer the Kotlin counterpart for suspend, extension and default-argument functions. |
| Utility classes and constants | Java static-member conventions do not map cleanly to file facades, companions, objects and ordinary `val` properties. Use Kotlin object and constant rules. |
| Logging, framework methods, test methods | Retain JVM semantics; annotation use-site targets, accessors, generated methods and suspend signatures may matter. No automatic source adaptation is applied. |
| Dependency cycles and forbidden dependencies | Include compiler-generated dependencies. Helpers are not globally removed from imported classes. |

Kotlin rules read metadata with the official
[Kotlin Metadata JVM library](https://kotlinlang.org/docs/metadata-jvm.html).
The [Kotlin/JVM interoperability
documentation](https://kotlinlang.org/docs/java-to-kotlin-interop.html) explains how source
properties, companions and top-level functions map to JVM members.

## Java Callers and Extensions

Java users can configure the optional rules without Kotlin extension functions:

```java
Taikai.builder()
    .namespace("com.company.project")
    .configure(kotlin -> kotlin
        .classesShouldBeDataClasses(".*Response")
        .dataClassesShouldBeImmutable(), KotlinConfigurer::new)
    .build()
    .checkAll();
```

Import `com.enofex.taikai.kotlin.KotlinConfigurer`. The public generic
`Builder.configure(customizer, supplier)` extension point uses the same per-configurer
registration and accumulation behavior as the built-in configuration methods.

## Runnable Example

See the [Spring Kotlin demo](https://github.com/enofex/taikai/tree/main/examples/spring-kotlin)
for an HTTP application with architecture tests and project checks using the receiver DSL.
