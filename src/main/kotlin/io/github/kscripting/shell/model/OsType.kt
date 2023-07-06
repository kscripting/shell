package io.github.kscripting.shell.model

import org.apache.commons.lang3.SystemUtils

enum class OsType(val osName: String) {
    ANY("any"), LINUX("linux"), FREEBSD("freebsd"), MACOS("darwin"), CYGWIN("cygwin"), MSYS("msys"), WINDOWS("windows");

    fun isPosixLike() = (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)
    fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    fun isWindowsLike() = (this == WINDOWS)

    companion object {
        val native: OsType = guessNativeType()

        fun findOrThrow(name: String): OsType = find(name) ?: throw IllegalArgumentException("Unsupported OS: '$name'")

        // Exact comparison (it.osName.equals(name, true)) seems to be not feasible as there is also e.g. "darwin21"
        // "darwin19", "linux-musl" (for Docker Alpine), "linux-gnu" and maybe even other osTypes. But it seems that
        // startsWith() covers all cases.
        fun find(name: String): OsType? = values().find { name.startsWith(it.osName, true) }

        private fun guessNativeType(): OsType {
            when {
                SystemUtils.IS_OS_LINUX -> return LINUX
                SystemUtils.IS_OS_MAC -> return MACOS
                SystemUtils.IS_OS_WINDOWS -> return WINDOWS
                SystemUtils.IS_OS_FREE_BSD -> return FREEBSD
            }

            return LINUX
        }
    }
}
