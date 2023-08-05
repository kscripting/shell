package io.github.kscripting.os

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker

val CURRENT_OS = AutoTypedMarker.create<Os>()

interface Os {
    val type: OsType
    val userHome: OsPath
    val pathSeparator: String
}
