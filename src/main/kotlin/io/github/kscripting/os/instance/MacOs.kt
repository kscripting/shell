package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsTypeNew

class MacOs(override val vfs: MacOsVfs) : Os {
    override val type: OsTypeNew = OsTypeNew.MACOS
    override val osTypePrefix: String = "darwin"
}
