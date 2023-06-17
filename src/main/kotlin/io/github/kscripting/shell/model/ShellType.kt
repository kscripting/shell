package io.github.kscripting.shell.model

enum class ShellType(vararg val executorCommand: String) {
    NONE(),
    BASH("/usr/bin/env bash", "-c"),
    SH("/usr/bin/env sh", "-c"),
    CMD("cmd", "/c")
}
