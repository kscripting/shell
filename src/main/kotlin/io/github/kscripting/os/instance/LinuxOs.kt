package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class LinuxOs(userHome: String) : Os {
    override val type: OsType = OsType.LINUX
    override val pathSeparator get() = "/"
    override val userHome: OsPath = OsPath.of(OsType.LINUX, userHome)
}
