package com.enofex.taikai.kotlin

import com.tngtech.archunit.core.domain.JavaClass
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
