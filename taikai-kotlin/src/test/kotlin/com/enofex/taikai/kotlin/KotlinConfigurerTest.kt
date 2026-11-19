package com.enofex.taikai.kotlin

import com.enofex.taikai.Taikai
import com.enofex.taikai.core.Namespace
import com.enofex.taikai.core.TaikaiRule.Configuration
import com.enofex.taikai.kotlin.fixtures.*
import com.enofex.taikai.kotlin.fixtures.compatibility.*
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

class KotlinConfigurerTest {

    @Test
    fun `rules accumulate disable and extend an existing configuration`() {
        val builder = Taikai.builder().classes(GoodDto::class.java)
            .kotlin { classesShouldMatch("GoodDto") }.kotlin { dataClassesShouldBeImmutable() }
        val original = builder.build()
        assertEquals(2, original.rules().size)
        val extended = original.toBuilder().kotlin { classesShouldBeDataClasses(".*Dto") }.build()
        assertEquals(3, extended.rules().size)
        assertEquals(2, original.rules().size)
        assertPasses(extended)
        builder.kotlin { disable() }
        assertEquals(0, builder.build().rules().size)
    }

    @Test
    fun `per rule namespace exclusion and imports work`() {
        assertPasses(Taikai.builder().namespace("com.enofex.taikai.kotlin.fixtures")
            .kotlin {
                classesShouldMatch("GoodDto", Configuration.of(Namespace.Import.WITH_TESTS,
                    listOf("com.enofex.taikai.kotlin.fixtures..")))
            }.build())
        val imported = ClassFileImporter().importClasses(GoodDto::class.java)
        assertPasses(Taikai.builder().kotlin {
            classesShouldBeDataClasses(".*Dto", Configuration.of(imported))
        }.build())
    }

    @Test
    fun `explicit imports respect per rule exclusions`() {
        val imported = ClassFileImporter().importClasses(MutableDto::class.java)
        assertPasses(Taikai.builder().kotlin {
            dataClassesShouldBeImmutable(
                Configuration.of(imported, listOf(MutableDto::class.java.name)))
        }.build())
    }

    @Test
    fun `rules with their own configuration honor global exclusions`() {
        assertPasses(Taikai.builder().namespace("com.enofex.taikai.kotlin.fixtures.compatibility")
            .excludeClasses("com.enofex.taikai.kotlin.fixtures.compatibility..")
            .kotlin {
                classesShouldNotMatch(".*", Configuration.of(Namespace.Import.WITH_TESTS))
                noUsageOfDeprecatedAPIs(Configuration.of(Namespace.Import.WITH_TESTS))
                propertiesShouldMatch(Matcher::class, "none",
                    Configuration.of(Namespace.Import.WITH_TESTS))
            }.build())
    }

    @Test
    fun `selected Kotlin classes count for empty selection but Java classes do not`() {
        assertPasses(kotlinRules(String::class) { classesShouldMatch("anything") })
        assertViolates(Taikai.builder().classes(String::class.java).failOnEmpty(true)
            .kotlin { classesShouldMatch("anything") }.build())
    }

    @Test
    fun `multi file facade helpers are skipped while part declarations are analyzed`() {
        val imported = fixtureClasses
            .that(JavaClass.Predicates.simpleNameStartingWith("MultiFileFixtures"))
        assertPasses(Taikai.builder().classes(imported).failOnEmpty(true)
            .kotlin { functionsShouldMatch("multiFile(One|Two)") }
            .metrics { it.functionsShouldNotExceedMaxParameters(1) }
            .build())
        val failure = assertViolates(Taikai.builder().classes(imported)
            .metrics { it.functionsShouldNotExceedMaxParameters(0) }
            .build())
        assertTrue(failure.message!!.contains("multiFileOne"))
        assertTrue(failure.message!!.contains("multiFileTwo"))
    }

    @Test
    fun `invalid naming patterns fail at configuration time`() {
        assertThrows<IllegalArgumentException> {
            kotlinRules(Plain::class) { classesShouldMatch("[") }
        }
        assertThrows<IllegalArgumentException> {
            kotlinRules(Registry::class) { objectsShouldMatch("[") }
        }
        assertThrows<IllegalArgumentException> {
            kotlinRules(GoodDto::class) { functionsShouldNotMatch("[") }
        }
        assertThrows<IllegalArgumentException> {
            kotlinRules(GoodDto::class) { propertiesShouldNotMatch("[") }
        }
    }

    @Test
    fun `metadata import does not initialize application classes`() {
        assertPasses(Taikai.builder().classes(Explosive::class.java)
            .kotlin { noUsageOfDeprecatedAPIs() }
            .metrics { it.functionsShouldNotExceedMaxParameters(1) }
            .build())
    }

    @Nested
    inner class ClassesShouldMatch {

        @Test
        fun `class names use source declarations`() {
            assertPasses(kotlinRules(GoodDto::class) { classesShouldMatch("GoodDto") })
            assertViolates(kotlinRules(GoodDto::class) { classesShouldMatch("Other") })
        }
    }

    @Nested
    inner class ClassesShouldNotMatch {

        @Test
        fun `matching source class names are reported`() {
            assertViolates(kotlinRules(BadImplementationImpl::class) {
                classesShouldNotMatch(".*Impl")
            })
            assertPasses(kotlinRules(GoodDto::class) { classesShouldNotMatch(".*Impl") })
        }
    }

    @Nested
    inner class ObjectsShouldMatch {

        @Test
        fun `object and companion object names use source declarations`() {
            assertPasses(kotlinRules(Registry::class) { objectsShouldMatch("Registry") })
            assertViolates(kotlinRules(Registry::class) { objectsShouldMatch("Other") })
            assertPasses(kotlinRules(Plain.Companion::class) { objectsShouldMatch("Companion") })
        }
    }

    @Nested
    inner class FunctionsShouldMatch {

        @Test
        fun `source functions exclude getters and generated data class methods`() {
            assertPasses(kotlinRules(GoodDto::class) {
                functionsShouldMatch("noDeclaredFunctions")
            })
            assertPasses(kotlinRules(Plain::class) { functionsShouldMatch("[a-z][a-zA-Z0-9]*") })
            val failure = assertViolates(kotlinRules(BadNames::class) {
                functionsShouldMatch("[a-z].*")
            })
            assertTrue(failure.message!!.contains("BadFunction"))
        }

        @Test
        fun `delegated functions are skipped while explicit overrides are checked`() {
            assertPasses(kotlinRules(DelegatingWorker::class) {
                functionsShouldMatch("noDeclaredFunctions")
            })
            assertViolates(kotlinRules(OverridingWorker::class) {
                functionsShouldMatch("noDeclaredFunctions")
            })
        }
    }

    @Nested
    inner class FunctionsShouldNotMatch {

        @Test
        fun `source names of internal functions and functions with defaults are reported`() {
            val failure = assertViolates(kotlinRules(NegativeNames::class) {
                functionsShouldNotMatch("^(foo|bar)$")
            })
            assertTrue(failure.message!!.contains(".foo("))
            assertTrue(failure.message!!.contains(".bar("))
            assertFalse(failure.message!!.contains("\$default"))
            assertPasses(kotlinRules(GoodDto::class) { functionsShouldNotMatch("^(foo|bar)$") })
        }
    }

    @Nested
    inner class PropertiesShouldMatch {

        @Test
        fun `properties are checked independently from const vals and backing fields`() {
            assertPasses(kotlinRules(Plain.Companion::class) { propertiesShouldMatch("[a-z].*") })
            assertViolates(kotlinRules(BadNames::class) { propertiesShouldMatch("[a-z].*") })
        }

        @Test
        fun `type selection includes nullable computed delegated and stored properties`() {
            val failure = assertViolates(kotlinRules(TypedProperties::class) {
                propertiesShouldMatch(Matcher::class, "matcher")
            })
            listOf("computed", "delegated", "nullable").forEach {
                assertTrue(failure.message!!.contains(it))
            }
            assertFalse(failure.message!!.contains("unrelated"))
            assertFalse(failure.message!!.contains("\$delegate"))
            assertPasses(kotlinRules(CorrectProperties::class) {
                propertiesShouldMatch(Matcher::class.java, "matcher")
            })
            assertPasses(kotlinRules(CorrectProperties::class) {
                propertiesShouldMatch(Matcher::class.java.name, "matcher")
            })
            assertViolates(kotlinRules(AliasProperty::class) {
                propertiesShouldMatch(Matcher::class, "matcher")
            })
        }

        @Test
        fun `type matching recognizes source and erased JVM types`() {
            assertPasses(kotlinRules(CollectionProperties::class) {
                propertiesShouldMatch(List::class, "wrong")
            })
            val collection = assertViolates(kotlinRules(CollectionProperties::class) {
                propertiesShouldMatch("java.util.List", "items")
            })
            assertTrue(collection.message!!.contains(".wrong"))
            val primitive = assertViolates(kotlinRules(PrimitiveProperties::class) {
                propertiesShouldMatch(Int::class, "number")
            })
            assertTrue(primitive.message!!.contains(".primitive"))
            assertTrue(primitive.message!!.contains(".nullable"))
        }

        @Test
        fun `blank type names fail at configuration time`() {
            assertThrows<IllegalArgumentException> {
                kotlinRules(GoodDto::class) { propertiesShouldMatch("", "name") }
            }
        }
    }

    @Nested
    inner class PropertiesShouldNotMatch {

        @Test
        fun `source names of computed and delegated properties are reported`() {
            val failure = assertViolates(kotlinRules(NegativeNames::class) {
                propertiesShouldNotMatch(".*(List|Set|Map)$")
            })
            assertTrue(failure.message!!.contains("namesList"))
            assertTrue(failure.message!!.contains("idsSet"))
            assertFalse(failure.message!!.contains("\$delegate"))
            assertPasses(kotlinRules(GoodDto::class) {
                propertiesShouldNotMatch(".*(List|Set|Map)$")
            })
        }
    }

    @Nested
    inner class NoUsageOfDeprecatedAPIs {

        @ParameterizedTest
        @MethodSource("com.enofex.taikai.kotlin.KotlinConfigurerTest#deprecatedUsages")
        fun `deprecated Kotlin API uses are reported`(type: KClass<*>) {
            val failure = assertViolates(kotlinRules(type) { noUsageOfDeprecatedAPIs() })
            assertTrue(failure.message!!.contains("deprecated"), failure.message)
            assertTrue(failure.message!!.contains(type.simpleName!!), failure.message)
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("com.enofex.taikai.kotlin.KotlinConfigurerTest#allowedUsages")
        fun `non deprecated overloads and accessor directions remain allowed`(type: KClass<*>) {
            assertPasses(kotlinRules(type) { noUsageOfDeprecatedAPIs() })
        }
    }

    @Nested
    inner class ConstantsShouldFollowConventions {

        @Test
        fun `const vals are checked independently from other properties`() {
            assertPasses(kotlinRules(Plain.Companion::class) { constantsShouldFollowConventions() })
            assertViolates(kotlinRules(BadConstants::class) { constantsShouldFollowConventions() })
        }
    }

    @Nested
    inner class ClassesShouldBeDataClasses {

        @Test
        fun `selected classes must be data classes`() {
            assertPasses(kotlinRules(GoodDto::class) { classesShouldBeDataClasses(".*Dto") })
            assertViolates(kotlinRules(WrongDto::class) { classesShouldBeDataClasses(".*Dto") })
        }
    }

    @Nested
    inner class DataClassesShouldBeImmutable {

        @Test
        fun `mutable properties of data classes are reported`() {
            assertPasses(kotlinRules(GoodDto::class) { dataClassesShouldBeImmutable() })
            val failure = assertViolates(kotlinRules(MutableDto::class) {
                dataClassesShouldBeImmutable()
            })
            assertTrue(failure.message!!.contains("name"))
            assertTrue(failure.message!!.contains("extra"))
        }
    }

    @Nested
    inner class ClassesShouldBeObjects {

        @Test
        fun `only object declarations satisfy the rule`() {
            assertPasses(kotlinRules(Registry::class) { classesShouldBeObjects("Registry") })
            assertPasses(kotlinRules(UtilityRegistry::class) {
                classesShouldBeObjects(".*Registry")
            })
            assertViolates(kotlinRules(Ordinary::class) { classesShouldBeObjects("Ordinary") })
            assertViolates(kotlinRules(Service::class) { classesShouldBeObjects("Service") })
            assertViolates(kotlinRules(Plain.Companion::class) {
                classesShouldBeObjects("Companion")
            })
        }
    }

    @Nested
    inner class ClassesShouldHaveVisibility {

        @Test
        fun `visibility comes from metadata`() {
            assertPasses(kotlinRules(InternalType::class) {
                classesShouldHaveVisibility("InternalType", KotlinVisibility.INTERNAL)
            })
            assertViolates(kotlinRules(InternalType::class) {
                classesShouldHaveVisibility(".*", KotlinVisibility.PUBLIC)
            })
        }
    }

    @Nested
    inner class FunctionsShouldHaveVisibility {

        @Test
        fun `visibility comes from metadata including mangled internal functions`() {
            assertPasses(kotlinRules(Plain::class) {
                functionsShouldHaveVisibility("internalWork", KotlinVisibility.INTERNAL)
            })
            assertViolates(kotlinRules(Plain::class) {
                functionsShouldHaveVisibility("internalWork", KotlinVisibility.PUBLIC)
            })
        }
    }

    @Nested
    inner class PropertiesShouldHaveVisibility {

        @Test
        fun `visibility comes from metadata`() {
            assertPasses(kotlinRules(Plain::class) {
                propertiesShouldHaveVisibility("internalValue", KotlinVisibility.INTERNAL)
                propertiesShouldHaveVisibility("secret", KotlinVisibility.PRIVATE)
            })
            assertViolates(kotlinRules(Plain::class) {
                propertiesShouldHaveVisibility("internalValue", KotlinVisibility.PUBLIC)
            })
        }
    }

    companion object {

        @JvmStatic
        fun allowedUsages(): List<KClass<*>> = listOf(
            CallsCurrentOverload::class, WritesOldGetter::class, ReadsOldSetter::class,
            OldApi::class, OldConstructor::class, CompanionProperties::class,
            DeprecatedOverloads::class, DeprecatedData::class,
        )

        @JvmStatic
        fun deprecatedUsages(): List<KClass<*>> = listOf(
            CallsOldFunction::class, CallsOldDefaultFunction::class, ReadsOldProperty::class,
            ReadsOldComputed::class, ReadsOldGetter::class, WritesOldSetter::class,
            UsesOldType::class, CallsOldConstructor::class, CallsOldDefaultConstructor::class,
            CallsOldObjectDefault::class, ReadsCompanionField::class, ReadsCompanionComputed::class,
            CallsOldTopLevel::class, ReadsOldTopLevel::class,
            ReferencesOldMethod::class, ReferencesOldConstructor::class,
            ReadsJavaDeprecatedField::class, CallsJavaDeprecatedMethod::class,
            CallsJavaDeprecatedConstructor::class, DeprecatedDefaultExpression::class,
            ReadsOldContract::class,
        )
    }
}
