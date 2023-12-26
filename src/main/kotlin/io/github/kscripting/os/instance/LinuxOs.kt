package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

class LinuxOs(override val vfs: LinuxVfs) : Os {
    override val marker: NamedAutoTypedMarker<LinuxOs> = NamedAutoTypedMarker.create("Linux")
    override val type: OsType = OsType.LINUX
    override val osTypePrefix: String = "linux"

    companion object {
        val marker: NamedAutoTypedMarker<LinuxOs> = NamedAutoTypedMarker.create("Linux")
    }
}
