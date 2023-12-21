package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class MacOs(override val type: OsType<MacOs>, userHome: String) : Os {
    override val osTypePrefix: String = "darwin"
    override val pathSeparator: String get() = "/"
    override val userHome: OsPath = OsPath(type, userHome)
}
