package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.integration.tools.TestContext.runProcess
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster

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
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, stdOut, eq(stdErr), envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String,
        stdErr: TestMatcher<String>,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, eq(stdOut), stdErr, envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String = "",
        stdErr: String = "",
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, eq(stdOut), eq(stdErr), envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: TestMatcher<String>,
        stdErr: TestMatcher<String>,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult {
        val processResult = runProcess(command, envAdjuster)
        println(processResult)

        val extCde = genericEquals(exitCode)

        extCde.checkAssertion("ExitCode", processResult.exitCode)
        stdOut.checkAssertion("StdOut", processResult.stdout)
        stdErr.checkAssertion("StdErr", processResult.stderr)
        println()

        return processResult
    }
}
