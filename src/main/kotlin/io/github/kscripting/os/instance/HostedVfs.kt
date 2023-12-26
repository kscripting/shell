package io.github.kscripting.os.instance

import io.github.kscripting.os.Vfs
import io.github.kscripting.os.model.OsPath

interface HostedVfs : Vfs {
    val nativeFsRoot: OsPath

    fun toNative(osPath: OsPath): OsPath
    fun toHosted(osPath: OsPath): OsPath
}
