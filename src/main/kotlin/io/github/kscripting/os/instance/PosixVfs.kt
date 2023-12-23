package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs

abstract class PosixVfs(override val type: OsTypeNew) : Vfs {
    override val pathSeparator: String = "/"
    override fun isValid(path: String): Boolean = true
}