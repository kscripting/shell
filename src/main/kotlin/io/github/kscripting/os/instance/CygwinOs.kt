package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

class CygwinOs(override val vfs: CygwinVfs, override val nativeOs: WindowsOs) : HostedOs {
    override val marker: AutoTypedMarker<CygwinOs> = AutoTypedMarker.create("Cygwin")
    override val type: OsType = OsType.CYGWIN
    override val osTypePrefix: String = "cygwin"
}
