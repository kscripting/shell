package io.github.kscripting.os

import io.github.kscripting.os.model.OsPath


interface Vfs {
    val type: OsTypeNew
    val pathSeparator: String
    val userHome: OsPath<out Vfs>

    //Relaxed validation:
    //1. It doesn't matter if there is '/' or '\' used as path separator - both are treated the same
    //2. Duplicated or trailing slashes '/' and backslashes '\' are ignored
    fun createOsPath(path: String): OsPath<out Vfs>
    fun createOsPath(root: String, pathParts: List<String>): OsPath<out Vfs> =
        createOsPath(root.trim() + "/" + pathParts.joinToString("/"))

    fun createOsPath(pathParts: List<String>): OsPath<out Vfs> =
        createOsPath(pathParts.joinToString("/"))

    fun createOsPath(vararg pathParts: String): OsPath<out Vfs> =
        createOsPath(pathParts.joinToString("/"))

    fun isValid(path: String): Boolean

    fun <T : Vfs> normalize(osPath: OsPath<T>): OsPath<T> {
        //Relative:
        // ./../ --> ../
        // ./a/../ --> ./
        // ./a/ --> ./a
        // ../a --> ../a
        // ../../a --> ../../a

        //Absolute:
        // /../ --> invalid (above root)
        // /a/../ --> /

        val newParts = mutableListOf<String>()
        var index = 0

        while (index < osPath.pathParts.size) {
            if (osPath.pathParts[index] == ".") {
                //Just skip . without adding it to newParts
            } else if (osPath.pathParts[index] == "..") {
                if (osPath.isAbsolute && newParts.size == 0) {
                    throw IllegalArgumentException("Path after normalization goes beyond root element: '${osPath.root}'")
                }

                if (newParts.size > 0) {
                    when (newParts.last()) {
                        "." -> {
                            //It's the first element - other dots should be already removed before
                            newParts.removeAt(newParts.size - 1)
                            newParts.add("..")
                        }

                        ".." -> {
                            newParts.add("..")
                        }

                        else -> {
                            newParts.removeAt(newParts.size - 1)
                        }
                    }
                } else {
                    newParts.add("..")
                }
            } else {
                newParts.add(osPath.pathParts[index])
            }

            index += 1
        }

        return OsPath(osPath.vfs, osPath.root, newParts)
    }
}

fun <T : Vfs> createPosixOsPath(vfs: T, path: String): OsPath<T> {
    require(vfs.isValid(path))

    //Detect root
    val root: String = when {
        path.startsWith("~/") || path.startsWith("~\\") -> "~"
        path.startsWith("/") -> "/"
        else -> ""
    }

    return createFinalPath(vfs, path, root)
}

fun <T : Vfs> createFinalPath(vfs: T, path: String, root: String): OsPath<T> {
    //Remove also empty path parts - there were duplicated or trailing slashes / backslashes in initial path
    val pathWithoutRoot = path.drop(root.length)

    require(vfs.isValid(pathWithoutRoot))

    val pathPartsResolved = pathWithoutRoot.split('/', '\\').filter { it.isNotBlank() }
    return vfs.normalize(OsPath(vfs, root, pathPartsResolved))
}

