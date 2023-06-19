package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.integration.tools.TestContext.runProcess
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.util.Sanitizer

object TestAssertion {
    fun <T : Any> genericEquals(value: T) = GenericEquals(value)

    fun any() = AnyMatch()
    fun eq(string: String, ignoreCase: Boolean = false) = Equals(string, ignoreCase)
    fun startsWith(string: String, ignoreCase: Boolean = false) = StartsWith(string, ignoreCase)
    fun contains(string: String, ignoreCase: Boolean = false) = Contains(string, ignoreCase)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: TestMatcher<String>,
        stdErr: String = "",
        envAdjuster: EnvAdjuster = {},
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null,
    ): ProcessResult = verify(command, exitCode, stdOut, eq(stdErr), envAdjuster, inputSanitizer, outputSanitizer)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String,
        stdErr: TestMatcher<String>,
        envAdjuster: EnvAdjuster = {},
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null,
    ): ProcessResult = verify(command, exitCode, eq(stdOut), stdErr, envAdjuster, inputSanitizer, outputSanitizer)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String = "",
        stdErr: String = "",
        envAdjuster: EnvAdjuster = {},
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null,
    ): ProcessResult = verify(command, exitCode, eq(stdOut), eq(stdErr), envAdjuster, inputSanitizer, outputSanitizer)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: TestMatcher<String>,
        stdErr: TestMatcher<String>,
        envAdjuster: EnvAdjuster = {},
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null,
    ): ProcessResult {
        val processResult = runProcess(command, envAdjuster, inputSanitizer, outputSanitizer)
        println(processResult)

        val extCde = genericEquals(exitCode)

        extCde.checkAssertion("ExitCode", processResult.exitCode)
        stdOut.checkAssertion("StdOut", processResult.stdout)
        stdErr.checkAssertion("StdErr", processResult.stderr)
        println()

        return processResult
    }
}
