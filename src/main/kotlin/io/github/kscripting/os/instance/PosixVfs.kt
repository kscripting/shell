package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import io.github.kscripting.os.Vfs

abstract class PosixVfs(override val type: OsType) : Vfs {
    override val pathSeparator: String = "/"
    override fun isValid(path: String): Boolean = true
}