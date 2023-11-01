package io.github.kscripting.os.model

import io.github.kscripting.os.instance.HostedOs
import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

//Wrappers
fun <P, R> Result<P>.flatMap(fn: (P) -> Result<R>): Result<R> = fold(
    onSuccess = { fn(it) },
    onFailure = { Result.failure(it) }
)

fun OsPath.toNative(): OsPath {
    if (!osType.isPosixHostedOnWindows()) {
        //Everything besides Cygwin/Msys is native...
        return this
    }

    val hostedOs = osType.value as HostedOs

    val newParts = mutableListOf<String>()
    var newRoot = ""

    if (isAbsolute) {
        when (osType) {
            OsType.CYGWIN -> {
                if (pathParts[0].equals("cygdrive", true)) { //Paths referring /cygdrive
                    newRoot = pathParts[1] + ":\\"
                    newParts.addAll(pathParts.subList(2, pathParts.size))
                } else if (root == "~") { //Paths starting with ~
                    newRoot = hostedOs.nativeFileSystemRoot.root
                    newParts.addAll(hostedOs.nativeFileSystemRoot.pathParts)
                    newParts.addAll(hostedOs.userHome.pathParts)
                    newParts.addAll(pathParts)
                } else { //Any other path like: /usr/bin
                    newRoot = hostedOs.nativeFileSystemRoot.root
                    newParts.addAll(hostedOs.nativeFileSystemRoot.pathParts)
                    newParts.addAll(pathParts)
                }
            }

            OsType.MSYS -> {
                if (pathParts[0].length == 1 && (pathParts[0][0].code in 65..90 || pathParts[0][0].code in 97..122)) { //Paths referring with drive letter at the beginning
                    newRoot = pathParts[0] + ":\\"
                    newParts.addAll(pathParts.subList(1, pathParts.size))
                } else if (root == "~") { //Paths starting with ~
                    newRoot = hostedOs.nativeFileSystemRoot.root
                    newParts.addAll(hostedOs.nativeFileSystemRoot.pathParts)
                    newParts.addAll(hostedOs.userHome.pathParts)
                    newParts.addAll(pathParts)
                } else { //Any other path like: /usr/bin
                    newRoot = hostedOs.nativeFileSystemRoot.root
                    newParts.addAll(hostedOs.nativeFileSystemRoot.pathParts)
                    newParts.addAll(pathParts)
                }
            }
        }
    } else {
        newParts.addAll(pathParts)
    }

    return OsPath(hostedOs.nativeType, newRoot, newParts)
}

fun <E> List<E>.startsWith(list: List<E>): Boolean = (this.size >= list.size && this.subList(0, list.size) == list)

fun OsPath.startsWith(osPath: OsPath): Boolean = root == osPath.root && pathParts.startsWith(osPath.pathParts)

fun OsPath.toHosted(targetOs: OsType<*>): OsPath {
    if (osType == targetOs) {
        //This is already targetOs...
        return this
    }

    if (!(targetOs.isPosixHostedOnWindows() && osType == ((targetOs.value) as HostedOs).nativeType)) {
        throw OsPathError.InvalidConversion("You can convert only paths to hosted OS-es")
    }

    val newParts = mutableListOf<String>()
    var newRoot = ""

    if (isAbsolute) {
        val hostedOs = targetOs.value as HostedOs

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

            if (targetOs.value.type == OsType.CYGWIN) {
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

// Conversion to OsPath
fun File.toOsPath(): OsPath = OsPath(OsType.native, absolutePath)

fun Path.toOsPath(): OsPath = OsPath(OsType.native, absolutePathString())

fun URI.toOsPath(): OsPath =
    if (this.scheme == "file") File(this).toOsPath() else throw IllegalArgumentException("Invalid conversion from URL to OsPath")


// Conversion from OsPath
fun OsPath.toNativePath(): Path = Paths.get(toNative().path)

fun OsPath.toNativeFile(): File = File(toNative().path)

fun OsPath.toNativeUri(): URI = File(toNative().path).toURI()


// OsPath operations
fun OsPath.exists(): Boolean = toNativePath().exists()

fun OsPath.createDirectories(): OsPath = OsPath(nativeType, toNativePath().createDirectories().pathString)

fun OsPath.copyTo(target: OsPath, overwrite: Boolean = false): OsPath =
    OsPath(nativeType, toNativePath().copyTo(target.toNativePath(), overwrite).pathString)


fun OsPath.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption): Unit =
    toNativePath().writeText(text, charset, *options)

fun OsPath.readText(charset: Charset = Charsets.UTF_8): String = toNativePath().readText(charset)


operator fun OsPath.div(osPath: OsPath): OsPath = resolve(osPath)

operator fun OsPath.div(path: String): OsPath = resolve(path)

fun OsPath.resolve(vararg pathParts: String): OsPath = resolve(OsPath(osType, *pathParts))

fun OsPath.resolve(osPath: OsPath): OsPath {
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

    return OsPath.normalize(osType, root, newPathParts)
}

// OsPath accessors

//val OsPath.rootOsPath
//    get() = OsPath.createOrThrow(osType, root)

val OsPath.nativeType
    get() = if (osType.isPosixHostedOnWindows()) OsType.WINDOWS else osType

val OsPath.extension
    get() = leaf.substringAfterLast('.', "")
