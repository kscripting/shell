package io.github.kscripting.shell.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.io.File
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.OpenOption
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.*

//Create OsPath
//https://stackoverflow.com/questions/1976007/what-characters-are-forbidden-in-windows-and-linux-directory-names
//The rule here is more strict than necessary, but it is at least good practice to follow such a rule.
//TODO: should I remove validation all together? It allows e.g. for globbing with asterisks and question marks
// maybe separate function in FileSystem: validate?
private val forbiddenCharacters = buildSet {
    add('<')
    add('>')
    add(':')
    add('"')
    add('|')
    add('?')
    add('*')
    for (i in 0 until 32) {
        add(i.toChar())
    }
}

private val windowsDriveRegex =
    "^([a-zA-Z]:(?=[\\\\/])|\\\\\\\\(?:[^*:<>?\\\\/|]+\\\\[^*:<>?\\\\/|]+|\\?\\\\(?:[a-zA-Z]:(?=\\\\)|(?:UNC\\\\)?[^*:<>?\\\\/|]+\\\\[^*:<>?\\\\/|]+)))".toRegex()


fun OsPath.Companion.of(vararg pathParts: String): OsPath = of(OsType.native, pathParts.toList())

fun OsPath.Companion.of(osType: OsType, vararg pathParts: String): OsPath = of(osType, pathParts.toList())

fun OsPath.Companion.of(osType: OsType, pathParts: List<String>): OsPath {
    return when (val result = ofWithEither(osType, pathParts)) {
        is Either.Right -> result.value
        is Either.Left -> throw result.value
    }
}

fun OsPath.Companion.ofOrNull(vararg pathParts: String): OsPath? = ofOrNull(OsType.native, pathParts.toList())

fun OsPath.Companion.ofOrNull(osType: OsType, vararg pathParts: String): OsPath? = ofOrNull(osType, pathParts.toList())

fun OsPath.Companion.ofOrNull(osType: OsType, pathParts: List<String>): OsPath? {
    return when (val result = ofWithEither(osType, pathParts)) {
        is Either.Right -> result.value
        is Either.Left -> null
    }
}

//Relaxed validation:
//1. It doesn't matter if there is '/' or '\' used as path separator - both are treated the same
//2. Duplicated trailing slashes '/' and backslashes '\' are just ignored
fun OsPath.Companion.ofWithEither(osType: OsType, pathParts: List<String>): Either<RuntimeException, OsPath> {
    val path = pathParts.joinToString("/")

    //Detect root
    val root: String = when {
        path.startsWith("~/") || path.startsWith("~\\") -> "~"
        osType.isPosixLike() && path.startsWith("/") -> "/"
        osType.isWindowsLike() -> {
            val match = windowsDriveRegex.find(path)
            val matchedRoot = match?.groupValues?.get(1)
            if (matchedRoot != null) matchedRoot + "\\" else ""
        }
        else -> ""
    }

    //TODO: https://learn.microsoft.com/pl-pl/dotnet/standard/io/file-path-formats
    // https://regex101.com/r/aU4yZ7/1

    //Remove also empty path parts - there were duplicated or trailing slashes / backslashes in initial path
    val pathPartsResolved = path.drop(root.length).split('/', '\\').filter { it.isNotBlank() }

    val normalizedPath = when (val result = normalize(root, pathPartsResolved)) {
        is Either.Right -> result.value
        is Either.Left -> return result.value.left()
    }

    //Validate root element of path and find out if it is absolute or relative
    val forbiddenCharacter = path.substring(root.length).find { forbiddenCharacters.contains(it) }

    if (forbiddenCharacter != null) {
        return IllegalArgumentException("Invalid character '$forbiddenCharacter' in path '$path'").left()
    }

    return OsPath(osType, root, normalizedPath).right()
}

fun OsPath.Companion.normalize(root: String, pathParts: List<String>): Either<RuntimeException, List<String>> {
    //Relative:
    // ./../ --> ../
    // ./a/../ --> ./
    // ./a/ --> ./a
    // ../a --> ../a
    // ../../a --> ../../a

    //Absolute:
    // /../ --> invalid (above root)
    // /a/../ --> /

    val isAbsolute: Boolean = root.isNotEmpty()

    val newParts = mutableListOf<String>()
    var index = 0

    while (index < pathParts.size) {
        if (pathParts[index] == ".") {
            //Just skip . without adding it to newParts
        } else if (pathParts[index] == "..") {
            if (isAbsolute && newParts.size == 0) {
                return IllegalArgumentException("Path after normalization goes beyond root element: '$root'").left()
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

    return newParts.right()
}

// Resolve OsPath
operator fun OsPath.div(osPath: OsPath): OsPath = resolve(osPath)
operator fun OsPath.div(path: String): OsPath = resolve(path)

fun OsPath.resolve(vararg pathParts: String): OsPath = resolve(OsPath.of(osType, *pathParts))

fun OsPath.resolve(osPath: OsPath): OsPath {
    require(osType == osPath.osType) {
        "Paths from different OS's: '${osType.name}' path can not be resolved with '${osPath.osType.name}' path"
    }

    require(osPath.isRelative) {
        "Can not resolve absolute or relative path '${path}' using absolute path '${osPath.path}'"
    }

    val newPathParts = buildList {
        addAll(pathParts)
        addAll(osPath.pathParts)
    }

    val normalizedPath = when (val result = OsPath.normalize(root, newPathParts)) {
        is Either.Right -> result.value
        is Either.Left -> throw result.value
    }

    return OsPath(osType, root, normalizedPath)
}

// Convert OsPath

//Not all conversions make sense: only Windows to CygWin and Msys and vice versa
//TODO: conversion of paths like /usr  /opt etc. is wrong; it needs also windows root of installation cygwin/msys
// base path: cygpath -w /
fun OsPath.convert(targetOsType: OsType /*nativeRootPath: OsPath = emptyPath*/): OsPath {
    if (osType == targetOsType) {
        return this
    }

    if ((osType.isPosixLike() && targetOsType.isPosixLike()) || (osType.isWindowsLike() && targetOsType.isWindowsLike())) {
        return OsPath(targetOsType, root, pathParts)
    }

    val toHosted = osType.isWindowsLike() && targetOsType.isPosixHostedOnWindows()
    val toNative = osType.isPosixHostedOnWindows() && targetOsType.isWindowsLike()

    require(toHosted || toNative) {
        "Only paths conversion between Windows and Posix hosted on Windows are supported"
    }

    val newParts = mutableListOf<String>()
    var newRoot = ""

    when {
        toHosted -> {
            if (isAbsolute) {
                //root is like 'C:\'
                val drive = root.dropLast(2).lowercase()

                newRoot = "/"

                if (targetOsType == OsType.CYGWIN) {
                    newParts.add("cygdrive")
                    newParts.add(drive)
                } else {
                    newParts.add(drive)
                }
            }

            newParts.addAll(pathParts)
        }

        toNative -> {
            if (isAbsolute) {
                if (osType == OsType.CYGWIN) {
                    newRoot = pathParts[1] + ":\\"
                    newParts.addAll(pathParts.subList(2, pathParts.size))
                } else {
                    newRoot = pathParts[0] + ":\\"
                    newParts.addAll(pathParts.subList(1, pathParts.size))
                }
            } else {
                newParts.addAll(pathParts)
            }
        }

        else -> throw IllegalArgumentException("Invalid conversion: $this to ${targetOsType.name}")
    }

    return OsPath(targetOsType, newRoot, newParts)
}


// Interact with file system
fun File.toOsPath(): OsPath = OsPath.of(OsType.native, absolutePath)

fun Path.toOsPath(): OsPath = OsPath.of(OsType.native, absolutePathString())

fun URI.toOsPath(): OsPath =
    if (this.scheme == "file") File(this).toOsPath() else throw IllegalArgumentException("Invalid conversion from URL to OsPath")

fun String.toOsPath(osType: OsType = OsType.native): OsPath =
    OsPath.of(osType, this)


// Conversion from OsPath
fun OsPath.toNativePath(): Path = Paths.get(toNativeOsPath().path)

fun OsPath.toNativeFile(): File = toNativePath().toFile()

fun OsPath.toNativeOsPath(): OsPath = if (osType.isPosixHostedOnWindows()) convert(OsType.WINDOWS) else this


// OsPath operations
fun OsPath.exists(): Boolean = toNativePath().exists()

fun OsPath.createDirectories(): OsPath = OsPath.of(nativeType, toNativePath().createDirectories().pathString)
fun OsPath.deleteRecursively(): Boolean = this.toNativeFile().deleteRecursively()
fun OsPath.readBytes(): ByteArray = this.toNativeFile().readBytes()

fun OsPath.copyTo(target: OsPath, overwrite: Boolean = false): OsPath =
    OsPath.of(nativeType, toNativePath().copyTo(target.toNativePath(), overwrite).pathString)

fun OsPath.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption): Unit =
    toNativePath().writeText(text, charset, *options)

fun OsPath.readText(charset: Charset = Charsets.UTF_8): String = toNativePath().readText(charset)


// OsPath accessors
val OsPath.leaf get(): String = if (pathParts.isEmpty()) root else pathParts.last()

//val OsPath.rootOsPath
//    get():OsPath = if (isRelative) OsPath.emptyPath else OsPath.of(osType, root)

val OsPath.parent
    get(): OsPath = OsPath.of(osType, pathParts.dropLast(1))

val OsPath.nativeType
    get(): OsType = if (osType.isPosixHostedOnWindows()) OsType.WINDOWS else osType

val OsPath.extension
    get(): String? = leaf.substringAfterLast('.', "")

val OsPath.path: String
    get() = root + pathParts.joinToString((if (osType.isWindowsLike()) "\\" else "/")) { it }
