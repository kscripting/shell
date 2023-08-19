package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType


class WindowsOs(userHome: String) : Os {
    override val osTypePrefix: String = "windows"
    override val type: OsType = OsType.WINDOWS
    override val pathSeparator get() = "\\"
    override val userHome: OsPath = TODO()
}
