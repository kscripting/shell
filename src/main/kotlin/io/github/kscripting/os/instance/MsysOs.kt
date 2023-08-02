package io.github.kscripting.os.instance

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class MsysOs(userHome: String, nativeFileSystemRoot: String) : HostedOs {
    override val type: OsType = OsType.MSYS
    override val userHome: OsPath = path(userHome)
    override val nativeType: OsType = OsType.WINDOWS
    override val nativeFileSystemRoot: OsPath = TODO()

    override fun path(vararg pathParts: String): OsPath = TODO()
    override fun toNativePath(osPath: OsPath): OsPath = TODO()
    override fun toHostedPath(osPath: OsPath): OsPath = TODO()
}
