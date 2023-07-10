package io.github.kscripting.shell.model

//Path representation for different OSes
data class OsPath(val osType: OsType, val root: String, val pathParts: List<String>) {
    val isRelative: Boolean get() = root.isEmpty()
    val isAbsolute: Boolean get() = !isRelative

    companion object
}
