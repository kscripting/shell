package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

interface HostedOs : Os {
    val nativeType: OsType<out Os>
    val nativeFileSystemRoot: OsPath
    override val pathSeparator: String get() = "/"
}
