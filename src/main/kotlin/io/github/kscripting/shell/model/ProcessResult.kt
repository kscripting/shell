package io.github.kscripting.shell.model

import io.github.kscripting.shell.util.ShellEscapeUtils.whitespaceCharsToSymbols

data class ProcessResult(val command: String, val exitCode: Int) {
    override fun toString(): String {
        return """|Command     : '${whitespaceCharsToSymbols(command)}'
                  |Exit Code   : $exitCode   
                  |""".trimMargin()
    }
}
