package com.enofex.taikai.kotlin

import com.enofex.taikai.ClassCriteria
import com.enofex.taikai.checks.ClassFileVersionCheck
import com.enofex.taikai.checks.ForbiddenFilesCheck
import com.enofex.taikai.checks.RequiredFilesCheck
import com.enofex.taikai.checks.ServiceRegistrationsCheck
import com.enofex.taikai.configures.AbstractConfigurer
import com.enofex.taikai.java.ImportPatterns.lombok
import com.enofex.taikai.java.ImportPatterns.shaded
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Path
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
                projectDirectory(Path.of(".."))
                configure(ServiceRegistrationsCheck::class.java, ServiceRegistrationsCheck.directories(
                    "taikai-kotlin/src/test/resources/META-INF/services"))
                configure(RequiredFilesCheck::class.java, RequiredFilesCheck.files(
                    "README.md", "LICENSE", "CONTRIBUTING.md", "CODE_OF_CONDUCT.md", "SECURITY.md", "docs/kotlin.md"))
                configure(ForbiddenFilesCheck::class.java, ForbiddenFilesCheck.files(
                    "taikai-kotlin/src/main/**/*.pem", "taikai-kotlin/src/main/**/*.key",
                    "taikai-kotlin/src/main/**/*.p12", "taikai-kotlin/src/main/**/*.jks",
                    "taikai-kotlin/src/main/**/.env", "taikai-kotlin/src/main/**/*.class",
                    "taikai-kotlin/src/main/**/*.jar"))
                configure(ClassFileVersionCheck::class.java, ClassFileVersionCheck.javaVersion(17))
            }
        }.dynamicTests()
}
