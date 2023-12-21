package io.github.kscripting.os.model

import io.github.kscripting.os.Os
import io.github.kscripting.os.instance.*
import net.igsoft.typeutils.globalcontext.GlobalContext
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.TypedMarker
import net.igsoft.typeutils.typedenum.TypedEnumCompanion
import org.apache.commons.lang3.SystemUtils


class GlobalOsType<T : Os> private constructor(private val marker: TypedMarker<T>) : OsType<T>,
    DefaultTypedMarker<T>(marker) {
    override val os: T get() = GlobalContext.getValue(marker)

    override fun isPosixLike() =
        (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)

    override fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    override fun isWindowsLike() = (this == WINDOWS)

    companion object : TypedEnumCompanion<OsType<out Os>>() {
        val LINUX: OsType<LinuxOs> = GlobalOsType(AutoTypedMarker.create<LinuxOs>())
        val WINDOWS: OsType<WindowsOs> = GlobalOsType(AutoTypedMarker.create<WindowsOs>())
        val CYGWIN: OsType<CygwinOs> = GlobalOsType(AutoTypedMarker.create<CygwinOs>())
        val MSYS: OsType<MsysOs> = GlobalOsType(AutoTypedMarker.create<MsysOs>())
        val MACOS: OsType<MacOs> = GlobalOsType(AutoTypedMarker.create<MacOs>())
        val FREEBSD: OsType<FreeBsdOs> = GlobalOsType(AutoTypedMarker.create<FreeBsdOs>())

        val native: OsType<out Os> = guessNativeType()

        fun findByOsTypeString(osTypeString: String): OsType<out Os>? =
            find { osTypeString.startsWith(it.os.osTypePrefix, true) }

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
