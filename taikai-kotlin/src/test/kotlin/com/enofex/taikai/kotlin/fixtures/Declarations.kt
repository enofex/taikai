@file:JvmName("DeclarationFixtures")
package com.enofex.taikai.kotlin.fixtures

class Plain(val active: Boolean) {
    internal fun internalWork(value: String) = value
    internal val internalValue = "value"
    private val secret = "secret"
    fun overloaded(value: String) = value
    fun overloaded(first: String, second: String) = first + second
    @JvmOverloads fun defaults(first: String = "a", second: String = "b") = first + second
    suspend fun suspended(value: String) = value
    companion object {
        const val DEFAULT_NAME = "default"
        val logger = "logger"
    }
}

data class GoodDto(val name: String, val active: Boolean)
data class MutableDto(var name: String) { var extra = "mutable" }
class WrongDto(val name: String)
object Registry
internal class InternalType
class Ordinary
class BadNames {
    val BadProperty = "bad"
    fun BadFunction() = Unit
}
object BadConstants { const val wrongName = "wrong" }
class Explosive {
    companion object { init { error("Analyzed classes must not be initialized") } }
    fun safe(value: String) = value
}
fun topLevel(value: String = "value") = value
fun String.extension(value: String) = this + value
suspend fun String.suspendExtension(value: String) = this + value

class DefaultDependency {
    fun text(value: String = com.enofex.taikai.kotlin.JavaDependency.value()) = value
}
interface Worker { fun inheritedName(): String }
class DelegatingWorker(private val worker: Worker) : Worker by worker
class OverridingWorker : Worker { override fun inheritedName() = "explicit" }
