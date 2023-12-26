package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType

class MacOs(override val vfs: MacOsVfs) : Os {
    override val type: OsType = OsType.MACOS
    override val osTypePrefix: String = "darwin"
}
