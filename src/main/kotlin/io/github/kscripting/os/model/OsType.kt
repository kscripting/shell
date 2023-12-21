package io.github.kscripting.os.model

import io.github.kscripting.os.Os
import io.github.kscripting.os.instance.*
import net.igsoft.typeutils.marker.TypedMarker

//@Suppress("PropertyName")
//interface OsTypeCompanion {
//    val LINUX: OsType<LinuxOs>
//    val WINDOWS: OsType<WindowsOs>
//    val CYGWIN: OsType<CygwinOs>
//    val MSYS: OsType<MsysOs>
//    val MACOS: OsType<MacOs>
//    val FREEBSD: OsType<FreeBsdOs>
//
//    val native: OsType<out Os>
//}

interface OsType<T : Os> : TypedMarker<T> {
    val os: T

    fun isPosixLike(): Boolean
    fun isPosixHostedOnWindows(): Boolean
    fun isWindowsLike(): Boolean
}
