package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.integration.tools.TestContext.runProcess
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster

object TestAssertion {
    private val defaultSanitizer =
        Sanitizer(mapOf("[bs]" to "\\", "[nl]" to System.getProperty("line.separator"), "[tb]" to "\t"))

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
        sanitizer: Sanitizer = defaultSanitizer,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, stdOut, eq(stdErr), sanitizer, envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String,
        stdErr: TestMatcher<String>,
        sanitizer: Sanitizer = defaultSanitizer,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, eq(stdOut), stdErr, sanitizer, envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: String = "",
        stdErr: String = "",
        sanitizer: Sanitizer = defaultSanitizer,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult = verify(command, exitCode, eq(stdOut), eq(stdErr), sanitizer, envAdjuster)

    fun verify(
        command: String,
        exitCode: Int = 0,
        stdOut: TestMatcher<String>,
        stdErr: TestMatcher<String>,
        sanitizer: Sanitizer = defaultSanitizer,
        envAdjuster: EnvAdjuster = {}
    ): ProcessResult {
        val processResult = runProcess(sanitizer.sanitizeInput(command), envAdjuster)
        println(sanitizer.sanitize(processResult))

        val extCde = genericEquals(exitCode)

        extCde.checkAssertion("ExitCode", processResult.exitCode, sanitizer)
        stdOut.checkAssertion("StdOut", processResult.stdout, sanitizer)
        stdErr.checkAssertion("StdErr", processResult.stderr, sanitizer)
        println()

        return processResult
    }
}
