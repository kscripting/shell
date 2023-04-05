package io.github.kscripting.shell

import io.github.kscripting.shell.model.ProcessResult

class ShellExecutor2 {

    fun execute(): ProcessResult {
        return ProcessResult(0, "", "")
    }

    companion object {
        fun builder() = ShellExecutor2Builder()
    }
}
