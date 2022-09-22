package io.github.kscripting.shell.model

data class ProcessResult(val command: String, val exitCode: Int, val stdout: String, val stderr: String) {
    override fun toString(): String {
        return """|Command     : '${whitespaceCharsToSymbols(command)}'
                  |Exit Code   : $exitCode   
                  |Stdout      : '${whitespaceCharsToSymbols(stdout)}'
                  |Stderr      : '${whitespaceCharsToSymbols(stderr)}'
                  |""".trimMargin()
    }

    private fun whitespaceCharsToSymbols(string: String): String =
        string.replace("\\", "[bs]").lines().joinToString("[nl]")
}
