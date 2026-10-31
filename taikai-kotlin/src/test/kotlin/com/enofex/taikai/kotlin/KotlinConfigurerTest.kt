package com.enofex.taikai.kotlin

import com.enofex.taikai.ClassCriteria
import com.enofex.taikai.Namespace
import com.enofex.taikai.Taikai
import com.enofex.taikai.TaikaiException
import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.checks.GradleWrapperCheck
import com.enofex.taikai.exports.AgentsMarkdownExport
import com.enofex.taikai.kotlin.fixtures.*
import com.enofex.taikai.metrics.MetricsConfigurer
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinConfigurerTest {
    private fun configured(vararg types: Class<*>, block: KotlinConfigurer.() -> Unit): Taikai =
        Taikai.builder().classes(*types).kotlin(block).build()

    private fun measured(vararg types: Class<*>, block: MetricsConfigurer.() -> Unit): Taikai =
        Taikai.builder().classes(*types).metrics { it.block() }.build()

    @Test
    fun `class and object names use source declarations`() {
        configured(GoodDto::class.java) { classesShouldMatch("GoodDto") }.check()
        assertThrows<AssertionError> { configured(GoodDto::class.java) { classesShouldMatch("Other") }.check() }
        configured(Registry::class.java) { objectsShouldMatch("Registry") }.check()
        assertThrows<AssertionError> { configured(Registry::class.java) { objectsShouldMatch("Other") }.check() }
        configured(Plain.Companion::class.java) { objectsShouldMatch("Companion") }.check()
    }

    @Test
    fun `source functions exclude getters and generated data class methods`() {
        configured(GoodDto::class.java) { functionsShouldMatch("noDeclaredFunctions") }.check()
        configured(Plain::class.java) { functionsShouldMatch("[a-z][a-zA-Z0-9]*") }.check()
        val failure = assertThrows<AssertionError> {
            configured(BadNames::class.java) { functionsShouldMatch("[a-z].*") }.checkAll()
        }
        assertTrue(failure.message!!.contains("BadFunction"))
    }

    @Test
    fun `properties are checked independently from const vals and backing fields`() {
        configured(Plain.Companion::class.java) {
            propertiesShouldMatch("[a-z].*")
            constantsShouldFollowConventions()
        }.checkAll()
        assertThrows<AssertionError> { configured(BadNames::class.java) { propertiesShouldMatch("[a-z].*") }.check() }
        assertThrows<AssertionError> { configured(BadConstants::class.java) { constantsShouldFollowConventions() }.check() }
    }

    @Test
    fun `data class and object requirements distinguish source kinds`() {
        configured(GoodDto::class.java) { classesShouldBeDataClasses(".*Dto"); dataClassesShouldBeImmutable() }.checkAll()
        assertThrows<AssertionError> { configured(WrongDto::class.java) { classesShouldBeDataClasses(".*Dto") }.check() }
        val failure = assertThrows<AssertionError> { configured(MutableDto::class.java) { dataClassesShouldBeImmutable() }.checkAll() }
        assertTrue(failure.message!!.contains("name")); assertTrue(failure.message!!.contains("extra"))
        configured(Registry::class.java) { classesShouldBeObjects("Registry") }.check()
        assertThrows<AssertionError> { configured(Ordinary::class.java) { classesShouldBeObjects("Ordinary") }.check() }
        assertThrows<AssertionError> { configured(Plain.Companion::class.java) { classesShouldBeObjects("Companion") }.check() }
    }

    @Test
    fun `visibility comes from metadata including mangled internal functions`() {
        configured(InternalType::class.java) { classesShouldHaveVisibility("InternalType", KotlinVisibility.INTERNAL) }.check()
        configured(Plain::class.java) {
            functionsShouldHaveVisibility("internalWork", KotlinVisibility.INTERNAL)
            propertiesShouldHaveVisibility("internalValue", KotlinVisibility.INTERNAL)
            propertiesShouldHaveVisibility("secret", KotlinVisibility.PRIVATE)
        }.checkAll()
        assertThrows<AssertionError> { configured(InternalType::class.java) { classesShouldHaveVisibility(".*", KotlinVisibility.PUBLIC) }.check() }
        assertThrows<AssertionError> { configured(Plain::class.java) { functionsShouldHaveVisibility("internalWork", KotlinVisibility.PUBLIC) }.check() }
        assertThrows<AssertionError> { configured(Plain::class.java) { propertiesShouldHaveVisibility("internalValue", KotlinVisibility.PUBLIC) }.check() }
    }

    @Test
    fun `parameters exclude default helpers receivers and continuations and identify overloads`() {
        val imported = ClassFileImporter().importPackages("com.enofex.taikai.kotlin.fixtures")
        val fileFacade = imported.that(JavaClass.Predicates.simpleName("DeclarationFixtures"))
        Taikai.builder().classes(fileFacade).metrics { it.functionsShouldNotExceedMaxParameters(1) }.build().checkAll()
        measured(Plain::class.java) { functionsShouldNotExceedMaxParameters(2) }.checkAll()
        val failure = assertThrows<AssertionError> { measured(Plain::class.java) { functionsShouldNotExceedMaxParameters(1) }.checkAll() }
        assertTrue(failure.message!!.contains("overloaded(Ljava/lang/String;Ljava/lang/String;)"))
        assertTrue(failure.message!!.contains("defaults"))
        assertFalse(failure.message!!.contains("suspended"))
        val negative = assertThrows<IllegalArgumentException> { measured(Plain::class.java) { functionsShouldNotExceedMaxParameters(-1) } }
        assertEquals("max must not be negative, but was -1", negative.message)
    }

    @Test
    fun `rules accumulate disable and extend an existing configuration`() {
        val builder = Taikai.builder().classes(GoodDto::class.java)
            .kotlin { classesShouldMatch("GoodDto") }.kotlin { dataClassesShouldBeImmutable() }
        val original = builder.build()
        assertEquals(2, original.rules().size)
        val extended = original.toBuilder().kotlin { classesShouldBeDataClasses(".*Dto") }.build()
        assertEquals(3, extended.rules().size)
        assertEquals(2, original.rules().size)
        extended.checkAll()
        builder.kotlin { disable() }
        assertEquals(0, builder.build().rules().size)
    }

    @Test
    fun `per rule namespace exclusion and imports work`() {
        Taikai.builder().namespace("com.enofex.taikai.kotlin.fixtures")
            .kotlin {
                classesShouldMatch("GoodDto", Configuration.of(Namespace.IMPORT.WITH_TESTS,
                    listOf("com.enofex.taikai.kotlin.fixtures..")))
            }.build().checkAll()
        val imported = ClassFileImporter().importClasses(GoodDto::class.java)
        Taikai.builder().kotlin {
            classesShouldBeDataClasses(".*Dto", Configuration.of(imported))
        }.build().check()
    }

    @Test
    fun `explicit imports respect per rule exclusions`() {
        val imported = ClassFileImporter().importClasses(MutableDto::class.java)
        Taikai.builder().kotlin {
            dataClassesShouldBeImmutable(Configuration.of(imported, listOf(MutableDto::class.java.name)))
        }.build().checkAll()
    }

    @Test
    fun `selected Kotlin classes count for empty selection but Java classes do not`() {
        configured(String::class.java) { classesShouldMatch("anything") }.check()
        assertThrows<AssertionError> {
            Taikai.builder().classes(String::class.java).failOnEmpty(true)
                .kotlin { classesShouldMatch("anything") }.build().check()
        }
    }

    @Test
    fun `delegated functions are skipped while explicit overrides are checked`() {
        configured(DelegatingWorker::class.java) { functionsShouldMatch("noDeclaredFunctions") }.check()
        assertThrows<AssertionError> {
            configured(OverridingWorker::class.java) { functionsShouldMatch("noDeclaredFunctions") }.check()
        }
    }

    @Test
    fun `multi file facade helpers are skipped while part declarations are analyzed`() {
        val imported = ClassFileImporter().importPackages("com.enofex.taikai.kotlin.fixtures")
            .that(JavaClass.Predicates.simpleNameStartingWith("MultiFileFixtures"))
        Taikai.builder().classes(imported).failOnEmpty(true)
            .kotlin { functionsShouldMatch("multiFile(One|Two)") }
            .metrics { it.functionsShouldNotExceedMaxParameters(1) }
            .build().checkAll()
        val failure = assertThrows<AssertionError> {
            Taikai.builder().classes(imported).metrics { it.functionsShouldNotExceedMaxParameters(0) }
                .build().checkAll()
        }
        assertTrue(failure.message!!.contains("multiFileOne"))
        assertTrue(failure.message!!.contains("multiFileTwo"))
    }

    @Test
    fun `generated default helpers retain forbidden bytecode dependencies`() {
        val rule = com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses()
            .that().haveSimpleName("DefaultDependency")
            .should().dependOnClassesThat().haveSimpleName("JavaDependency")
        val imported = ClassFileImporter().importClasses(DefaultDependency::class.java, JavaDependency::class.java)
        val failure = assertThrows<AssertionError> {
            Taikai.builder().classes(imported).addRule(com.enofex.taikai.TaikaiRule.of(rule))
                .build().checkAll()
        }
        assertTrue(failure.message!!.contains("text$" + "default"))
    }

    @Test
    fun `invalid naming patterns fail at configuration time`() {
        assertThrows<IllegalArgumentException> { configured(Plain::class.java) { classesShouldMatch("[") } }
        assertThrows<IllegalArgumentException> { configured(Registry::class.java) { objectsShouldMatch("[") } }
    }

    @Test
    fun `metadata import does not initialize application classes`() {
        Taikai.builder().classes(Explosive::class.java)
            .kotlin { noUsageOfDeprecatedAPIs() }
            .metrics { it.functionsShouldNotExceedMaxParameters(1) }
            .build().checkAll()
    }

    @Test
    fun `receiver DSL builds without executing and preserves shared rules`() {
        val instance = taikai {
            classes(GoodDto::class)
            java { classesShouldResideInPackage("com.enofex.taikai.kotlin.fixtures..") }
            kotlin { dataClassesShouldBeImmutable() }
        }
        instance.checkAll()
        val invalid = taikai { classes(MutableDto::class); kotlin { dataClassesShouldBeImmutable() } }
        assertThrows<AssertionError> { invalid.checkAll() }
    }

    @Test
    fun `receiver DSL excludes classes by literal and by criteria`() {
        val withTests = Configuration.of(Namespace.IMPORT.WITH_TESTS)
        val fixtures = "com.enofex.taikai.kotlin.fixtures"
        assertThrows<AssertionError> {
            taikai { namespace(fixtures); kotlin { dataClassesShouldBeImmutable(withTests) } }.checkAll()
        }
        taikai {
            namespace(fixtures)
            excludeClasses(MutableDto::class)
            kotlin { dataClassesShouldBeImmutable(withTests) }
        }.checkAll()
        taikai {
            namespace(fixtures)
            excludeClasses(ClassCriteria.simpleNameMatching("MutableDto"))
            kotlin { dataClassesShouldBeImmutable(withTests) }
        }.checkAll()
    }

    @Test
    fun `receiver DSL registers rules for every rule group and discovers checks`() {
        val rule = TaikaiRule.of(ArchRuleDefinition.classes().should().haveSimpleNameNotEndingWith("Impl"))
        val instance = taikai {
            classes(GoodDto::class)
            addRules(listOf(rule, rule))
            quarkus { noInjectionFields() }
            jpa { entities { it.shouldBePackagePrivate() } }
            metrics { classesShouldNotExceedMaxInheritanceDepth(3) }
            discoverChecks()
        }
        assertEquals(5, instance.rules().size)
        assertEquals(5, instance.checks().size)
        assertThrows<TaikaiException> {
            taikai {
                classes(GoodDto::class)
                discoverChecks { configure(GradleWrapperCheck::class.java, GradleWrapperCheck.directories("..")) }
            }
        }
    }

    @Test
    fun `Kotlin function parameters belong to the metrics group`() {
        val instance = taikai {
            classes(Plain::class)
            metrics { functionsShouldNotExceedMaxParameters(1) }
        }
        assertEquals(listOf("Kotlin functions should not have more than 1 value parameters"),
            instance.rules().map { it.archRule().description })
        assertThrows<AssertionError> { instance.checkAll() }
        val disabled = taikai {
            classes(Plain::class)
            metrics {
                functionsShouldNotExceedMaxParameters(1)
                disable()
            }
        }
        assertTrue(disabled.rules().isEmpty())
    }

    @Test
    fun `receiver DSL discovers exports`() {
        val instance = taikai {
            classes(GoodDto::class)
            discoverExports { configure(AgentsMarkdownExport::class.java, AgentsMarkdownExport.file("ARCHITECTURE.md")) }
        }
        assertEquals(listOf(AgentsMarkdownExport::class.java), instance.exports().map { it.javaClass })
    }
}
