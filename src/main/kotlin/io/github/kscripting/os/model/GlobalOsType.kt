package io.github.kscripting.os.model

import io.github.kscripting.os.Os
import io.github.kscripting.os.instance.*
import net.igsoft.typeutils.globalcontext.GlobalContext
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.TypedMarker
import net.igsoft.typeutils.typedenum.TypedEnumCompanion
import org.apache.commons.lang3.SystemUtils


class GlobalOsType<T : Os> private constructor(private val marker: TypedMarker<T>) : DefaultTypedMarker<T>(marker) {
    val os: T get() = GlobalContext.getValue(marker)

    fun isPosixLike() =
        (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)

    fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    fun isWindowsLike() = (this == WINDOWS)

    companion object : TypedEnumCompanion<GlobalOsType<out Os>>() {
        val LINUX: GlobalOsType<LinuxOs> = GlobalOsType(AutoTypedMarker.create<LinuxOs>())
        val WINDOWS: GlobalOsType<WindowsOs> = GlobalOsType(AutoTypedMarker.create<WindowsOs>())
        val CYGWIN: GlobalOsType<CygwinOs> = GlobalOsType(AutoTypedMarker.create<CygwinOs>())
        val MSYS: GlobalOsType<MsysOs> = GlobalOsType(AutoTypedMarker.create<MsysOs>())
        val MACOS: GlobalOsType<MacOs> = GlobalOsType(AutoTypedMarker.create<MacOs>())
        val FREEBSD: GlobalOsType<FreeBsdOs> = GlobalOsType(AutoTypedMarker.create<FreeBsdOs>())

        val native: GlobalOsType<out Os> = guessNativeType()

        fun findByOsTypeString(osTypeString: String): GlobalOsType<out Os>? =
            find { osTypeString.startsWith(it.os.osTypePrefix, true) }

        private fun guessNativeType(): GlobalOsType<out Os> {
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
