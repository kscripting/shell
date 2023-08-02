package io.github.kscripting.os

import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.OsType
import net.igsoft.typeutils.marker.AutoTypedMarker
import net.igsoft.typeutils.marker.TypedMarker

interface Os {
    val type: OsType

    val userHome: OsPath
    val pathSeparator: String

    fun path(vararg pathParts: String): OsPath

    companion object {
        val CURRENT_OS = AutoTypedMarker.create<Os>()
    }
}
