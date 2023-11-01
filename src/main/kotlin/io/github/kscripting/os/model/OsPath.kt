package io.github.kscripting.os.model

sealed interface OsPathError {
    object EmptyPath : OsPathError, RuntimeException()
    data class InvalidConversion(val errorMessage: String) : OsPathError, RuntimeException(errorMessage)
}

//Path representation for different OSes
@Suppress("MemberVisibilityCanBePrivate")
data class OsPath private constructor(val osType: OsType<*>, val root: String, val pathParts: List<String>) {
    val isRelative: Boolean get() = root.isEmpty() && pathParts.isNotEmpty()
    val isAbsolute: Boolean get() = root.isNotEmpty()

    val path: String get() = root + pathParts.joinToString(osType.value.pathSeparator) { it }
    val leaf: String get() = if (pathParts.isEmpty()) root else pathParts.last()

    override fun toString(): String = "$path [$osType]"

    companion object {
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


        //Relaxed validation:
        //1. It doesn't matter if there is '/' or '\' used as path separator - both are treated the same
        //2. Duplicated or trailing slashes '/' and backslashes '\' are just ignored

        operator fun invoke(osType: OsType<*>, root: String, pathParts: List<String>): OsPath {
            //TODO: verify root - do not allow empty paths
            if (root.isEmpty() && pathParts.isEmpty()) {
                throw OsPathError.EmptyPath
            }

            return OsPath(osType, root, pathParts)
        }

        operator fun invoke(vararg pathParts: String): OsPath {
            return invoke(OsType.native, pathParts.toList())
        }

        operator fun invoke(osType: OsType<*>, vararg pathParts: String): OsPath {
            return invoke(osType, pathParts.toList())
        }

        operator fun invoke(osType: OsType<*>, pathParts: List<String>): OsPath {
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
            val normalizedPath = normalize(root, pathPartsResolved)

            //Validate root element of path and find out if it is absolute or relative
            val forbiddenCharacter = path.substring(root.length).find { forbiddenCharacters.contains(it) }

            if (forbiddenCharacter != null) {
                throw IllegalArgumentException("Invalid character '$forbiddenCharacter' in path '$path'")
            }

            return OsPath(osType, root, normalizedPath)
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

            val isAbsolute: Boolean = root.isNotEmpty()

            val newParts = mutableListOf<String>()
            var index = 0

            while (index < pathParts.size) {
                if (pathParts[index] == ".") {
                    //Just skip . without adding it to newParts
                } else if (pathParts[index] == "..") {
                    if (isAbsolute && newParts.size == 0) {
                        throw IllegalArgumentException("Path after normalization goes beyond root element: '$root'")
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
    }
}
