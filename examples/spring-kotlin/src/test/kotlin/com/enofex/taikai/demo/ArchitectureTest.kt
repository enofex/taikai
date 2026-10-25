package com.enofex.taikai.demo

import com.enofex.taikai.Taikai
import com.enofex.taikai.TaikaiRule
import com.enofex.taikai.java.ImportPatterns
import com.enofex.taikai.demo.service.TaskService
import com.enofex.taikai.demo.repository.TaskRepository
import com.enofex.taikai.kotlin.kotlin
import com.enofex.taikai.kotlin.taikai
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class ArchitectureTest {
    @Test
    fun `application follows shared and Kotlin architecture rules`() {
        taikai {
            namespace("com.enofex.taikai.demo")
            // Enum and interface conventions allow empty selections in this small application.
            java {
                fieldsShouldNotBePublic()
                classesShouldResideInPackage("com.enofex.taikai.demo..")
                methodsShouldNotDeclareGenericExceptions()
                imports {
                    it.shouldHaveNoCycles().shouldNotImport(ImportPatterns.shaded())
                }
                naming {
                    it.enumConstantsShouldFollowConventions().interfacesShouldNotHavePrefixI()
                }
            }
            spring {
                services { it.namesShouldEndWithService().shouldNotDependOnControllers() }
                repositories { it.namesShouldEndWithRepository().shouldNotDependOnServices().shouldNotDependOnControllers() }
                controllers { it.namesShouldEndWithController() }
            }
            kotlin {
                noUsageOfDeprecatedAPIs()
                classesShouldNotMatch(".*Impl")
                functionsShouldNotMatch("^(foo|bar)$")
                propertiesShouldNotMatch(".*(List|Set|Map)$")
                propertiesShouldMatch(TaskService::class, "service")
                propertiesShouldMatch(TaskRepository::class, "repository")
                constantsShouldFollowConventions()
                classesShouldBeDataClasses(".*(Request|Response)")
                dataClassesShouldBeImmutable()
                functionsShouldMatch("[a-z][a-zA-Z0-9]*")
                propertiesShouldMatch("[a-z][a-zA-Z0-9]*")
                functionsShouldNotExceedMaxParameters(3)
            }
            addRule(TaikaiRule.of(noClasses().that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..web..")))
            addRule(TaikaiRule.of(noClasses().that().resideInAPackage("..web..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")))
        }.checkAll()
    }

    @Test
    fun `existing builder is callable directly from Kotlin`() {
        Taikai.builder().namespace("com.enofex.taikai.demo")
            .spring { spring -> spring.services { it.namesShouldEndWithService() } }
            .kotlin { dataClassesShouldBeImmutable() }
            .build().check()
    }
}
