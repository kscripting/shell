package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

class LinuxOs(override val vfs: LinuxVfs) : Os {
    override val marker: AutoTypedMarker<LinuxOs> = AutoTypedMarker.create("Linux")
    override val type: OsType = OsType.LINUX
    override val osTypePrefix: String = "linux"

    companion object {
        val marker: AutoTypedMarker<LinuxOs> = AutoTypedMarker.create("Linux")
    }
}
