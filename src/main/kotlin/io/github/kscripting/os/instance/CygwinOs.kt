package io.github.kscripting.os.instance

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class CygwinOs(userHome: String, nativeFileSystemRoot: String) : HostedOs {
    override val osTypePrefix: String = "cygwin"
    override val type: OsType<CygwinOs> = OsType.CYGWIN
    override val userHome: OsPath = OsPath.of(type, userHome)
    override val nativeType: OsType<WindowsOs> = OsType.WINDOWS
    override val nativeFileSystemRoot: OsPath = OsPath.of(nativeType, nativeFileSystemRoot)
}
