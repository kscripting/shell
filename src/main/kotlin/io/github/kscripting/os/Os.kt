package io.github.kscripting.os

import net.igsoft.typeutils.marker.AutoTypedMarker

interface Os {
    //LINUX("linux"), MACOS("darwin"), WINDOWS("windows"), CYGWIN("cygwin"), MSYS("msys"), FREEBSD("freebsd");
    // Exact comparison (it.osName.equals(name, true)) seems to be not feasible as there is also e.g. "darwin21"
    // "darwin19", "linux-musl" (for Docker Alpine), "linux-gnu" and maybe even other osTypes. But it seems that
    // startsWith() covers all cases.
    val marker: AutoTypedMarker<out Os>
    val osTypePrefix: String

    val type: OsType

    //val environment: Map<String, String>
    //val systemProperties: Map<String, String>
    val vfs: Vfs
}
