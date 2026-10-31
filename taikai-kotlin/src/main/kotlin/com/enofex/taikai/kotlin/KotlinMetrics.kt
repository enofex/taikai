@file:JvmName("KotlinMetrics")

package com.enofex.taikai.kotlin

import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.configures.AbstractConfigurer
import com.enofex.taikai.configures.ConfigurerContext
import com.enofex.taikai.metrics.MetricsConfigurer
import kotlin.metadata.jvm.signature

/**
 * Adds a Kotlin rule to the metrics group: declared functions do not have more than [max] value
 * parameters. Extension and context receivers, suspend continuations, default-argument machinery,
 * generated overloads and constructors do not count.
 */
@JvmOverloads
fun MetricsConfigurer.functionsShouldNotExceedMaxParameters(
    max: Int,
    configuration: Configuration = Configuration.defaultConfiguration(),
): MetricsConfigurer {
    require(max >= 0) { "max must not be negative, but was $max" }
    return configure({ it.functionsShouldNotExceedMaxParameters(max, configuration) }, ::KotlinMetricsConfigurer)
}

/** Holds the Kotlin rules of the metrics group, so that disabling the group removes them as well. */
internal class KotlinMetricsConfigurer(context: ConfigurerContext) : AbstractConfigurer(context) {
    fun functionsShouldNotExceedMaxParameters(max: Int, configuration: Configuration) {
        addRule<KotlinMetricsConfigurer>(
            kotlinRule("Kotlin functions should not have more than $max value parameters", configuration) { owner, declarations ->
                declarations.functions.filter { it.valueParameters.size > max }.map {
                    "Function ${owner.name}.${it.name}${it.signature?.descriptor.orEmpty()} has ${it.valueParameters.size} value parameters, max allowed: $max"
                }
            })
    }
}
