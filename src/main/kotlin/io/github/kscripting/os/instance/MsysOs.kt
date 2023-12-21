package io.github.kscripting.os.instance

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class MsysOs(
    override val type: OsType<MsysOs>,
    override val nativeType: OsType<WindowsOs>,
    userHome: String,
    nativeFileSystemRoot: String
) : HostedOs {
    override val osTypePrefix: String = "msys"
    override val userHome: OsPath = OsPath(type, userHome)
    override val nativeFileSystemRoot: OsPath = OsPath(nativeType, nativeFileSystemRoot)
}
