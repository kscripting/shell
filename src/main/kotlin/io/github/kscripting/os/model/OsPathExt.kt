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

fun OsPath.toNative(): Result<OsPath> {
    if (!osType.isPosixHostedOnWindows()) {
        //Everything besides Cygwin/Msys is native...
        return Result.success(this)
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

fun Result<OsPath>.toNative(): Result<OsPath> = flatMap { it.toNative() }


fun <E> List<E>.startsWith(list: List<E>): Boolean = (this.size >= list.size && this.subList(0, list.size) == list)

fun OsPath.startsWith(osPath: OsPath): Boolean = root == osPath.root && pathParts.startsWith(osPath.pathParts)

fun OsPath.toHosted(targetOs: OsType<*>): Result<OsPath> = runCatching{
    if (osType == targetOs) {
        //This is already targetOs...
        return Result.success(this)
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

fun Result<OsPath>.toHosted(targetOs: OsType<*>): Result<OsPath> = flatMap { it.toHosted(targetOs) }


val Result<OsPath>.path: Result<String> get() = map { it.path }

// Conversion to OsPath
fun File.toOsPath(): Result<OsPath> = OsPath(OsType.native, absolutePath)

fun Path.toOsPath(): Result<OsPath> = OsPath(OsType.native, absolutePathString())

fun URI.toOsPath(): Result<OsPath> =
    if (this.scheme == "file") File(this).toOsPath() else Result.failure(IllegalArgumentException("Invalid conversion from URL to OsPath"))


// Conversion from OsPath
fun OsPath.toNativePath(): Result<Path> = toNative().path.map { Paths.get(it) }

fun OsPath.toNativeFile(): Result<File> = toNative().path.map { File(it) }
fun Result<OsPath>.toNativeFile(): Result<File> = toNative().path.map { File(it) }

fun OsPath.toNativeUri(): Result<URI> = toNative().path.map { File(it).toURI() }


// OsPath operations
fun OsPath.exists(): Result<Boolean> = toNativePath().map { it.exists() }
val Result<OsPath>.exists: Result<Boolean> get() = flatMap { it.exists() }

fun OsPath.createDirectories(): Result<OsPath> = Result.runCatching {
    return OsPath(nativeType, toNativePath().getOrThrow().createDirectories().pathString)
}

fun Result<OsPath>.createDirectories(): Result<OsPath> = flatMap { it.createDirectories() }


fun OsPath.copyTo(target: OsPath, overwrite: Boolean = false): Result<OsPath> = Result.run {
    return OsPath(
        nativeType,
        toNativePath().getOrThrow().copyTo(target.toNativePath().getOrThrow(), overwrite).pathString
    )
}

fun Result<OsPath>.copyTo(target: OsPath, overwrite: Boolean = false): Result<OsPath> =
    flatMap { it.copyTo(target, overwrite) }

fun OsPath.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption): Result<Unit> =
    runCatching {
        toNativePath().getOrThrow().writeText(text, charset, *options)
    }

fun OsPath.readText(charset: Charset = Charsets.UTF_8): Result<String> = runCatching {
    toNativePath().getOrThrow().readText(charset)
}

fun Result<OsPath>.readText(charset: Charset = Charsets.UTF_8): Result<String> = flatMap { it.readText(charset) }


operator fun OsPath.div(osPath: OsPath): Result<OsPath> = resolve(osPath)
operator fun Result<OsPath>.div(osPath: OsPath): Result<OsPath> = flatMap { it.div(osPath) }

operator fun OsPath.div(path: String): Result<OsPath> = resolve(path)
operator fun Result<OsPath>.div(path: String): Result<OsPath> = flatMap { it.div(path) }


fun OsPath.resolve(vararg pathParts: String): Result<OsPath> = resolve(OsPath(osType, *pathParts).getOrThrow())
fun Result<OsPath>.resolve(vararg pathParts: String): Result<OsPath> = flatMap { it.resolve(*pathParts) }

fun OsPath.resolve(osPath: OsPath): Result<OsPath> {
    if (osType != osPath.osType) {
        return Result.failure(IllegalArgumentException("Paths from different OS's: '${osType}' path can not be resolved with '${osPath.osType}' path"))
    }

    if (osPath.isAbsolute) {
        return Result.failure(IllegalArgumentException("Can not resolve absolute or relative path '${path}' using absolute path '${osPath.path}'"))
    }

    val newPathParts = buildList {
        addAll(pathParts)
        addAll(osPath.pathParts)
    }

    val normalizedPath = OsPath.normalize(root, newPathParts).getOrElse { return Result.failure(it) }
    return OsPath(osType, root, normalizedPath)
}

fun Result<OsPath>.resolve(osPath: OsPath): Result<OsPath> = flatMap { it.resolve(osPath) }
fun Result<OsPath>.resolve(osPath: Result<OsPath>): Result<OsPath> {
    val currentPath = this.getOrElse { return Result.failure(it) }
    val newPath = osPath.getOrElse { return Result.failure(it) }
    return currentPath.resolve(newPath)
}


// OsPath accessors

//val OsPath.rootOsPath
//    get() = OsPath.createOrThrow(osType, root)

//val OsPath.parent
//    get() = toNativePath().parent

val OsPath.nativeType
    get() = if (osType.isPosixHostedOnWindows()) OsType.WINDOWS else osType

val OsPath.extension
    get() = leaf.substringAfterLast('.', "")
