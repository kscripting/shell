package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType

class FreeBsdOs(override val vfs: FreeBsdVfs) : Os {
    override val type: OsType = OsType.FREEBSD
    override val osTypePrefix: String = "freebsd"
}
