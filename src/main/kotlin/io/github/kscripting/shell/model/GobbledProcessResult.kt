package io.github.kscripting.shell.model

import io.github.kscripting.shell.util.ShellEscapeUtils.whitespaceCharsToSymbols

data class GobbledProcessResult(val command: String, val exitCode: Int, val stdout: String, val stderr: String) {
    override fun toString(): String {
        return """|Command     : '${whitespaceCharsToSymbols(command)}'
                  |Exit Code   : $exitCode   
                  |Stdout      : '${whitespaceCharsToSymbols(stdout)}'
                  |Stderr      : '${whitespaceCharsToSymbols(stderr)}'
                  |""".trimMargin()
    }
}
