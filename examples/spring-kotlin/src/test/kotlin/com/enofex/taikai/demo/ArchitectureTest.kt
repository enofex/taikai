package com.enofex.taikai.demo

import com.enofex.taikai.ClassCriteria
import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.TaikaiRule.Configuration
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.demo.task.TaskService
import com.enofex.taikai.demo.task.TaskRepository
import com.enofex.taikai.kotlin.KotlinVisibility
import com.enofex.taikai.kotlin.kotlin
import com.enofex.taikai.kotlin.taikai
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.stream.Stream

private const val NAMESPACE = "com.enofex.taikai.demo"

class ArchitectureTest {

    @DisplayName("Check architecture constraints")
    @TestFactory
    fun architecture(): Stream<DynamicTest> =
        taikai {
            namespace(NAMESPACE)
            java {
                noUsageOf(Date::class.java)
                noUsageOf(Calendar::class.java)
                noUsageOf(SimpleDateFormat::class.java)
                fieldsShouldNotBePublic()
                classesShouldResideInPackage("$NAMESPACE..")
                classesShouldImplementHashCodeAndEquals()
                methodsShouldNotDeclareGenericExceptions()
                imports {
                    it.shouldHaveNoCycles()
                        .shouldNotImport(ImportPatterns.shaded())
                }
                // No enums or interfaces yet; these rules guard code added later.
                naming {
                    it.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI()
                }
            }
            spring {
                noAutowiredFields()
                noInjectionIntoStaticFields()
                // No proxied or transactional methods yet; these rules guard code added later.
                noSelfInvocationOfProxiedMethods()
                transactional { it.shouldNotBeUsedInControllers() }
                boot { it.applicationClassShouldResideInPackage(NAMESPACE) }
                controllers {
                    it.shouldBeAnnotatedWithRestController()
                        .namesShouldEndWithController()
                        .shouldNotDependOnOtherControllers()
                        .shouldNotDependOnRepositories()
                        .methodsShouldReturn(ClassCriteria.simpleNameMatching(".*Response"))
                }
                services { it.shouldBeAnnotatedWithService().namesShouldEndWithService().shouldNotDependOnControllers() }
                repositories {
                    it.shouldBeAnnotatedWithRepository()
                        .namesShouldEndWithRepository()
                        .shouldNotDependOnServices()
                        .shouldNotDependOnControllers()
                }
            }
            test {
                junit {
                    it.classesShouldEndWithTest()
                        .classesShouldNotBeAnnotatedWithDisabled()
                        .methodsShouldNotBeAnnotatedWithDisabled()
                        .methodsShouldContainAssertionsOrVerifications()
                        .testsShouldBeExecutable()
                }
            }
            kotlin {
                noUsageOfDeprecatedAPIs()
                classesShouldHaveVisibility(".*", KotlinVisibility.INTERNAL, Configuration.of("$NAMESPACE.task"))
                propertiesShouldHaveVisibility("service|repository|tasks", KotlinVisibility.PRIVATE)
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
