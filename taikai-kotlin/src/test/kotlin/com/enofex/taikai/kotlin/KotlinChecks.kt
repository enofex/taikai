package com.enofex.taikai.kotlin

import com.enofex.taikai.Namespace
import com.enofex.taikai.Taikai
import com.enofex.taikai.metrics.MetricsConfigurer
import com.tngtech.archunit.core.domain.JavaClasses
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass

/** The fixtures, imported once by the shared cache of [Namespace]. */
internal val fixtureClasses: JavaClasses
    get() = Namespace.withTests("com.enofex.taikai.kotlin.fixtures")

/** An instance with the Kotlin rules of [block] for exactly the given classes. */
internal fun kotlinRules(vararg types: KClass<*>, block: KotlinConfigurer.() -> Unit): Taikai =
    configured(types) { kotlin(block) }

/** An instance with the metrics rules of [block] for exactly the given classes. */
internal fun metricsRules(vararg types: KClass<*>, block: MetricsConfigurer.() -> Unit): Taikai =
    configured(types) { metrics { block(it) } }

/** Asserts that no rule or check of [taikai] is violated. */
internal fun assertPasses(taikai: Taikai) {
    assertDoesNotThrow { taikai.checkAll() }
}

/** Asserts that a rule or check of [taikai] fails and returns the report of all violations. */
internal fun assertViolates(taikai: Taikai): AssertionError =
    assertThrows<AssertionError> { taikai.checkAll() }

private fun configured(
    types: Array<out KClass<*>>,
    block: Taikai.Builder.() -> Taikai.Builder,
): Taikai = block(Taikai.builder().classes(*types.map { it.java }.toTypedArray())).build()
