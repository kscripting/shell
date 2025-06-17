package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import io.github.kscripting.os.Vfs

abstract class PosixVfs(override val type: OsType) : Vfs {
    override val pathSeparator: String = "/"
    override fun isValid(path: String): Result<Unit> {
        // NOTE: https://stackoverflow.com/a/1311070/5321061
        for (char in path.withIndex()) {
            if (char.value in INVALID_CHARS) {
                return Result.failure(IllegalArgumentException("Invalid character '${char.value}' in path '$path'"))
            }
        }

        return Result.success(Unit)
    }

    companion object {
        private const val INVALID_CHARS = "\u0000"
    }
}
