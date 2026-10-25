package com.enofex.taikai.kotlin

import com.tngtech.archunit.core.domain.JavaClass
import kotlin.metadata.KmClassifier
import kotlin.metadata.KmProperty
import kotlin.metadata.jvm.fieldSignature
import kotlin.metadata.jvm.getterSignature

/** Matches source classifiers or exact erased JVM types, including computed and delegated properties. */
internal fun KmProperty.hasType(owner: JavaClass, typeName: String): Boolean {
    val requested = typeName.replace('$', '.')
    val classifier = returnType.classifier as? KmClassifier.Class
    if (classifier?.name?.replace('/', '.') == requested) return true

    val getter = getterSignature
    if (getter != null && owner.methods.any {
        it.name == getter.name && it.descriptor == getter.descriptor &&
            it.rawReturnType.name.replace('$', '.') == requested
    }) return true

    val field = fieldSignature
    return field != null && owner.fields.any {
        it.name == field.name && it.descriptor == field.descriptor &&
            it.rawType.name.replace('$', '.') == requested
    }
}
