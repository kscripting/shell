package io.github.kscripting.os.instance

import io.github.kscripting.os.Vfs
import io.github.kscripting.os.model.OsPath

interface HostedVfs : Vfs {
    val nativeFsRoot: OsPath<out Vfs>
}
