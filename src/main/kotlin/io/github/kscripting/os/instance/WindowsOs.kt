package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType

class WindowsOs(override val vfs: WindowsVfs) : Os {
    override val type: OsType = OsType.WINDOWS
    override val osTypePrefix: String = "windows"
}
