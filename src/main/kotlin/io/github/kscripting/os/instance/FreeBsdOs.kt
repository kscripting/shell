package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsTypeNew

class FreeBsdOs(override val vfs: FreeBsdVfs) : Os {
    override val type: OsTypeNew = OsTypeNew.FREEBSD
    override val osTypePrefix: String = "freebsd"
}
