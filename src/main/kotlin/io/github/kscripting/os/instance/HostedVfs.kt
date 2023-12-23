package io.github.kscripting.os.instance

import io.github.kscripting.os.Vfs
import io.github.kscripting.os.model.OsPath

interface HostedVfs : Vfs {
    val nativeFsRoot: OsPath<out Vfs>

    fun toNative(osPath: OsPath<out Vfs>): OsPath<out Vfs> = osPath
    fun toHosted(osPath: OsPath<out Vfs>): OsPath<out Vfs> = osPath
}
