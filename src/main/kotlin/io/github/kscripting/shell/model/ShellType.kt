package io.github.kscripting.shell.model

enum class ShellType(vararg val executorCommand: String) {
    NONE(),
    BASH("bash", "-c"),
    SH("sh", "-c"),
    CMD("cmd", "/c")
}
