package com.enofex.taikai.kotlin

import com.enofex.taikai.checks.GradleWrapperCheck
import com.enofex.taikai.core.ClassCriteria
import com.enofex.taikai.core.Namespace
import com.enofex.taikai.core.TaikaiException
import com.enofex.taikai.core.TaikaiRule
import com.enofex.taikai.core.TaikaiRule.Configuration
import com.enofex.taikai.exports.AgentsMarkdownExport
import com.enofex.taikai.kotlin.fixtures.GoodDto
import com.enofex.taikai.kotlin.fixtures.MutableDto
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TaikaiDslTest {

    @Test
    fun `receiver DSL builds without executing and preserves shared rules`() {
        val instance = taikai {
            classes(GoodDto::class)
            java { classesShouldResideInPackage("com.enofex.taikai.kotlin.fixtures..") }
            kotlin { dataClassesShouldBeImmutable() }
        }
        assertPasses(instance)
        val invalid = taikai {
            classes(MutableDto::class)
            kotlin { dataClassesShouldBeImmutable() }
        }
        assertViolates(invalid)
    }

    @Test
    fun `receiver DSL excludes classes by literal and by criteria`() {
        val withTests = Configuration.of(Namespace.Import.WITH_TESTS)
        val fixtures = "com.enofex.taikai.kotlin.fixtures"
        assertViolates(taikai {
            namespace(fixtures)
            kotlin { dataClassesShouldBeImmutable(withTests) }
        })
        assertPasses(taikai {
            namespace(fixtures)
            excludeClasses(MutableDto::class)
            kotlin { dataClassesShouldBeImmutable(withTests) }
        })
        assertPasses(taikai {
            namespace(fixtures)
            excludeClasses(ClassCriteria.simpleNameMatching("MutableDto"))
            kotlin { dataClassesShouldBeImmutable(withTests) }
        })
    }

    @Test
    fun `receiver DSL registers rules for every rule group and discovers checks`() {
        val rule = TaikaiRule.of(
            ArchRuleDefinition.classes().should().haveSimpleNameNotEndingWith("Impl"))
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
                discoverChecks {
                    configure(GradleWrapperCheck::class.java, GradleWrapperCheck.directories(".."))
                }
            }
        }
    }

    @Test
    fun `receiver DSL discovers exports`() {
        val instance = taikai {
            classes(GoodDto::class)
            discoverExports {
                configure(AgentsMarkdownExport::class.java,
                    AgentsMarkdownExport.file("ARCHITECTURE.md"))
            }
        }
        assertEquals(listOf(AgentsMarkdownExport::class.java),
            instance.exports().map { it.javaClass })
    }
}
