package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

class WindowsOs(override val vfs: WindowsVfs) : Os {
    override val marker: NamedAutoTypedMarker<WindowsOs> = NamedAutoTypedMarker.create("Windows")
    override val type: OsType = OsType.WINDOWS
    override val osTypePrefix: String = "windows"
}
