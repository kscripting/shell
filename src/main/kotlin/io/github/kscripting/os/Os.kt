package io.github.kscripting.os

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

val CURRENT_OS = AutoTypedMarker.create<Os>()

interface Os {
    //LINUX("linux"), MACOS("darwin"), WINDOWS("windows"), CYGWIN("cygwin"), MSYS("msys"), FREEBSD("freebsd");
    // Exact comparison (it.osName.equals(name, true)) seems to be not feasible as there is also e.g. "darwin21"
    // "darwin19", "linux-musl" (for Docker Alpine), "linux-gnu" and maybe even other osTypes. But it seems that
    // startsWith() covers all cases.
    val osTypePrefix: String
    val type: OsType<out Os>
    val userHome: OsPath
    val pathSeparator: String
}
