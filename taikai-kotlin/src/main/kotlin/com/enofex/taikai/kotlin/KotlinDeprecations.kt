package com.enofex.taikai.kotlin

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaCodeUnit
import com.tngtech.archunit.core.domain.JavaConstructor
import com.tngtech.archunit.core.domain.JavaMember
import com.tngtech.archunit.core.domain.JavaAccess
import com.tngtech.archunit.core.domain.JavaFieldAccess
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated
import java.net.URI
import kotlin.metadata.isData
import kotlin.metadata.jvm.*

/** Deprecation is read from imported annotations, including Kotlin's property annotation carrier. */
internal class KotlinDeprecations {
    private val declarations = mutableMapOf<JavaClass, KotlinDeclarations?>()
    private val failures = linkedSetOf<String>()
    private val completed = mutableMapOf<String, JavaClass>()
    private val visited = mutableSetOf<String>()

    fun violations(owner: JavaClass): List<String> {
        inspect(owner)
        return failures.toList()
    }

    private fun inspect(owner: JavaClass) {
        if (!visited.add(owner.name)) return
        owner.directDependenciesFromSelf.filter { deprecated(it.targetClass) }.forEach {
            failures += "${it.description}: uses deprecated class ${it.targetClass.name}"
        }
        owner.accessesFromSelf.forEach { access ->
            // Kotlin callable references can compile to nested synthetic classes rather than
            // JVM method references. Include those implementation helpers for explicit imports.
            if (access.targetOwner.name.startsWith(owner.name + "$") && access.targetOwner.source.isPresent) {
                val nested = complete(access.targetOwner)
                if (JavaModifier.SYNTHETIC in nested.modifiers) inspect(nested)
            }
            val member = access.target.resolveMember().orElse(null)
            if (member != null && implementationAccess(access, member)) return@forEach
            if (deprecated(access.target) || (member != null && deprecatedMember(member, access.origin))) {
                failures += "${access.description}: uses deprecated API ${access.target.fullName}"
            }
        }
    }

    private fun implementationAccess(access: JavaAccess<*>, member: JavaMember): Boolean {
        val origin = access.origin
        if (origin.owner != member.owner) return false
        if (access is JavaFieldAccess && access.accessType == JavaFieldAccess.AccessType.SET &&
            (origin is JavaConstructor || origin.name == "<clinit>")) return true
        val metadata = read(origin.owner) ?: return false
        if (origin is JavaMethod) {
            val declared = metadata.functions.any {
                it.signature?.let { signature -> signature.name == origin.name && signature.descriptor == origin.descriptor } == true
            }
            if (member is JavaMethod) {
                if (origin.name == member.name + "\$default" && deprecatedDefaultHelper(origin, metadata)) return true
                // @JvmOverloads wrappers are not always marked synthetic; source signatures
                // distinguish generated overloads from explicitly declared same-name functions.
                if (!declared && origin.name == member.name.removeSuffix("\$default") &&
                    (deprecated(member) || deprecatedDefaultHelper(member, metadata))) return true
            }
            if (access is JavaFieldAccess && metadata.type?.isData == true && !declared &&
                (origin.name in setOf("copy", "copy\$default", "equals", "hashCode", "toString") ||
                    Regex("component[0-9]+").matches(origin.name))) return true
        }
        if (origin is JavaConstructor && member is JavaConstructor) {
            if (deprecatedDefaultConstructor(origin, metadata)) return true
            val declared = metadata.type?.constructors.orEmpty().any { it.signature?.descriptor == origin.descriptor }
            if (!declared && deprecatedDefaultConstructor(member, metadata)) return true
        }
        return false
    }

    private fun deprecatedMember(member: JavaMember, origin: JavaCodeUnit): Boolean {
        if (deprecated(member)) return true
        val owner = member.owner
        val metadata = read(owner) ?: return false

        if (member is JavaMethod && deprecatedDefaultHelper(member, metadata)) return true
        if (member is JavaConstructor && deprecatedDefaultConstructor(member, metadata)) return true

        // @JvmField companion properties have their field on the enclosing class, while
        // the synthetic method carrying the source annotation lives on the companion.
        val propertyOwners = mutableListOf(owner to metadata)
        metadata.type?.companionObject?.let { name ->
            owner.tryGetField(name).orElse(null)?.rawType?.let { companion ->
                val imported = if (companion.isAnnotatedWith("kotlin.Metadata")) companion else complete(companion)
                read(imported)?.let { propertyOwners += imported to it }
            }
        }
        return propertyOwners.any { (propertyOwner, declarations) ->
            declarations.properties.any { property ->
                val matches = if (member is JavaMethod) {
                    listOf(property.getterSignature, property.setterSignature).any {
                        it != null && it.name == member.name && it.descriptor == member.descriptor &&
                            propertyOwner == owner
                    }
                } else {
                    property.fieldSignature?.let {
                        it.name == member.name && it.descriptor == member.descriptor
                    } == true
                }
                val ownAccessor = propertyOwner == origin.owner &&
                    listOf(property.getterSignature, property.setterSignature).any {
                        it != null && it.name == origin.name && it.descriptor == origin.descriptor
                    }
                matches && !ownAccessor && property.syntheticMethodForAnnotations?.let { signature ->
                    deprecatedCarrier(propertyOwner, signature)
                } == true
            }
        }
    }

    private fun deprecatedCarrier(owner: JavaClass, signature: JvmMethodSignature): Boolean {
        val carrier = owner.methods.find { it.name == signature.name && it.descriptor == signature.descriptor }
        if (carrier != null) return deprecated(carrier)
        if (!owner.isInterface) return false
        // Kotlin can put interface property annotation carriers in DefaultImpls even when
        // the property getter is abstract and the interface itself has no implementation.
        val source = owner.source.orElse(null) ?: return false
        val name = owner.name + "\$DefaultImpls"
        val helper = completed.getOrPut(name) {
            try {
                val uri = URI.create(source.uri.toString().removeSuffix(".class") + "\$DefaultImpls.class")
                ClassFileImporter().importUrl(uri.toURL()).get(name)
            } catch (exception: Exception) {
                failures += "Cannot inspect Kotlin property annotation carrier $name: ${exception.message}"
                owner
            }
        }
        return helper.methods.any { it.name == signature.name && it.descriptor == signature.descriptor && deprecated(it) }
    }

    private fun deprecatedDefaultHelper(helper: JavaMethod, declarations: KotlinDeclarations): Boolean {
        if (!helper.name.endsWith("\$default") || JavaModifier.SYNTHETIC !in helper.modifiers) return false
        return declarations.functions.any { function ->
            val signature = function.signature ?: return@any false
            if (helper.name != "${signature.name}\$default") return@any false
            val original = helper.owner.methods.find {
                it.name == signature.name && it.descriptor == signature.descriptor
            } ?: return@any false
            if (!deprecated(original)) return@any false
            val receiver = if (JavaModifier.STATIC in original.modifiers) ""
                else "L${helper.owner.name.replace('.', '/')};"
            val masks = "I".repeat((function.valueParameters.size + 31) / 32)
            val descriptor = "(" + receiver + signature.descriptor.substringAfter('(')
                .substringBefore(')') + masks + "Ljava/lang/Object;)" +
                signature.descriptor.substringAfter(')')
            helper.descriptor == descriptor
        }
    }

    private fun deprecatedDefaultConstructor(helper: JavaConstructor, declarations: KotlinDeclarations): Boolean {
        if (JavaModifier.SYNTHETIC !in helper.modifiers) return false
        return declarations.type?.constructors.orEmpty().any { constructor ->
            val signature = constructor.signature ?: return@any false
            val original = helper.owner.constructors.find { it.descriptor == signature.descriptor }
                ?: return@any false
            if (!deprecated(original)) return@any false
            val masks = "I".repeat((constructor.valueParameters.size + 31) / 32)
            val descriptor = signature.descriptor.substringBefore(')') + masks +
                "Lkotlin/jvm/internal/DefaultConstructorMarker;)V"
            helper.descriptor == descriptor
        }
    }

    private fun complete(owner: JavaClass): JavaClass = completed.getOrPut(owner.name) {
        // Referenced member types can be stubs in ArchUnit's domain. Import the exact source
        // resource when companion property annotations or callable-reference bodies are needed.
        try {
            owner.source.orElse(null)?.let { source ->
                ClassFileImporter().importUrl(source.uri.toURL()).get(owner.name)
            } ?: owner
        } catch (exception: Exception) {
            failures += "Cannot inspect Kotlin implementation ${owner.name}: ${exception.message}"
            owner
        }
    }

    private fun read(owner: JavaClass): KotlinDeclarations? = declarations.getOrPut(owner) {
        if (!owner.isAnnotatedWith("kotlin.Metadata")) return@getOrPut null
        try {
            KotlinDeclarations.read(owner)
        } catch (exception: Exception) {
            failures += "Cannot read Kotlin metadata for ${owner.name}: ${exception.message}"
            null
        }
    }

    private fun deprecated(element: CanBeAnnotated): Boolean =
        element.isAnnotatedWith("java.lang.Deprecated") || element.isAnnotatedWith("kotlin.Deprecated")
}
