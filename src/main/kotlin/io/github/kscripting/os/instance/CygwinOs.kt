package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType

class CygwinOs(override val vfs: CygwinVfs, override val nativeOs: WindowsOs) : HostedOs {
    override val type: OsType = OsType.CYGWIN
    override val osTypePrefix: String = "cygwin"
}
