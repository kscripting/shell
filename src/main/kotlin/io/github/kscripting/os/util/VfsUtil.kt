package io.github.kscripting.os.util

import io.github.kscripting.os.Vfs
import io.github.kscripting.os.instance.CygwinVfs
import io.github.kscripting.os.instance.HostedVfs
import io.github.kscripting.os.instance.WindowsVfs
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.startsWith


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
    return OsPath(vfs, root, normalize(root, pathPartsResolved))
}

fun <T: HostedVfs> toHostedConverter(vfs: T, osPath: OsPath<WindowsVfs>): OsPath<T> {
    val newParts = mutableListOf<String>()
    var newRoot = ""

    if (osPath.isAbsolute) {
        val nativeFsRoot = vfs.nativeFsRoot as OsPath<WindowsVfs>

        if (osPath.startsWith(nativeFsRoot)) {
            if (osPath.pathParts.subList(nativeFsRoot.pathParts.size, osPath.pathParts.size)
                    .startsWith(vfs.userHome.pathParts)
            ) {
                //It is user home: ~
                newRoot = "~/"
                newParts.addAll(
                    osPath.pathParts.subList(
                        nativeFsRoot.pathParts.size + vfs.userHome.pathParts.size,
                        osPath.pathParts.size
                    )
                )
            } else {
                //It is hostedOs root: /
                newRoot = "/"
                newParts.addAll(osPath.pathParts.subList(nativeFsRoot.pathParts.size, osPath.pathParts.size))
            }
        } else {
            //Otherwise:
            //root is like 'C:\'
            val drive = osPath.root.dropLast(2).lowercase()

            newRoot = "/"

            //TODO: not generic!!
            if (vfs is CygwinVfs) {
                newParts.add("cygdrive")
            }

            newParts.add(drive)

            newParts.addAll(osPath.pathParts)
        }
    } else {
        newParts.addAll(osPath.pathParts)
    }

    return OsPath(vfs, newRoot, newParts)
}

fun normalize(root: String, pathParts: List<String>): List<String> {
    //Relative:
    // ./../ --> ../
    // ./a/../ --> ./
    // ./a/ --> ./a
    // ../a --> ../a
    // ../../a --> ../../a

    //Absolute:
    // /../ --> invalid (above root)
    // /a/../ --> /

    val isAbsolute = root.isNotEmpty()
    val newParts = mutableListOf<String>()
    var index = 0

    while (index < pathParts.size) {
        if (pathParts[index] == ".") {
            //Just skip . without adding it to newParts
        } else if (pathParts[index] == "..") {
            if (isAbsolute && newParts.size == 0) {
                throw IllegalArgumentException("Path after normalization goes beyond root element: '${root}'")
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
            newParts.add(pathParts[index])
        }

        index += 1
    }

    return newParts
}
