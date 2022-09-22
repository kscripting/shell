package io.github.kscripting.shell.model

data class ScriptContext(
    val osType: OsType,
    val workingDir: OsPath,
    val executorDir: OsPath,
    val scriptLocation: ScriptLocation
)
