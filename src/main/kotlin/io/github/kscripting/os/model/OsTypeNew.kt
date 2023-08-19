package io.github.kscripting.os.model

import io.github.kscripting.os.Os
import io.github.kscripting.os.instance.*
import net.igsoft.typeutils.globalcontext.GlobalContext
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.TypedMarker
import org.apache.commons.lang3.SystemUtils
import kotlin.properties.ReadOnlyProperty


abstract class TypedEnumCompanion<T> {
    private val registry: MutableMap<String, T> = mutableMapOf()

    fun find(name: String): T? = registry[name]
    fun findOrThrow(name: String): T =
        find(name) ?: throw IllegalArgumentException("Can not find enum value for: '$name'")

    protected fun <V : T> register(name: String, instance: V): V {
        registry[name] = instance
        return instance
    }

    protected fun <V : T> register(instance: V): ReadOnlyProperty<Any?, V> {
        return ReadOnlyProperty { _, property ->
            registry[property.name] = instance
            instance
        }
    }

    protected fun findName(instance: T) =
        registry.entries.firstOrNull { it.value == instance }?.key ?: error("Enum not registered")
}

class OsTypeNew<T : Os> private constructor(private val marker: TypedMarker<T>) : TypedMarker<T> {
    val value get() = GlobalContext.getValue(marker)

    fun isPosixLike() =
        (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)

    fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    fun isWindowsLike() = (this == WINDOWS)

    companion object : TypedEnumCompanion<OsTypeNew<out Os>>() {
        val LINUX by register(OsTypeNew(AutoTypedMarker.create<LinuxOs>()))
        val WINDOWS by register(OsTypeNew(AutoTypedMarker.create<WindowsOs>()))
        val CYGWIN by register(OsTypeNew(AutoTypedMarker.create<CygwinOs>()))
        val MSYS by register(OsTypeNew(AutoTypedMarker.create<MsysOs>()))
        val MACOS by register(OsTypeNew(AutoTypedMarker.create<MacOs>()))
        val FREEBSD by register(OsTypeNew(AutoTypedMarker.create<FreeBsdOs>()))

        val native: OsTypeNew<out Os> = guessNativeType()

        private fun guessNativeType(): OsTypeNew<out Os> {
            when {
                SystemUtils.IS_OS_LINUX -> return LINUX
                SystemUtils.IS_OS_MAC -> return MACOS
                SystemUtils.IS_OS_WINDOWS -> return WINDOWS
                SystemUtils.IS_OS_FREE_BSD -> return FREEBSD
            }

            return LINUX
        }
    }

    override val clazz: Class<T> get() = marker.clazz
    override val id: Any get() = marker.id
    override fun toString(): String = findName(this)
}

fun main() {
    GlobalContext.register(OsTypeNew.LINUX, LinuxOs("home"))
    val test = OsTypeNew.find("WINDOWS")
    println(test)

    println(OsTypeNew.LINUX)
}
