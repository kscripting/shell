package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType

class LinuxOs(override val vfs: LinuxVfs) : Os {
    override val type: OsType = OsType.LINUX
    override val osTypePrefix: String = "linux"
}
