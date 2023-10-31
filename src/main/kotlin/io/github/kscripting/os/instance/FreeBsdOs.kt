package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType

class FreeBsdOs(userHome: String) : Os {
    override val osTypePrefix: String = "freebsd"
    override val type: OsType<FreeBsdOs> = OsType.FREEBSD
    override val pathSeparator: String get() = "/"
    override val userHome: OsPath = OsPath.of(OsType.FREEBSD, userHome).getOrThrow()
}
