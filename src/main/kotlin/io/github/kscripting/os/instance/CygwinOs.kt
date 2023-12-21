package io.github.kscripting.os.instance

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class CygwinOs(
    override val type: OsType<CygwinOs>,
    override val nativeType: OsType<WindowsOs>,
    userHome: String,
    nativeFileSystemRoot: String
) : HostedOs {
    override val osTypePrefix: String = "cygwin"
    override val userHome: OsPath = OsPath(type, userHome)
    override val nativeFileSystemRoot: OsPath = OsPath(nativeType, nativeFileSystemRoot)
}
