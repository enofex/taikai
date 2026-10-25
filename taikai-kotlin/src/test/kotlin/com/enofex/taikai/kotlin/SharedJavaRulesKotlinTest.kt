package com.enofex.taikai.kotlin

import com.enofex.taikai.Namespace
import com.enofex.taikai.Taikai
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.kotlin.fixtures.compatibility.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SharedJavaRulesKotlinTest {
    @Test
    fun `generic exception declarations are checked through Throws annotations`() {
        val failure = assertThrows<AssertionError> {
            Taikai.builder().classes(GenericThrows::class.java)
                .java { it.methodsShouldNotDeclareGenericExceptions() }.build().checkAll()
        }
        assertTrue(failure.message!!.contains("generic"))
        assertTrue(failure.message!!.contains("runtime"))
        Taikai.builder().classes(SpecificThrows::class.java)
            .java { it.methodsShouldNotDeclareGenericExceptions() }.build().check()
    }

    @Test
    fun `shared enum and interface naming rules apply to Kotlin declarations`() {
        Taikai.builder().classes(GoodEnum::class.java, Service::class.java)
            .java { it.naming { naming -> naming.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI() } }
            .build().checkAll()
        assertThrows<AssertionError> {
            Taikai.builder().classes(BadEnum::class.java)
                .java { it.naming { naming -> naming.enumConstantsShouldFollowConventions() } }.build().check()
        }
        assertThrows<AssertionError> {
            Taikai.builder().classes(IService::class.java)
                .java { it.naming { naming -> naming.interfacesShouldNotHavePrefixI() } }.build().check()
        }
    }

    @Test
    fun `shaded dependency restriction applies to Kotlin bytecode`() {
        val failure = assertThrows<AssertionError> {
            Taikai.builder().classes(KotlinUsesShaded::class.java)
                .java { it.imports { imports -> imports.shouldNotImport(ImportPatterns.shaded()) } }
                .build().check()
        }
        assertTrue(failure.message!!.contains("shaded.Shadow"))
    }

    @Test
    fun `package cycle detection includes Kotlin dependencies`() {
        val namespace = "com.enofex.taikai.kotlin.fixtures.compatibility.cycle"
        val failure = assertThrows<AssertionError> {
            Taikai.builder().namespace(namespace)
                .java { it.imports { imports ->
                    imports.shouldHaveNoCycles(Configuration.of(namespace, Namespace.IMPORT.ONLY_TESTS))
                } }.build().check()
        }
        assertTrue(failure.message!!.contains("Cycle detected"))
        assertTrue(failure.message!!.contains("first"))
        assertTrue(failure.message!!.contains("second"))
    }

    @Test
    fun `Kotlin utility objects use the source object rule`() {
        Taikai.builder().classes(UtilityRegistry::class.java)
            .kotlin { classesShouldBeObjects(".*Registry") }.build().check()
        assertThrows<AssertionError> {
            Taikai.builder().classes(Service::class.java)
                .kotlin { classesShouldBeObjects("Service") }.build().check()
        }
    }
}
