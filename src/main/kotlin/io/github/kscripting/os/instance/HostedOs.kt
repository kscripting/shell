package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

interface HostedOs : Os {
    val nativeType: OsType
    val nativeFileSystemRoot: OsPath
    override val pathSeparator get(): String = "/"

    fun toNativePath(osPath: OsPath): OsPath
    fun toHostedPath(osPath: OsPath): OsPath
}
