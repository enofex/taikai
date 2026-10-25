package com.enofex.taikai.kotlin

import com.enofex.taikai.Taikai
import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.java.JavaConfigurer
import com.enofex.taikai.logging.LoggingConfigurer
import com.enofex.taikai.spring.SpringConfigurer
import com.enofex.taikai.test.TestConfigurer
import com.tngtech.archunit.core.domain.JavaClasses
import kotlin.reflect.KClass

/** Prevents unqualified access to an outer Taikai configuration receiver. */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class TaikaiDslMarker

/** Builds an instance without executing rules. Call check() or checkAll() explicitly. */
fun taikai(block: TaikaiDsl.() -> Unit): Taikai =
    TaikaiDsl(Taikai.builder()).apply(block).build()

/** Adds Kotlin rules to an existing builder. */
fun Taikai.Builder.kotlin(block: @TaikaiDslMarker KotlinConfigurer.() -> Unit): Taikai.Builder =
    configure({ it.block() }, ::KotlinConfigurer)

/** A receiver-based facade; the original Java API remains available without this facade. */
@TaikaiDslMarker
class TaikaiDsl internal constructor(private val builder: Taikai.Builder) {
    fun namespace(value: String) { builder.namespace(value) }
    fun classes(value: JavaClasses) { builder.classes(value) }
    fun classes(vararg value: KClass<*>) { builder.classes(*value.map { it.java }.toTypedArray()) }
    fun excludeClasses(vararg patterns: String) { builder.excludeClasses(*patterns) }
    fun failOnEmpty(value: Boolean = true) { builder.failOnEmpty(value) }
    fun addRule(rule: TaikaiRule) { builder.addRule(rule) }
    fun java(block: @TaikaiDslMarker JavaConfigurer.() -> Unit) { builder.java { it.block() } }
    fun spring(block: @TaikaiDslMarker SpringConfigurer.() -> Unit) { builder.spring { it.block() } }
    fun logging(block: @TaikaiDslMarker LoggingConfigurer.() -> Unit) { builder.logging { it.block() } }
    fun test(block: @TaikaiDslMarker TestConfigurer.() -> Unit) { builder.test { it.block() } }
    fun kotlin(block: @TaikaiDslMarker KotlinConfigurer.() -> Unit) { builder.kotlin(block) }
    internal fun build(): Taikai = builder.build()
}
