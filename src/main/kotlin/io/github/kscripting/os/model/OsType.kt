package io.github.kscripting.os.model

import io.github.kscripting.os.Os
import io.github.kscripting.os.instance.*
import net.igsoft.typeutils.enum.TypedEnumCompanion
import net.igsoft.typeutils.globalcontext.GlobalContext
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.TypedMarker
import org.apache.commons.lang3.SystemUtils


class OsType<T : Os> private constructor(private val marker: TypedMarker<T>) : DefaultTypedMarker<T>(marker) {
    val value: T get() = GlobalContext.getValue(marker)

    fun isPosixLike() =
        (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)

    fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    fun isWindowsLike() = (this == WINDOWS)

    companion object : TypedEnumCompanion<OsType<out Os>>() {
        val LINUX by register(OsType(AutoTypedMarker.create<LinuxOs>()))
        val WINDOWS by register(OsType(AutoTypedMarker.create<WindowsOs>()))
        val CYGWIN by register(OsType(AutoTypedMarker.create<CygwinOs>()))
        val MSYS by register(OsType(AutoTypedMarker.create<MsysOs>()))
        val MACOS by register(OsType(AutoTypedMarker.create<MacOs>()))
        val FREEBSD by register(OsType(AutoTypedMarker.create<FreeBsdOs>()))

        val native: OsType<out Os> = guessNativeType()

        fun findByOsTypeString(osTypeString: String): OsType<out Os>? =
            find { osTypeString.startsWith(it.value.osTypePrefix, true) }

        private fun guessNativeType(): OsType<out Os> {
            when {
                SystemUtils.IS_OS_LINUX -> return LINUX
                SystemUtils.IS_OS_MAC -> return MACOS
                SystemUtils.IS_OS_WINDOWS -> return WINDOWS
                SystemUtils.IS_OS_FREE_BSD -> return FREEBSD
            }

            return LINUX
        }
    }

    override fun toString(): String = findName(this)
}
