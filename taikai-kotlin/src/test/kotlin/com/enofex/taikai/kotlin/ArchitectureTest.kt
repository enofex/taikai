package com.enofex.taikai.kotlin

import com.enofex.taikai.ClassCriteria
import com.enofex.taikai.checks.ClassFileVersionCheck
import com.enofex.taikai.checks.ForbiddenFilesCheck
import com.enofex.taikai.checks.MavenWrapperCheck
import com.enofex.taikai.checks.RequiredFilesCheck
import com.enofex.taikai.configures.AbstractConfigurer
import com.enofex.taikai.java.ImportPatterns.lombok
import com.enofex.taikai.java.ImportPatterns.shaded
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.stream.Stream

class ArchitectureTest {

    @DisplayName("Should fulfill architecture constraints")
    @TestFactory
    fun architecture(): Stream<DynamicTest> =
        taikai {
            namespace("com.enofex.taikai.kotlin")
            java {
                noUsageOfSystemOutOrErr()
                noUsageOf(Date::class.java)
                noUsageOf(Calendar::class.java)
                noUsageOf(SimpleDateFormat::class.java)
                classesShouldImplementHashCodeAndEquals()
                methodsShouldNotDeclareGenericExceptions()
                fieldsShouldNotBePublic()
                classesShouldResideInPackage("com.enofex.taikai.kotlin..")
                imports {
                    it.shouldHaveNoCycles()
                        .shouldNotImport(shaded())
                        .shouldNotImport(lombok())
                }
                naming {
                    it.packagesShouldMatchDefault()
                        .classesShouldMatch(ClassCriteria.assignableTo(AbstractConfigurer::class.java), ".*Configurer")
                        .interfacesShouldNotHavePrefixI()
                        .enumConstantsShouldFollowConventions()
                }
            }
            kotlin {
                noUsageOfDeprecatedAPIs()
                classesShouldNotMatch(".*Impl")
                functionsShouldMatch("[a-z][a-zA-Z0-9]*")
                propertiesShouldMatch("[a-z][a-zA-Z0-9]*")
                propertiesShouldNotMatch(".*(List|Set|Map)$")
                constantsShouldFollowConventions()
                dataClassesShouldBeImmutable()
            }
            discoverChecks {
                configure(RequiredFilesCheck::class.java, RequiredFilesCheck.files(
                    "../README.md", "../LICENSE", "../CONTRIBUTING.md", "../CODE_OF_CONDUCT.md",
                    "../SECURITY.md", "../docs/kotlin.md"))
                configure(MavenWrapperCheck::class.java, MavenWrapperCheck.directories(".."))
                configure(ForbiddenFilesCheck::class.java, ForbiddenFilesCheck.files(
                    "src/main/**/*.pem", "src/main/**/*.key", "src/main/**/*.p12", "src/main/**/*.jks",
                    "src/main/**/.env", "src/main/**/*.class", "src/main/**/*.jar"))
                configure(ClassFileVersionCheck::class.java, ClassFileVersionCheck.javaVersion(17))
            }
        }.dynamicTests()
}
