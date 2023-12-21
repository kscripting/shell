package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class LinuxOs(override val type: OsType<LinuxOs>, userHome: String) : Os {
    override val osTypePrefix: String = "linux"
    override val pathSeparator: String get() = "/"
    override val userHome: OsPath = OsPath(type, userHome)
}
