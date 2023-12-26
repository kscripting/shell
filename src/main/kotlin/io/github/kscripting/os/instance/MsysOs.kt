package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType

class MsysOs(override val nativeOs: WindowsOs, override val vfs: MsysVfs) : HostedOs {
    override val type: OsType = OsType.MSYS
    override val osTypePrefix: String = "msys"
}
