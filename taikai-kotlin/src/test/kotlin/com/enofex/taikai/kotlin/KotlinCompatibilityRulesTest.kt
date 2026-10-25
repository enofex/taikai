package com.enofex.taikai.kotlin

import com.enofex.taikai.Namespace
import com.enofex.taikai.Taikai
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.kotlin.fixtures.GoodDto
import com.enofex.taikai.kotlin.fixtures.compatibility.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass

class KotlinCompatibilityRulesTest {
    private fun check(type: KClass<*>, block: KotlinConfigurer.() -> Unit) =
        Taikai.builder().classes(type.java).kotlin(block).build().checkAll()

    @Test
    fun `negative rules use source names including internal and computed declarations`() {
        assertThrows<AssertionError> { check(BadImplementationImpl::class) { classesShouldNotMatch(".*Impl") } }
        val functions = assertThrows<AssertionError> {
            check(NegativeNames::class) { functionsShouldNotMatch("^(foo|bar)$") }
        }
        assertTrue(functions.message!!.contains(".foo("))
        assertTrue(functions.message!!.contains(".bar("))
        assertFalse(functions.message!!.contains("\$default"))
        val properties = assertThrows<AssertionError> {
            check(NegativeNames::class) { propertiesShouldNotMatch(".*(List|Set|Map)$") }
        }
        assertTrue(properties.message!!.contains("namesList"))
        assertTrue(properties.message!!.contains("idsSet"))
        assertFalse(properties.message!!.contains("\$delegate"))
        check(GoodDto::class) {
            classesShouldNotMatch(".*Impl")
            functionsShouldNotMatch("^(foo|bar)$")
            propertiesShouldNotMatch(".*(List|Set|Map)$")
        }
    }

    @Test
    fun `type selection includes nullable computed delegated and stored properties`() {
        val failure = assertThrows<AssertionError> {
            check(TypedProperties::class) { propertiesShouldMatch(Matcher::class, "matcher") }
        }
        listOf("computed", "delegated", "nullable").forEach { assertTrue(failure.message!!.contains(it)) }
        assertFalse(failure.message!!.contains("unrelated"))
        assertFalse(failure.message!!.contains("\$delegate"))
        check(CorrectProperties::class) { propertiesShouldMatch(Matcher::class.java, "matcher") }
        check(CorrectProperties::class) { propertiesShouldMatch(Matcher::class.java.name, "matcher") }
        assertThrows<AssertionError> { check(AliasProperty::class) { propertiesShouldMatch(Matcher::class, "matcher") } }
    }

    @Test
    fun `type matching recognizes source and erased JVM types`() {
        check(CollectionProperties::class) { propertiesShouldMatch(List::class, "wrong") }
        val collection = assertThrows<AssertionError> {
            check(CollectionProperties::class) { propertiesShouldMatch("java.util.List", "items") }
        }
        assertTrue(collection.message!!.contains(".wrong"))
        val primitive = assertThrows<AssertionError> {
            check(PrimitiveProperties::class) { propertiesShouldMatch(Int::class, "number") }
        }
        assertTrue(primitive.message!!.contains(".primitive"))
        assertTrue(primitive.message!!.contains(".nullable"))
    }

    @Test
    fun `new rules honor per rule configuration and exclusions`() {
        Taikai.builder().namespace("com.enofex.taikai.kotlin.fixtures.compatibility")
            .excludeClasses("com.enofex.taikai.kotlin.fixtures.compatibility..")
            .kotlin {
                classesShouldNotMatch(".*", Configuration.of(Namespace.IMPORT.WITH_TESTS))
                noUsageOfDeprecatedAPIs(Configuration.of(Namespace.IMPORT.WITH_TESTS))
                propertiesShouldMatch(Matcher::class, "none", Configuration.of(Namespace.IMPORT.WITH_TESTS))
            }.build().checkAll()
        assertThrows<IllegalArgumentException> { check(GoodDto::class) { functionsShouldNotMatch("[") } }
        assertThrows<IllegalArgumentException> { check(GoodDto::class) { propertiesShouldNotMatch("[") } }
        assertThrows<IllegalArgumentException> { check(GoodDto::class) { propertiesShouldMatch("", "name") } }
    }

    @ParameterizedTest
    @MethodSource("deprecatedUsages")
    fun `deprecated Kotlin API uses are reported`(type: KClass<*>) {
        val failure = assertThrows<AssertionError> { check(type) { noUsageOfDeprecatedAPIs() } }
        assertTrue(failure.message!!.contains("deprecated"), failure.message)
        assertTrue(failure.message!!.contains(type.simpleName!!), failure.message)
    }

    @Test
    fun `non deprecated overloads and accessor directions remain allowed`() {
        listOf(CallsCurrentOverload::class, WritesOldGetter::class, ReadsOldSetter::class,
            OldApi::class, OldConstructor::class, CompanionProperties::class,
            DeprecatedOverloads::class, DeprecatedData::class)
            .forEach { type -> check(type) { noUsageOfDeprecatedAPIs() } }
    }

    companion object {
        @JvmStatic
        fun deprecatedUsages(): List<KClass<*>> = listOf(
            CallsOldFunction::class, CallsOldDefaultFunction::class, ReadsOldProperty::class,
            ReadsOldComputed::class, ReadsOldGetter::class, WritesOldSetter::class,
            UsesOldType::class, CallsOldConstructor::class, CallsOldDefaultConstructor::class,
            CallsOldObjectDefault::class, ReadsCompanionField::class, ReadsCompanionComputed::class,
            CallsOldTopLevel::class, ReadsOldTopLevel::class,
            ReferencesOldMethod::class, ReferencesOldConstructor::class,
            ReadsJavaDeprecatedField::class, CallsJavaDeprecatedMethod::class,
            CallsJavaDeprecatedConstructor::class, DeprecatedDefaultExpression::class, ReadsOldContract::class,
        )
    }
}
