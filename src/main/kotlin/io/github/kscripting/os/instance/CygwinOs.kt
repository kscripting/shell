package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew

class CygwinOs(override val vfs: CygwinVfs, override val nativeOs: WindowsOs) : HostedOs {
    override val type: OsTypeNew = OsTypeNew.CYGWIN
    override val osTypePrefix: String = "cygwin"
}
