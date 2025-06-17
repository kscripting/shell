package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

class FreeBsdOs(override val vfs: FreeBsdVfs) : Os {
    override val marker: AutoTypedMarker<FreeBsdOs> = AutoTypedMarker.create("FreeBsd")
    override val type: OsType = OsType.FREEBSD
    override val osTypePrefix: String = "freebsd"
}
