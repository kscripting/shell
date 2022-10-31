package io.github.kscripting.shell.process

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.model.toNativeFile
import java.io.PrintStream
import java.util.concurrent.TimeUnit

typealias EnvAdjuster = (MutableMap<String, String>) -> Unit

object ProcessRunner {
    val DEFAULT_OUT_PRINTERS = listOf(System.out)
    val DEFAULT_ERR_PRINTERS = listOf(System.err)

    fun runProcess(
        vararg command: String,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS,
    ): ProcessResult {
        return runProcess(
            command.asList(),
            workingDirectory,
            envAdjuster,
            waitTimeMinutes,
            outPrinter,
            errPrinter
        )
    }

    fun runProcess(
        command: List<String>,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS,
    ): ProcessResult {
        try {
            // simplify with https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
            val process = ProcessBuilder(command).directory(workingDirectory?.toNativeFile()).apply {
                envAdjuster(environment())
            }.start()

            // we need to gobble the streams to prevent that the internal pipes hit their respective buffer limits, which
            // would lock the sub-process execution (see see https://github.com/kscripting/kscript/issues/55
            // https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
            val inputStreamReader = StreamGobbler(process.inputStream, outPrinter).start()
            val errorStreamReader = StreamGobbler(process.errorStream, errPrinter).start()

            val exitedNormally = process.waitFor(waitTimeMinutes.toLong(), TimeUnit.MINUTES)

            inputStreamReader.finish()
            errorStreamReader.finish()

            if (!exitedNormally) {
                throw IllegalStateException("Command has timed out after $waitTimeMinutes minutes.")
            }

            return ProcessResult(command.joinToString(" "), process.exitValue())
        } catch (e: Exception) {
            throw IllegalStateException("Error executing command: '$command'", e)
        }
    }
}
