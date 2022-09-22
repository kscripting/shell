package io.github.kscripting.shell

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.OsType
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.process.ProcessRunner

object ShellExecutor {
    fun eval(
        osType: OsType, command: String, workingDirectory: OsPath? = null, envAdjuster: EnvAdjuster = {}
    ): ProcessResult {
        //NOTE: cmd is an argument to shell (bash/cmd), so it should stay not split by whitespace as a single string
        if (osType == OsType.WINDOWS) {
            return ProcessRunner.runProcess(
                "cmd", "/c", command, workingDirectory = workingDirectory, envAdjuster = envAdjuster
            )
        }

        return ProcessRunner.runProcess(
            "bash", "-c", command, workingDirectory = workingDirectory, envAdjuster = envAdjuster
        )
    }
}
