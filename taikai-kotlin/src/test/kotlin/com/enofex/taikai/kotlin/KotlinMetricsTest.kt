package com.enofex.taikai.kotlin

import com.enofex.taikai.Taikai
import com.enofex.taikai.kotlin.fixtures.Plain
import com.tngtech.archunit.core.domain.JavaClass
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KotlinMetricsTest {

    @Test
    fun `parameters exclude default helpers receivers and continuations and identify overloads`() {
        val fileFacade = fixtureClasses.that(JavaClass.Predicates.simpleName("DeclarationFixtures"))
        assertPasses(Taikai.builder().classes(fileFacade)
            .metrics { it.functionsShouldNotExceedMaxParameters(1) }.build())
        assertPasses(metricsRules(Plain::class) { functionsShouldNotExceedMaxParameters(2) })
        val failure = assertViolates(metricsRules(Plain::class) {
            functionsShouldNotExceedMaxParameters(1)
        })
        assertTrue(failure.message!!.contains("overloaded(Ljava/lang/String;Ljava/lang/String;)"))
        assertTrue(failure.message!!.contains("defaults"))
        assertFalse(failure.message!!.contains("suspended"))
        val negative = assertThrows<IllegalArgumentException> {
            metricsRules(Plain::class) { functionsShouldNotExceedMaxParameters(-1) }
        }
        assertEquals("max must not be negative, but was -1", negative.message)
    }

    @Test
    fun `Kotlin function parameters belong to the metrics group`() {
        val instance = taikai {
            classes(Plain::class)
            metrics { functionsShouldNotExceedMaxParameters(1) }
        }
        assertEquals(listOf("Kotlin functions should not have more than 1 value parameters"),
            instance.rules().map { it.archRule().description })
        assertViolates(instance)
        val disabled = taikai {
            classes(Plain::class)
            metrics {
                functionsShouldNotExceedMaxParameters(1)
                disable()
            }
        }
        assertEquals(0, disabled.rules().size)
        assertPasses(disabled)
    }
}
