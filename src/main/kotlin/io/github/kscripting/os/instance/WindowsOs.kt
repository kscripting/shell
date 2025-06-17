package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

class WindowsOs(override val vfs: WindowsVfs) : Os {
    override val marker: AutoTypedMarker<WindowsOs> = AutoTypedMarker.create("Windows")
    override val type: OsType = OsType.WINDOWS
    override val osTypePrefix: String = "windows"
}
