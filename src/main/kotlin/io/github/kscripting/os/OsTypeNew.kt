package io.github.kscripting.os

enum class OsTypeNew {
    LINUX, WINDOWS, CYGWIN, MSYS, MACOS, FREEBSD;

    fun isPosixLike() =
        (this == LINUX || this == MACOS || this == FREEBSD || this == CYGWIN || this == MSYS)

    fun isPosixHostedOnWindows() = (this == CYGWIN || this == MSYS)
    fun isWindowsLike() = (this == WINDOWS)
}