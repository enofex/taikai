@file:Suppress("DEPRECATION")
package com.enofex.taikai.kotlin.fixtures.compatibility

class OldApi {
    @Deprecated("Use current") fun old(value: String = "old") = value
    fun old(value: Int, tag: String = "allowed") = value + tag.length
    @Deprecated("Use current") val oldProperty: String = "old"
    @Deprecated("Use current") val oldComputed: String get() = "old"
    @get:Deprecated("Don't read") var getterOnly: String = "value"
    @set:Deprecated("Don't write") var setterOnly: String = "value"
}
@Deprecated("Use CurrentType") class OldType
class OldConstructor @Deprecated("Use a factory") constructor(val value: String = "old")
object OldCompanionOwner {
    @Deprecated("Use current") fun old(value: String = "old") = value
}
class CompanionProperties {
    companion object {
        @JvmField @Deprecated("Use current") val oldField = "old"
        @Deprecated("Use current") val oldComputed: String get() = "old"
    }
}
@Deprecated("Use current") fun oldTopLevel(value: String = "old") = value
@Deprecated("Use current") val oldTopLevelProperty: String get() = "old"

class CallsOldFunction { fun use(api: OldApi) = api.old("value") }
class CallsOldDefaultFunction { fun use(api: OldApi) = api.old() }
class CallsCurrentOverload { fun use(api: OldApi) = api.old(1) }
class ReadsOldProperty { fun use(api: OldApi) = api.oldProperty }
class ReadsOldComputed { fun use(api: OldApi) = api.oldComputed }
class ReadsOldGetter { fun use(api: OldApi) = api.getterOnly }
class WritesOldGetter { fun use(api: OldApi) { api.getterOnly = "allowed" } }
class ReadsOldSetter { fun use(api: OldApi) = api.setterOnly }
class WritesOldSetter { fun use(api: OldApi) { api.setterOnly = "forbidden" } }
class UsesOldType { fun use() = OldType() }
class CallsOldConstructor { fun use() = OldConstructor("value") }
class CallsOldDefaultConstructor { fun use() = OldConstructor() }
class CallsOldObjectDefault { fun use() = OldCompanionOwner.old() }
class ReadsCompanionField { fun use() = CompanionProperties.oldField }
class ReadsCompanionComputed { fun use() = CompanionProperties.oldComputed }
class CallsOldTopLevel { fun use() = oldTopLevel() }
class ReadsOldTopLevel { fun use() = oldTopLevelProperty }
class ReferencesOldMethod { fun use(api: OldApi): (String) -> String = api::old }
class ReferencesOldConstructor { fun use(): (String) -> OldConstructor = ::OldConstructor }

class ReadsJavaDeprecatedField { fun use(api: JavaLegacyApi) = api.oldField }
class CallsJavaDeprecatedMethod { fun use(api: JavaLegacyApi) = api.oldMethod() }
class CallsJavaDeprecatedConstructor { fun use() = JavaLegacyApi("value") }
class DeprecatedDefaultExpression {
    fun current(value: String = oldTopLevel()) = value
}

class DeprecatedOverloads {
    @Deprecated("Use current") @JvmOverloads fun old(value: String = "old") = value
}
data class DeprecatedData(@Deprecated("Use current") val oldValue: String)

interface OldContract { @Deprecated("Use current") val old: String }
class ReadsOldContract { fun use(api: OldContract) = api.old }
