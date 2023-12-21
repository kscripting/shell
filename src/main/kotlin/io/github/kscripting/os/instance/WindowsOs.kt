package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType


class WindowsOs(override val type: OsType<WindowsOs>, userHome: String) : Os {
    override val osTypePrefix: String = "windows"
    override val pathSeparator: String get() = "\\"
    override val userHome: OsPath = OsPath(type, userHome)
}
