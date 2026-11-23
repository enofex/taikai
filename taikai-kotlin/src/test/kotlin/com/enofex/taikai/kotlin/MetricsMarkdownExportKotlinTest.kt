package com.enofex.taikai.kotlin

import com.enofex.taikai.core.TaikaiRule
import com.enofex.taikai.exports.EntryPoints
import com.enofex.taikai.exports.MetricsMarkdownExport
import com.enofex.taikai.exports.spi.ExportContext
import com.enofex.taikai.exports.spi.TaikaiExport
import com.enofex.taikai.kotlin.fixtures.GoodDto
import com.enofex.taikai.kotlin.fixtures.MutableDto
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.EvaluationResult
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.Optional

class MetricsMarkdownExportKotlinTest {

    @TempDir
    lateinit var directory: Path

    @Test
    fun `data classes and calls with default arguments are cohesive and Kotlin is no library`() {
        val file = directory.resolve("METRICS.md")
        // the facade of several files does not name a source file, unlike the other classes
        val classes = ClassFileImporter().importClasses(GoodDto::class.java, MutableDto::class.java,
            Class.forName("com.enofex.taikai.kotlin.fixtures.MultiFileFixtures"),
            DefaultArguments::class.java)

        MetricsMarkdownExport().export(
            context(classes, MetricsMarkdownExport.file(file.toString())))

        val markdown = Files.readString(file)

        assertTrue(markdown.contains("### Least Cohesive Classes\n\nNone."), markdown)
        assertTrue(markdown.contains("| Libraries | 0 |"), markdown)
    }

    @Test
    fun `entry points with default arguments and in companion objects count once`() {
        val file = directory.resolve("METRICS.md")
        val classes = ClassFileImporter().importClasses(Jobs::class.java, Jobs.Companion::class.java)

        MetricsMarkdownExport().export(context(classes, MetricsMarkdownExport.file(file.toString())
            .withEntryPoints(EntryPoints.annotatedWith(Job::class.java))))

        val markdown = Files.readString(file)

        assertTrue(markdown.contains("| `MetricsMarkdownExportKotlinTest\$Jobs` | 2 | Job 2 | 0 |"),
            markdown)
    }

    /** The context of the export, which Taikai provides when it discovers and runs the export. */
    private fun context(classes: JavaClasses, settings: MetricsMarkdownExport.Settings) =
        object : ExportContext {
            override fun export(): Class<out TaikaiExport> = MetricsMarkdownExport::class.java
            override fun projectDirectory(): Path = directory
            override fun namespace(): String? = null
            override fun classes(): JavaClasses = classes
            override fun rules(): Collection<TaikaiRule> = emptyList()
            override fun evaluate(rule: TaikaiRule): EvaluationResult =
                throw UnsupportedOperationException()
            override fun classLoader(): ClassLoader = javaClass.classLoader
            override fun <T : Any> configuration(type: Class<T>): Optional<T> =
                if (type == MetricsMarkdownExport.Settings::class.java) {
                    Optional.of(type.cast(settings))
                } else {
                    Optional.empty()
                }
        }

    annotation class Job

    /**
     * The compiler adds the static `refresh${'$'}default` for the default argument, and a static
     * `cleanup` to this class that calls the one of the companion object.
     */
    class Jobs {
        @Job
        fun refresh(force: Boolean = false) = force

        companion object {
            @Job
            @JvmStatic
            fun cleanup() = Unit
        }
    }

    /** Calls [add] with its default argument through a function that the compiler generates. */
    class DefaultArguments {
        private var count = 0
        private var name = ""

        fun add(amount: Int = 1) {
            count += amount
        }

        fun rename() {
            add()
            name = "renamed"
        }
    }
}
