package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.ShellExecutor
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.util.Sanitizer
import java.io.InputStream

abstract class ShellTestCompanionBase {
    open fun commonEnvAdjuster(specificEnvAdjuster: EnvAdjuster = {}): EnvAdjuster {
        return { map ->
            map[TestContext.pathEnvVariableName] = TestContext.pathEnvVariableCalculatedPath
            specificEnvAdjuster(map)
        }
    }

    open fun runProcess(
        command: String,
        inputSanitizer: Sanitizer?,
        outputSanitizer: Sanitizer?,
        inputStream: InputStream?,
        envAdjuster: EnvAdjuster
    ): ProcessResult {
        //In MSYS all quotes should be single quotes, otherwise content is interpreted e.g. backslashes.
        //(MSYS bash interpreter is also replacing double quotes into the single quotes: see: bash -xc 'kscript "println(1+1)"')
        val newCommand = when {
            TestContext.osType.isPosixHostedOnWindows() -> command.replace('"', '\'')
            else -> command
        }

        return ShellExecutor.evalAndGobble(
            newCommand,
            TestContext.osType,
            null,
            inputSanitizer = inputSanitizer ?: TestContext.defaultInputSanitizer,
            outputSanitizer = outputSanitizer ?: TestContext.defaultOutputSanitizer,
            inputStream = inputStream,
            envAdjuster = commonEnvAdjuster(envAdjuster)
        )
    }
}
