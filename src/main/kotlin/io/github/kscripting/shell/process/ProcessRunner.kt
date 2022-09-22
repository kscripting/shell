package io.github.kscripting.shell.process

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.model.toNativeFile
import java.util.concurrent.TimeUnit

typealias EnvAdjuster = (MutableMap<String, String>) -> Unit

object ProcessRunner {
    fun runProcess(
        vararg command: String, workingDirectory: OsPath? = null, envAdjuster: EnvAdjuster = {}
    ): ProcessResult {
        return runProcess(command.asList(), workingDirectory, envAdjuster)
    }

    fun runProcess(
        command: List<String>, workingDirectory: OsPath? = null, envAdjuster: EnvAdjuster = {}
    ): ProcessResult {
        try {
            // simplify with https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
            val process = ProcessBuilder(command).directory(workingDirectory?.toNativeFile()).apply {
                envAdjuster(environment())
            }.start()

            // we need to gobble the streams to prevent that the internal pipes hit their respective buffer limits, which
            // would lock the sub-process execution (see see https://github.com/holgerbrandl/kscript/issues/55
            // https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
            val inputStreamReader = StreamGobbler(process.inputStream).start()
            val errorStreamReader = StreamGobbler(process.errorStream).start()

            val waitTimeMinutes = 10L
            val exitedNormally = process.waitFor(waitTimeMinutes, TimeUnit.MINUTES)

            if (!exitedNormally) {
                throw IllegalStateException("Command has timed out after $waitTimeMinutes minutes.")
            }

            // we need to wait for the gobbler threads, or we may lose some output (e.g. in case of short-lived processes
            return ProcessResult(
                command.joinToString(" "), process.exitValue(), inputStreamReader.output, errorStreamReader.output
            )
        } catch (e: Exception) {
            throw IllegalStateException("Error executing command: '$command'", e)
        }
    }
}
