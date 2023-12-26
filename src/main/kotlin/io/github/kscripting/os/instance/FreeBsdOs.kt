package io.github.kscripting.os.instance

import io.github.kscripting.os.Os
import io.github.kscripting.os.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.DefaultTypedMarker
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

class FreeBsdOs(override val vfs: FreeBsdVfs) : Os {
    override val marker: NamedAutoTypedMarker<FreeBsdOs> = NamedAutoTypedMarker.create("FreeBsd")
    override val type: OsType = OsType.FREEBSD
    override val osTypePrefix: String = "freebsd"
}
