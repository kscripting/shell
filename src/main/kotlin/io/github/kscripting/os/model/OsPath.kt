package io.github.kscripting.os.model

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs

sealed interface OsPathError {
    object EmptyPath : OsPathError, RuntimeException()
    data class InvalidConversion(val errorMessage: String) : OsPathError, RuntimeException(errorMessage)
}

//Path representation for different OSes
@Suppress("MemberVisibilityCanBePrivate")
//TODO: should be only instantiated from VFS, not in any place
data class OsPath<T : Vfs>(@Transient internal val vfs: T, val root: String, val pathParts: List<String>) {
    val osType: OsTypeNew = vfs.type
    val isRelative: Boolean get() = root.isEmpty() && pathParts.isNotEmpty()
    val isAbsolute: Boolean get() = root.isNotEmpty()

    val path: String get() = root + pathParts.joinToString(vfs.pathSeparator) { it }
    val leaf: String get() = if (pathParts.isEmpty()) root else pathParts.last()

    override fun toString(): String = "$path [$osType]"
}
