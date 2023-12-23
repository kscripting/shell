package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew

class MsysOs(override val nativeOs: WindowsOs, override val vfs: MsysVfs) : HostedOs {
    override val type: OsTypeNew = OsTypeNew.MSYS
    override val osTypePrefix: String = "msys"
}
