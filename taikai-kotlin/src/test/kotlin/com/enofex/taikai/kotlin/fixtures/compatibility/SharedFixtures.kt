package com.enofex.taikai.kotlin.fixtures.compatibility

import java.io.IOException

class GenericThrows {
    @Throws(Exception::class) fun generic() = Unit
    @Throws(RuntimeException::class) fun runtime() = Unit
}
class SpecificThrows {
    @Throws(IOException::class) fun specific() = Unit
}
enum class GoodEnum { FIRST, SECOND }
enum class BadEnum { wrong }
interface Service
interface IService
class KotlinUsesShaded(val dependency: com.enofex.taikai.kotlin.fixtures.compatibility.shaded.Shadow)
object UtilityRegistry { fun value() = "value" }
