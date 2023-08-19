package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType


class WindowsOs(userHome: String) : Os {
    override val osTypePrefix: String = "windows"
    override val type: OsType<WindowsOs> = OsType.WINDOWS
    override val pathSeparator: String get() = "\\"
    override val userHome: OsPath = OsPath.of(type, userHome)
}
