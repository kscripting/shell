package io.github.kscripting.shell.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right

//Path representation for different OSes
@Suppress("MemberVisibilityCanBePrivate")
data class OsPath(val osType: OsType, val root: String, val pathParts: List<String>) {
    val isEmpty: Boolean get() = this === emptyPath
    val isRelative: Boolean get() = root.isEmpty()
    val isAbsolute: Boolean get() = !isRelative
    val pathSeparator: Char get() = if (osType.isWindowsLike()) '\\' else '/'
    val path get(): String = root + pathParts.joinToString(pathSeparator.toString()) { it }

    operator fun div(osPath: OsPath): OsPath {
        return resolve(osPath)
    }

    operator fun div(path: String): OsPath {
        return resolve(path)
    }

    fun resolve(vararg pathParts: String): OsPath {
        return resolve(createOrThrow(osType, *pathParts))
    }

    fun resolve(osPath: OsPath): OsPath {
        if (osPath.isEmpty) {
            return this
        }

        require(osType == osPath.osType || osType == OsType.ANY) {
            "Paths from different OS's: '${osType.name}' path can not be resolved with '${osPath.osType.name}' path"
        }

        require(osPath.isRelative) {
            "Can not resolve absolute or relative path '${path}' using absolute path '${osPath.path}'"
        }

        val newPathParts = buildList {
            addAll(pathParts)
            addAll(osPath.pathParts)
        }

        val normalizedPath = when (val result = normalize(root, newPathParts)) {
            is Either.Right -> result.value
            is Either.Left -> throw IllegalArgumentException(result.value)
        }

        return OsPath(osType, root, normalizedPath)
    }

    //Not all conversions make sense: only Windows to CygWin and Msys and vice versa
    //TODO: conversion of paths like /usr  /opt etc. is wrong; it needs also windows root of installation cygwin/msys
    // base path: cygpath -w /
    fun convert(targetOsType: OsType /*nativeRootPath: OsPath = emptyOsPath*/): OsPath {
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

    override fun toString(): String = "$path [${osType.name}]"

    companion object {
        val emptyPath = OsPath(OsType.ANY, "", emptyList())

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


        fun createOrThrow(vararg pathParts: String): OsPath = createOrThrow(OsType.native, pathParts.toList())

        fun createOrThrow(osType: OsType, vararg pathParts: String): OsPath = createOrThrow(osType, pathParts.toList())

        fun createOrThrow(osType: OsType, pathParts: List<String>): OsPath {
            return when (val result = internalCreate(osType, pathParts)) {
                is Either.Right -> result.value
                is Either.Left -> throw IllegalArgumentException(result.value)
            }
        }

        fun create(vararg pathParts: String): OsPath? = create(OsType.native, pathParts.toList())

        fun create(osType: OsType, vararg pathParts: String): OsPath? = create(osType, pathParts.toList())

        fun create(osType: OsType, pathParts: List<String>): OsPath? {
            return when (val result = internalCreate(osType, pathParts)) {
                is Either.Right -> result.value
                is Either.Left -> null
            }
        }

        //Relaxed validation:
        //1. It doesn't matter if there is '/' or '\' used as path separator - both are treated the same
        //2. Duplicated or trailing slashes '/' and backslashes '\' are just ignored
        private fun internalCreate(osType: OsType, pathParts: List<String>): Either<String, OsPath> {
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

            if (root.isEmpty() && normalizedPath.isEmpty()) {
                return emptyPath.right()
            }

            //Validate root element of path and find out if it is absolute or relative
            val forbiddenCharacter = path.substring(root.length).find { forbiddenCharacters.contains(it) }

            if (forbiddenCharacter != null) {
                return "Invalid character '$forbiddenCharacter' in path '$path'".left()
            }

            return OsPath(osType, root, normalizedPath).right()
        }

        fun normalize(root: String, pathParts: List<String>): Either<String, List<String>> {
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

                    if (isAbsolute && newParts.size == 1) {
                        return "Path after normalization goes beyond root element: '$root'".left()
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
    }
}
