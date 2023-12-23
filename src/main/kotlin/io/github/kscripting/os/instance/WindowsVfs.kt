package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs
import io.github.kscripting.os.createFinalPath
import io.github.kscripting.os.model.OsPath

class WindowsVfs(userHome: String) : Vfs {
    override val type: OsTypeNew = OsTypeNew.WINDOWS
    override val pathSeparator: String = "\\"

    //https://stackoverflow.com/questions/1976007/what-characters-are-forbidden-in-windows-and-linux-directory-names
    //The rule here is more strict than necessary, but it is at least good practice to follow such a rule.
    private val windowsDriveRegex =
        "^([a-zA-Z]:(?=[\\\\/])|\\\\\\\\(?:[^*:<>?\\\\/|]+\\\\[^*:<>?\\\\/|]+|\\?\\\\(?:[a-zA-Z]:(?=\\\\)|(?:UNC\\\\)?[^*:<>?\\\\/|]+\\\\[^*:<>?\\\\/|]+)))".toRegex()


    override val userHome: OsPath<WindowsVfs> = createOsPath(userHome)

    override fun createOsPath(path: String): OsPath<WindowsVfs> {
        //Detect root
        val root = when {
            path.startsWith("~/") || path.startsWith("~\\") -> "~"
            else -> {
                val match = windowsDriveRegex.find(path)
                val matchedRoot = match?.groupValues?.get(1)
                if (matchedRoot != null) matchedRoot + "\\" else ""
            }
        }

        return createFinalPath(this, path, root)
    }

    override fun isValid(path: String): Boolean {
        return path.none { INVALID_CHARS.contains(it) }
    }

    companion object {
        private const val INVALID_CHARS = "<>:\"|?*"
    }
}
