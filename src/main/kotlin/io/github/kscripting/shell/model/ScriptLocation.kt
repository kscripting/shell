package io.github.kscripting.shell.model

import java.net.URI

data class ScriptLocation(
    val level: Int,
    val scriptSource: ScriptSource,
    val scriptType: ScriptType,
    val sourceUri: URI?,
    val sourceContextUri: URI,
    val scriptName: String //without Kotlin extension (but possibly with other extensions)
)
