package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsTypeNew

class WindowsOs(override val vfs: WindowsVfs) : Os {
    override val type: OsTypeNew = OsTypeNew.WINDOWS
    override val osTypePrefix: String = "windows"
}
