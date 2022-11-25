package io.github.kscripting.shell.util

object ShellEscapeUtils {
    fun whitespaceCharsToSymbols(string: String): String =
        string.replace("\\", "[bs]").lines().joinToString("[nl]")
}
