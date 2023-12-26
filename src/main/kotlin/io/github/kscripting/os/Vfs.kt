package io.github.kscripting.os

import io.github.kscripting.os.model.OsPath


interface Vfs {
    val type: OsType
    val pathSeparator: String
    val userHome: OsPath

    //Relaxed validation:
    //1. It doesn't matter if there is '/' or '\' used as path separator - both are treated the same
    //2. Duplicated or trailing slashes '/' and backslashes '\' are ignored
    fun createOsPath(path: String): OsPath
    fun createOsPath(root: String, pathParts: List<String>): OsPath =
        createOsPath(root.trim() + "/" + pathParts.joinToString("/"))

    fun createOsPath(pathParts: List<String>): OsPath =
        createOsPath(pathParts.joinToString("/"))

    fun createOsPath(vararg pathParts: String): OsPath =
        createOsPath(pathParts.joinToString("/"))

    fun isValid(path: String): Boolean
}
