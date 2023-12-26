package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

class CygwinOs(override val vfs: CygwinVfs, override val nativeOs: WindowsOs) : HostedOs {
    override val marker: NamedAutoTypedMarker<CygwinOs> = NamedAutoTypedMarker.create("Cygwin")
    override val type: OsType = OsType.CYGWIN
    override val osTypePrefix: String = "cygwin"
}
