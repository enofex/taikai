package com.enofex.taikai.kotlin

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaMember
import com.tngtech.archunit.core.domain.JavaMethod
import kotlin.metadata.*
import kotlin.metadata.jvm.*

/** Reads imported annotation data only: analyzed application classes are never loaded. */
internal data class KotlinDeclarations(
    val type: KmClass?,
    val functions: List<KmFunction>,
    val properties: List<KmProperty>,
) {
    companion object {
        fun read(owner: JavaClass): KotlinDeclarations {
            val annotation = owner.getAnnotationOfType("kotlin.Metadata").`as`(Metadata::class.java)
            return when (val metadata = KotlinClassMetadata.readStrict(annotation)) {
                is KotlinClassMetadata.Class -> declarations(metadata.kmClass, metadata.kmClass)
                is KotlinClassMetadata.FileFacade -> declarations(null, metadata.kmPackage)
                is KotlinClassMetadata.MultiFileClassPart -> declarations(null, metadata.kmPackage)
                is KotlinClassMetadata.SyntheticClass, is KotlinClassMetadata.MultiFileClassFacade ->
                    KotlinDeclarations(null, emptyList(), emptyList())
                else -> error("Unsupported Kotlin metadata kind for ${owner.name}")
            }
        }

        private fun declarations(type: KmClass?, container: KmDeclarationContainer) =
            KotlinDeclarations(
                type,
                container.functions.filter { it.kind == MemberKind.DECLARATION },
                container.properties.filter { it.kind == MemberKind.DECLARATION },
            )
    }
}

/** Returns whether this JVM signature has the name and descriptor of the given member. */
@JvmSynthetic
internal fun JvmMemberSignature.matches(member: JavaMember): Boolean =
    name == member.name && descriptor == member.descriptor

/** Returns the method of this class with the name and descriptor of the given JVM signature. */
@JvmSynthetic
internal fun JavaClass.method(signature: JvmMethodSignature): JavaMethod? = methods.find(signature::matches)
