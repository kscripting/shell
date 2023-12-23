package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsTypeNew

class LinuxOs(override val vfs: LinuxVfs) : Os {
    override val type: OsTypeNew = OsTypeNew.LINUX
    override val osTypePrefix: String = "linux"
}
