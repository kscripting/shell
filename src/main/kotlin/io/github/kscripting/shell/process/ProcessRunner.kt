package io.github.kscripting.shell.process

import io.github.kscripting.shell.model.CommandTimeoutException
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.model.toNativeFile
import io.github.kscripting.shell.util.Sanitizer
import io.github.kscripting.shell.util.Sanitizer.Companion.EMPTY_SANITIZER
import java.io.InputStream
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
        inheritInput: Boolean = false,
        outputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS,
        inputStream: InputStream? = null
    ): Int {
        return runProcess(
            command.asList(),
            workingDirectory,
            envAdjuster,
            waitTimeMinutes,
            inheritInput,
            outputSanitizer,
            outPrinter,
            errPrinter,
            inputStream
        )
    }

    fun runProcess(
        command: List<String>,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        outputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS,
        inputStream: InputStream? = null
    ): Int {
        try {
            // simplify with https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
            val process = ProcessBuilder(command)
                .directory(workingDirectory?.toNativeFile())
                .redirectInput(if (inheritInput) ProcessBuilder.Redirect.INHERIT else ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .apply {
                    envAdjuster(environment())
                }.start()

            // My own explanation is here: https://github.com/lxc/lxd/issues/6856
            val outputStream = process.outputStream
            if (!inheritInput && outputStream != null) {
                inputStream?.transferTo(outputStream)
                outputStream.close()
            }

            // we need to gobble the streams to prevent that the internal pipes hit their respective buffer limits, which
            // would lock the sub-process execution (see see https://github.com/kscripting/kscript/issues/55
            // https://stackoverflow.com/questions/14165517/processbuilder-forwarding-stdout-and-stderr-of-started-processes-without-blocki
            val inputStreamReader = StreamGobbler(outputSanitizer, process.inputStream, outPrinter).start()
            val errorStreamReader = StreamGobbler(outputSanitizer, process.errorStream, errPrinter).start()

            val exitedNormally = process.waitFor(waitTimeMinutes.toLong(), TimeUnit.MINUTES)

            inputStreamReader.finish()
            errorStreamReader.finish()

            if (!exitedNormally) {
                throw CommandTimeoutException("Command has timed out after $waitTimeMinutes minutes.")
            }

            return process.exitValue()
        } catch (e: Exception) {
            throw IllegalStateException("Error executing command: '$command'", e)
        }
    }
}
