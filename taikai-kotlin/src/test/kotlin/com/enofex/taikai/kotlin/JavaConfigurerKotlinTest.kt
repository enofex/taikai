package com.enofex.taikai.kotlin

import com.enofex.taikai.Taikai
import com.enofex.taikai.core.Namespace
import com.enofex.taikai.core.TaikaiRule
import com.enofex.taikai.core.TaikaiRule.Configuration
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.kotlin.fixtures.DefaultDependency
import com.enofex.taikai.kotlin.fixtures.JavaDependency
import com.enofex.taikai.kotlin.fixtures.compatibility.*
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** The rules of [com.enofex.taikai.java.JavaConfigurer] and custom rules on Kotlin bytecode. */
class JavaConfigurerKotlinTest {

    @Test
    fun `generated default helpers retain forbidden bytecode dependencies`() {
        val rule = ArchRuleDefinition.noClasses()
            .that().haveSimpleName("DefaultDependency")
            .should().dependOnClassesThat().haveSimpleName("JavaDependency")
        val imported = ClassFileImporter()
            .importClasses(DefaultDependency::class.java, JavaDependency::class.java)
        val failure = assertViolates(Taikai.builder().classes(imported)
            .addRule(TaikaiRule.of(rule)).build())
        assertTrue(failure.message!!.contains("text$" + "default"))
    }

    @Nested
    inner class MethodsShouldNotDeclareGenericExceptions {

        @Test
        fun `generic exception declarations are checked through Throws annotations`() {
            val failure = assertViolates(Taikai.builder().classes(GenericThrows::class.java)
                .java { it.methodsShouldNotDeclareGenericExceptions() }.build())
            assertTrue(failure.message!!.contains("generic"))
            assertTrue(failure.message!!.contains("runtime"))
            assertPasses(Taikai.builder().classes(SpecificThrows::class.java)
                .java { it.methodsShouldNotDeclareGenericExceptions() }.build())
        }
    }

    @Nested
    inner class Naming {

        @Test
        fun `shared enum and interface naming rules apply to Kotlin declarations`() {
            assertPasses(Taikai.builder().classes(GoodEnum::class.java, Service::class.java)
                .java { it.naming { naming ->
                    naming.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI()
                } }
                .build())
            assertViolates(Taikai.builder().classes(BadEnum::class.java)
                .java { it.naming { naming -> naming.enumConstantsShouldFollowConventions() } }
                .build())
            assertViolates(Taikai.builder().classes(IService::class.java)
                .java { it.naming { naming -> naming.interfacesShouldNotHavePrefixI() } }
                .build())
        }
    }

    @Nested
    inner class Imports {

        @Test
        fun `shaded dependency restriction applies to Kotlin bytecode`() {
            val failure = assertViolates(Taikai.builder().classes(KotlinUsesShaded::class.java)
                .java { it.imports { imports -> imports.shouldNotImport(ImportPatterns.shaded()) } }
                .build())
            assertTrue(failure.message!!.contains("shaded.Shadow"))
        }

        @Test
        fun `package cycle detection includes Kotlin dependencies`() {
            val namespace = "com.enofex.taikai.kotlin.fixtures.compatibility.cycle"
            val failure = assertViolates(Taikai.builder().namespace(namespace)
                .java { it.imports { imports ->
                    imports.shouldHaveNoCycles(
                        Configuration.of(namespace, Namespace.Import.ONLY_TESTS))
                } }.build())
            assertTrue(failure.message!!.contains("Cycle detected"))
            assertTrue(failure.message!!.contains("first"))
            assertTrue(failure.message!!.contains("second"))
        }
    }
}
