package io.github.kscripting.os.instance

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class CygwinOs(userHome: String, nativeFileSystemRoot: String) : HostedOs {
    override val type: OsType = OsType.CYGWIN
    override val userHome: OsPath = TODO()
    override val nativeType: OsType = OsType.WINDOWS
    override val nativeFileSystemRoot: OsPath = TODO()
}
