package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

class MsysOs(override val nativeOs: WindowsOs, override val vfs: MsysVfs) : HostedOs {
    override val marker: AutoTypedMarker<MsysOs> = AutoTypedMarker.create("Msys")
    override val type: OsType = OsType.MSYS
    override val osTypePrefix: String = "msys"
}
