package io.github.kscripting.shell

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.OsType
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvironmentAdjuster
import io.github.kscripting.shell.process.ProcessRunner

class ShellConnector(private val osType: OsType) {
    fun eval(
        command: String, workingDirectory: OsPath? = null, environmentAdjuster: EnvironmentAdjuster = {}
    ): ProcessResult {
        //NOTE: cmd is an argument to shell (bash/cmd), so it should stay not split by whitespace as a single string
        if (osType == OsType.WINDOWS) {
            return ProcessRunner.runProcess(
                "cmd", "/c", command, workingDirectory = workingDirectory, environmentAdjuster = environmentAdjuster
            )
        }

        return ProcessRunner.runProcess(
            "bash", "-c", command, workingDirectory = workingDirectory, environmentAdjuster = environmentAdjuster
        )
    }

    fun which(command: String, environmentAdjuster: EnvironmentAdjuster = {}): List<String> =
        eval("${if (osType == OsType.WINDOWS) "where" else "which"} $command", null, environmentAdjuster).stdout.trim()
            .lines()

    fun isInPath(command: String, environmentAdjuster: EnvironmentAdjuster = {}): Boolean {
        val paths = which(command, environmentAdjuster)
        return paths.isNotEmpty() && paths[0].isNotBlank()
    }
}
