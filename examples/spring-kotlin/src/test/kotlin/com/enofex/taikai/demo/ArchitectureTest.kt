package com.enofex.taikai.demo

import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.demo.task.TaskService
import com.enofex.taikai.demo.task.TaskRepository
import com.enofex.taikai.kotlin.KotlinVisibility
import com.enofex.taikai.kotlin.kotlin
import com.enofex.taikai.kotlin.taikai
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

private const val NAMESPACE = "com.enofex.taikai.demo"

class ArchitectureTest {

    @TestFactory
    fun architecture(): Stream<DynamicTest> =
        taikai {
            namespace(NAMESPACE)
            java {
                fieldsShouldNotBePublic()
                classesShouldResideInPackage("$NAMESPACE..")
                methodsShouldNotDeclareGenericExceptions()
                imports {
                    it.shouldHaveNoCycles().shouldNotImport(ImportPatterns.shaded())
                }
                // No enums or interfaces yet; these rules guard code added later.
                naming {
                    it.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI()
                }
            }
            spring {
                services { it.namesShouldEndWithService().shouldNotDependOnControllers() }
                repositories { it.namesShouldEndWithRepository().shouldNotDependOnServices().shouldNotDependOnControllers() }
                controllers { it.namesShouldEndWithController().shouldNotDependOnRepositories() }
            }
            kotlin {
                noUsageOfDeprecatedAPIs()
                classesShouldHaveVisibility(".*", KotlinVisibility.INTERNAL, Configuration.of("$NAMESPACE.task"))
                classesShouldNotMatch(".*Impl")
                functionsShouldNotMatch("^(foo|bar)$")
                propertiesShouldNotMatch(".*(List|Set|Map)$")
                propertiesShouldMatch(TaskService::class, "service")
                propertiesShouldMatch(TaskRepository::class, "repository")
                // No constants yet; this rule guards code added later.
                constantsShouldFollowConventions()
                classesShouldBeDataClasses(".*(Request|Response)")
                dataClassesShouldBeImmutable()
                functionsShouldMatch("[a-z][a-zA-Z0-9]*")
                propertiesShouldMatch("[a-z][a-zA-Z0-9]*")
                functionsShouldNotExceedMaxParameters(3)
            }
            // Feature packages stay independent of each other as the application grows.
            addRule(TaikaiRule.of(slices().matching("$NAMESPACE.(*)..").should().notDependOnEachOther()))
        }.dynamicTests()
}
