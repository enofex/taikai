package com.enofex.taikai.kotlin

import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.configures.AbstractConfigurer
import com.enofex.taikai.configures.ConfigurerContext
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import kotlin.metadata.*
import kotlin.metadata.jvm.signature
import kotlin.reflect.KClass

/** Source visibility, rather than the JVM visibility of a compiled declaration. */
enum class KotlinVisibility { PUBLIC, INTERNAL, PROTECTED, PRIVATE }

/**
 * Rules for Kotlin declarations, which read the Kotlin metadata of the compiled classes:
 *
 * ```
 * Taikai.builder()
 *     .namespace("com.company.project")
 *     .kotlin {
 *         classesShouldBeDataClasses(".*Response")
 *         dataClassesShouldBeImmutable()
 *     }
 *     .build()
 *     .checkAll()
 * ```
 *
 * A class is a class, interface, object or companion object declared in Kotlin, but not a file
 * facade such as `MainKt` or a class that the compiler generates, such as one for a lambda. A
 * function is a function that a class or file declares in the source, but not a property accessor
 * or a function that the compiler generates, such as `copy` of a data class. A property is a
 * property that a class or file declares in the source, also one without a backing field.
 * Inherited and delegated members do not count. Regexes match the whole simple name in the source,
 * such as the name of an `internal` function before its JVM name is mangled.
 *
 * @param context the context of the builder
 */
class KotlinConfigurer(context: ConfigurerContext) : AbstractConfigurer(context) {
    /**
     * Removes every Kotlin rule registered on this builder so far.
     *
     * @return this configurer for fluent chaining
     */
    fun disable(): KotlinConfigurer = apply { clear() }

    /**
     * Adds a rule that the simple names of classes must match the given regex.
     *
     * @param regex the regex that class names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun classesShouldMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin class names should match $regex", configuration) { owner, declarations ->
            if (declarations.type != null && !pattern.matches(owner.simpleName))
                listOf("Class ${owner.name} does not match $regex") else emptyList()
        }
    }

    /**
     * Adds a rule that the simple names of classes must not match the given regex.
     *
     * @param regex the regex that class names must not match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun classesShouldNotMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin class names should not match $regex", configuration) { owner, declarations ->
            if (declarations.type != null && pattern.matches(owner.simpleName))
                listOf("Class ${owner.name} matches forbidden pattern $regex") else emptyList()
        }
    }

    /**
     * Adds a rule that the simple names of objects and companion objects must match the given
     * regex. Other classes are not checked.
     *
     * @param regex the regex that object names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun objectsShouldMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin object names should match $regex", configuration) { owner, declarations ->
            val kind = declarations.type?.kind
            if ((kind == ClassKind.OBJECT || kind == ClassKind.COMPANION_OBJECT) && !pattern.matches(owner.simpleName))
                listOf("Object ${owner.name} does not match $regex") else emptyList()
        }
    }

    /**
     * Adds a rule that the names of functions must match the given regex.
     *
     * @param regex the regex that function names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun functionsShouldMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin function names should match $regex", configuration) { owner, declarations ->
            declarations.functions.filterNot { pattern.matches(it.name) }.map {
                "Function ${owner.name}.${it.name}${it.signature?.descriptor.orEmpty()} does not match $regex"
            }
        }
    }

    /**
     * Adds a rule that the names of functions must not match the given regex.
     *
     * @param regex the regex that function names must not match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun functionsShouldNotMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin function names should not match $regex", configuration) { owner, declarations ->
            declarations.functions.filter { pattern.matches(it.name) }.map {
                "Function ${owner.name}.${it.name}${it.signature?.descriptor.orEmpty()} " +
                    "matches forbidden pattern $regex"
            }
        }
    }

    /**
     * Adds a rule that the names of properties, except constants, must match the given regex.
     *
     * @param regex the regex that property names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun propertiesShouldMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin property names should match $regex", configuration) { owner, declarations ->
            declarations.properties.filterNot { it.isConst || pattern.matches(it.name) }.map {
                "Property ${owner.name}.${it.name} does not match $regex"
            }
        }
    }

    /**
     * Adds a rule that the names of properties, except constants, must not match the given regex.
     *
     * @param regex the regex that property names must not match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun propertiesShouldNotMatch(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin property names should not match $regex", configuration) { owner, declarations ->
            declarations.properties.filter { !it.isConst && pattern.matches(it.name) }.map {
                "Property ${owner.name}.${it.name} matches forbidden pattern $regex"
            }
        }
    }

    /**
     * Adds a rule that the names of properties of the given type, except constants, must match the
     * given regex. A property has the type if the type of its declaration or the erased JVM type of
     * its getter or backing field is exactly that type, which is not loaded.
     *
     * @param typeName the fully qualified name of the type, for example `com.company.Matcher`
     * @param regex the regex that property names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     * @throws IllegalArgumentException if [typeName] is blank
     */
    @JvmOverloads
    fun propertiesShouldMatch(
        typeName: String,
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        require(typeName.isNotBlank()) { "Property type name must not be blank" }
        return propertyNamesForTypes(setOf(typeName), regex, configuration)
    }

    /**
     * Same as [propertiesShouldMatch] with a type name, but with the type given as a Java class,
     * which only matches the erased JVM type, so that `Int::class.java` matches `Int` properties,
     * but not `Int?` ones.
     *
     * @param type the type of the properties
     * @param regex the regex that property names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun propertiesShouldMatch(
        type: Class<*>,
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = propertiesShouldMatch(type.name, regex, configuration)

    /**
     * Same as [propertiesShouldMatch] with a type name, but with the type given as a Kotlin class,
     * which matches by its Kotlin and its JVM name, so that `Int::class` matches both `Int` and
     * `Int?` properties.
     *
     * @param type the type of the properties
     * @param regex the regex that property names must match
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun propertiesShouldMatch(
        type: KClass<*>,
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = propertyNamesForTypes(
        setOfNotNull(type.qualifiedName, type.java.name), regex, configuration,
    )

    /**
     * Adds a rule that Kotlin classes must not use classes or members deprecated with Kotlin's or
     * Java's annotation, including deprecated properties and accessors.
     *
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun noUsageOfDeprecatedAPIs(
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = rule("Kotlin classes should not use deprecated APIs", configuration) { owner, _ ->
        KotlinDeprecations().violations(owner)
    }

    /**
     * Adds a rule that the names of constants, the `const val` properties, must consist of
     * uppercase letters, digits and underscores, starting with a letter. Other properties are not
     * checked.
     *
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun constantsShouldFollowConventions(
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer =
        rule("Kotlin const val names should use uppercase underscores", configuration) { owner, declarations ->
            declarations.properties.filter { it.isConst && !constantName.matches(it.name) }.map {
                "Constant ${owner.name}.${it.name} does not use uppercase underscores"
            }
        }

    /**
     * Adds a rule that the classes with simple names matching the given regex must be data classes.
     *
     * @param regex the regex selecting the classes by their simple names
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun classesShouldBeDataClasses(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin classes matching $regex should be data classes", configuration) { owner, declarations ->
            if (declarations.type != null && pattern.matches(owner.simpleName) && !declarations.type.isData)
                listOf("Class ${owner.name} is not a data class") else emptyList()
        }
    }

    /**
     * Adds a rule that data classes must declare only `val` properties, also outside the primary
     * constructor. The objects that the properties reference may still be mutable.
     *
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun dataClassesShouldBeImmutable(
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer =
        rule("Kotlin data classes should declare only val properties", configuration) { owner, declarations ->
            if (declarations.type?.isData == true) declarations.properties.filter { it.isVar }.map {
                "Data class ${owner.name} declares mutable property ${it.name}"
            } else emptyList()
        }

    /**
     * Adds a rule that the classes with simple names matching the given regex must be object
     * declarations. A companion object does not count as one.
     *
     * @param regex the regex selecting the classes by their simple names
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun classesShouldBeObjects(
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin classes matching $regex should be objects", configuration) { owner, declarations ->
            if (declarations.type != null && pattern.matches(owner.simpleName) &&
                declarations.type.kind != ClassKind.OBJECT
            ) {
                listOf("Class ${owner.name} is not an object declaration")
            } else {
                emptyList()
            }
        }
    }

    /**
     * Adds a rule that the classes with simple names matching the given regex must have the given
     * visibility in the source.
     *
     * @param regex the regex selecting the classes by their simple names
     * @param visibility the required visibility
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun classesShouldHaveVisibility(
        regex: String,
        visibility: KotlinVisibility,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin classes matching $regex should be $visibility", configuration) { owner, declarations ->
            if (declarations.type != null && pattern.matches(owner.simpleName) &&
                !matches(declarations.type.visibility, visibility)
            ) {
                listOf("Class ${owner.name} has visibility ${declarations.type.visibility}, expected $visibility")
            } else {
                emptyList()
            }
        }
    }

    /**
     * Adds a rule that the functions with names matching the given regex must have the given
     * visibility in the source, so that an `internal` function counts as internal, although it is
     * public on the JVM.
     *
     * @param regex the regex selecting the functions by their names
     * @param visibility the required visibility
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun functionsShouldHaveVisibility(
        regex: String,
        visibility: KotlinVisibility,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin functions matching $regex should be $visibility", configuration) { owner, declarations ->
            declarations.functions.filter { pattern.matches(it.name) && !matches(it.visibility, visibility) }.map {
                "Function ${owner.name}.${it.name}${it.signature?.descriptor.orEmpty()} " +
                    "has visibility ${it.visibility}, expected $visibility"
            }
        }
    }

    /**
     * Adds a rule that the properties with names matching the given regex must have the given
     * visibility in the source. The visibility of their accessors is not checked.
     *
     * @param regex the regex selecting the properties by their names
     * @param visibility the required visibility
     * @param configuration the rule configuration
     * @return this configurer for fluent chaining
     */
    @JvmOverloads
    fun propertiesShouldHaveVisibility(
        regex: String,
        visibility: KotlinVisibility,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        return rule("Kotlin properties matching $regex should be $visibility", configuration) { owner, declarations ->
            declarations.properties.filter { pattern.matches(it.name) && !matches(it.visibility, visibility) }.map {
                "Property ${owner.name}.${it.name} has visibility ${it.visibility}, expected $visibility"
            }
        }
    }

    private fun propertyNamesForTypes(
        typeNames: Set<String>,
        regex: String,
        configuration: Configuration,
    ): KotlinConfigurer {
        val pattern = Regex(regex)
        val label = typeNames.first()
        val description = "Kotlin properties of type $label should have names matching $regex"
        return rule(description, configuration) { owner, declarations ->
            declarations.properties.filter {
                !it.isConst && typeNames.any { type -> it.hasType(owner, type) } && !pattern.matches(it.name)
            }.map { "Property ${owner.name}.${it.name} of type $label does not match $regex" }
        }
    }

    private fun matches(actual: Visibility, expected: KotlinVisibility): Boolean = when (expected) {
        KotlinVisibility.PUBLIC -> actual == Visibility.PUBLIC
        KotlinVisibility.INTERNAL -> actual == Visibility.INTERNAL
        KotlinVisibility.PROTECTED -> actual == Visibility.PROTECTED
        KotlinVisibility.PRIVATE -> actual == Visibility.PRIVATE || actual == Visibility.PRIVATE_TO_THIS
    }

    private fun rule(
        description: String,
        configuration: Configuration,
        check: (JavaClass, KotlinDeclarations) -> List<String>,
    ): KotlinConfigurer {
        addRule<KotlinConfigurer>(kotlinRule(description, configuration, check))
        return this
    }
}

/** The names of constants: uppercase letters, digits and underscores. */
private val constantName = Regex("[A-Z][A-Z0-9_]*")

/** A rule on Kotlin classes, checked against the declarations of their Kotlin metadata. */
@JvmSynthetic
internal fun kotlinRule(
    description: String,
    configuration: Configuration,
    check: (JavaClass, KotlinDeclarations) -> List<String>,
): TaikaiRule {
    val condition = object : ArchCondition<JavaClass>(description) {
        override fun check(item: JavaClass, events: ConditionEvents) {
            val declarations = try {
                KotlinDeclarations.read(item)
            } catch (exception: Exception) {
                events.add(SimpleConditionEvent.violated(item,
                    "Cannot read Kotlin metadata for ${item.name}: ${exception.message}"))
                return
            }
            check(item, declarations).forEach { events.add(SimpleConditionEvent.violated(item, it)) }
        }
    }
    return TaikaiRule.of(classes().that().areAnnotatedWith("kotlin.Metadata")
        .should(condition).`as`(description), configuration)
}
