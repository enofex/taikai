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

/** Optional Kotlin/JVM rules. Java rule semantics remain unchanged. Patterns match entire source simple names. */
class KotlinConfigurer(context: ConfigurerContext) : AbstractConfigurer(context) {
    /** Removes every Kotlin rule registered on this builder so far. */
    fun disable(): KotlinConfigurer = apply { clear() }

    /** Adds a rule that the simple name of every declared Kotlin class must match the given regex. */
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
     * Adds a rule that source class names must not match the given regex, excluding file facades
     * and synthetic helpers.
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
     * Adds a rule that regular and companion object names must match the given regex; other class
     * kinds are ignored.
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
     * Adds a rule that declared source function names must match the given regex, excluding
     * accessors and generated helpers.
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
     * Adds a rule that source function names, including names before internal JVM mangling, must
     * not match the given regex.
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
     * Adds a rule that non-constant source property names must match the given regex, independently
     * of JVM backing fields.
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
     * Adds a rule that declared non-constant property names, including computed properties, must
     * not match the given regex.
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
     * Adds a rule that the names of properties of an exact source classifier or erased JVM type
     * must match the given regex, without loading the type.
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

    /** Java-friendly overload for selecting an exact erased property type. */
    @JvmOverloads
    fun propertiesShouldMatch(
        type: Class<*>,
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = propertiesShouldMatch(type.name, regex, configuration)

    /** Kotlin-friendly overload; source classifiers preserve nullable primitive type matching. */
    @JvmOverloads
    fun propertiesShouldMatch(
        type: KClass<*>,
        regex: String,
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = propertyNamesForTypes(
        setOfNotNull(type.qualifiedName, type.java.name), regex, configuration,
    )

    /**
     * Adds a rule that Kotlin classes must not use deprecated APIs, checking Kotlin and Java
     * deprecated classes and member accesses from Kotlin/JVM bytecode.
     */
    @JvmOverloads
    fun noUsageOfDeprecatedAPIs(
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer = rule("Kotlin classes should not use deprecated APIs", configuration) { owner, _ ->
        KotlinDeprecations().violations(owner)
    }

    /** Adds a rule that const val names must use uppercase underscore naming; other properties are not checked. */
    @JvmOverloads
    fun constantsShouldFollowConventions(
        configuration: Configuration = Configuration.defaultConfiguration(),
    ): KotlinConfigurer =
        rule("Kotlin const val names should use uppercase underscores", configuration) { owner, declarations ->
            declarations.properties.filter { it.isConst && !constantName.matches(it.name) }.map {
                "Constant ${owner.name}.${it.name} does not use uppercase underscores"
            }
        }

    /** Adds a rule that Kotlin classes selected by simple-name regex must be data classes. */
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
     * Adds a rule that data classes must declare only val properties, including properties outside
     * the primary constructor. Not deep immutability.
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
     * Adds a rule that Kotlin classes selected by simple-name regex must be objects. Companion
     * objects do not satisfy this.
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

    /** Adds a rule that selected Kotlin classes must have the requested source visibility. */
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
     * Adds a rule that selected source functions, including JVM-mangled internal functions, must
     * have the requested source visibility.
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
     * Adds a rule that selected source properties must have the requested source visibility;
     * accessor visibility is not checked separately.
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
