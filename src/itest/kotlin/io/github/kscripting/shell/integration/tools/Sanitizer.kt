package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.model.ProcessResult

//Mapping:
//[bs] --> \\
//[nl] --> \n
class Sanitizer(private val mapping: Map<String, String> = mapOf()) {
    fun sanitizeInput(string: String): String {
        return sanitize(string) { key, value ->
            this.replace(key, value)
        }
    }

    fun sanitizeOutput(string: String): String {
        return sanitize(string) { key, value ->
            this.replace(value, key)
        }
    }

    fun sanitize(processResult: ProcessResult): ProcessResult {
        return processResult.copy(
            stdout = sanitizeOutput(processResult.stdout),
            stderr = sanitizeOutput(processResult.stderr)
        )
    }

    fun sanitize(string: String, fn: String.(String, String) -> String): String {
        var result = string

        for (entry in mapping.entries) {
            result = result.fn(entry.key, entry.value)
        }

        return result
    }
}
