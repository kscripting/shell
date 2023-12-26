package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

class MacOs(override val vfs: MacOsVfs) : Os {
    override val marker: NamedAutoTypedMarker<MacOs> = NamedAutoTypedMarker.create("Mac")
    override val type: OsType = OsType.MACOS
    override val osTypePrefix: String = "darwin"
}
