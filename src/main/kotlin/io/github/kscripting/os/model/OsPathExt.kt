package io.github.kscripting.os.model

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs
import io.github.kscripting.os.instance.HostedOs
import java.io.File

//import io.github.kscripting.os.Vfs
import io.github.kscripting.os.instance.HostedVfs
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

//import java.io.File
//import java.net.URI
//import java.nio.charset.Charset
//import java.nio.file.OpenOption
//import java.nio.file.Path
//import java.nio.file.Paths
//import kotlin.io.path.*
//
fun <T: Vfs> OsPath<T>.toNative(): OsPath<*> = vfs.toNative(this)

/*
fun <E> List<E>.startsWith(list: List<E>): Boolean = (this.size >= list.size && this.subList(0, list.size) == list)

fun <T : Vfs> OsPath<T>.startsWith(osPath: OsPath<T>): Boolean = root == osPath.root && pathParts.startsWith(osPath.pathParts)


fun <T : Vfs> OsPath<T>.toHosted(targetOs: OsType<*>): OsPath<T> {
    if (osType == targetOs) {
        //This is already targetOs...
        return this
    }

    if (!(targetOs.isPosixHostedOnWindows() && osType == ((targetOs.os) as HostedOs).nativeType)) {
        throw OsPathError.InvalidConversion("You can convert only paths to hosted OS-es")
    }

    val newParts = mutableListOf<String>()
    var newRoot = ""

    if (isAbsolute) {
        val hostedOs = targetOs.os as HostedOs

        if (this.startsWith(hostedOs.nativeFileSystemRoot)) {
            if (pathParts.subList(hostedOs.nativeFileSystemRoot.pathParts.size, pathParts.size)
                    .startsWith(hostedOs.userHome.pathParts)
            ) {
                //It is user home: ~
                newRoot = "~/"
                newParts.addAll(
                    pathParts.subList(
                        hostedOs.nativeFileSystemRoot.pathParts.size + hostedOs.userHome.pathParts.size,
                        pathParts.size
                    )
                )
            } else {
                //It is hostedOs root: /
                newRoot = "/"
                newParts.addAll(pathParts.subList(hostedOs.nativeFileSystemRoot.pathParts.size, pathParts.size))
            }
        } else {
            //Otherwise:
            //root is like 'C:\'
            val drive = root.dropLast(2).lowercase()

            newRoot = "/"

            if (targetOs.os.type == GlobalOsType.CYGWIN) {
                newParts.add("cygdrive")
                newParts.add(drive)
            } else {
                newParts.add(drive)
            }

            newParts.addAll(pathParts)
        }
    } else {
        newParts.addAll(pathParts)
    }

    return OsPath(targetOs, newRoot, newParts)
}
*/

//// Conversion to OsPath
//fun File.toOsPath(): OsPath = OsPath(GlobalOsType.native, absolutePath)
//
//fun Path.toOsPath(): OsPath = OsPath(GlobalOsType.native, absolutePathString())
//
//fun URI.toOsPath(): OsPath =
//    if (this.scheme == "file") File(this).toOsPath() else throw IllegalArgumentException("Invalid conversion from URL to OsPath")


//// Conversion from OsPath
fun OsPath<*>.toNativePath(): Path = Paths.get(toNative().path)
fun OsPath<*>.toNativeFile(): File = File(toNative().path)
fun OsPath<*>.toNativeUri(): URI = File(toNative().path).toURI()


//// OsPath operations
//fun OsPath.exists(): Boolean = toNativePath().exists()
//
//fun OsPath.createDirectories(): OsPath = OsPath(nativeType, toNativePath().createDirectories().pathString)
//
//fun OsPath.copyTo(target: OsPath, overwrite: Boolean = false): OsPath =
//    OsPath(nativeType, toNativePath().copyTo(target.toNativePath(), overwrite).pathString)
//
//
//fun OsPath.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption): Unit =
//    toNativePath().writeText(text, charset, *options)
//
//fun OsPath.readText(charset: Charset = Charsets.UTF_8): String = toNativePath().readText(charset)
//

operator fun <T : Vfs> OsPath<T>.div(osPath: OsPath<T>): OsPath<T> = resolve(osPath)

operator fun <T : Vfs> OsPath<T>.div(path: String): OsPath<T> = resolve(path)

fun <T : Vfs> OsPath<T>.resolve(vararg pathParts: String): OsPath<T> = resolve(vfs.createOsPath(pathParts.joinToString("/")) as OsPath<T>)

fun <T : Vfs> OsPath<T>.resolve(osPath: OsPath<T>): OsPath<T> {
    if (osType != osPath.osType) {
        throw IllegalArgumentException("Paths from different OS's: '${osType}' path can not be resolved with '${osPath.osType}' path")
    }

    if (osPath.isAbsolute) {
        throw IllegalArgumentException("Can not resolve absolute or relative path '${path}' using absolute path '${osPath.path}'")
    }

    val newPathParts = buildList {
        addAll(pathParts)
        addAll(osPath.pathParts)
    }

    return vfs.normalize(vfs.createOsPath(root, newPathParts) as OsPath<T>)
}

//// OsPath accessors
//
////val OsPath.rootOsPath
////    get() = OsPath.createOrThrow(osType, root)
//
//val OsPath.nativeType
//    get() = if (osType.isPosixHostedOnWindows()) GlobalOsType.WINDOWS else osType
//
//val OsPath.extension
//    get() = leaf.substringAfterLast('.', "")
