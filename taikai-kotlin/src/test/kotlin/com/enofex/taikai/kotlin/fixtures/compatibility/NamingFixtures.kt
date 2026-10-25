package com.enofex.taikai.kotlin.fixtures.compatibility

class Matcher
class BadImplementationImpl
class TypedProperties {
    val matcher = Matcher()
    val computed: Matcher get() = Matcher()
    val delegated: Matcher by lazy { Matcher() }
    val nullable: Matcher? get() = null
    val unrelated = "not a Matcher"
}
class CorrectProperties {
    val matcher: Matcher? get() = Matcher()
    val unrelated = "unrelated"
}
class CollectionProperties {
    val wrong: MutableList<Matcher> get() = mutableListOf()
}
class PrimitiveProperties {
    val primitive: Int = 1
    val nullable: Int? get() = null
}
class NegativeNames {
    internal fun foo() = Unit
    fun bar(value: String = "value") = value
    val namesList: List<String> get() = emptyList()
    val idsSet by lazy { emptySet<String>() }
}

typealias MatcherAlias = Matcher
class AliasProperty { val wrong: MatcherAlias get() = Matcher() }
