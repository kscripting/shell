package io.github.kscripting.shell

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.OsType
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.process.ProcessRunner
import io.github.kscripting.shell.process.ProcessRunner.DEFAULT_ERR_PRINTERS
import io.github.kscripting.shell.process.ProcessRunner.DEFAULT_OUT_PRINTERS
import io.github.kscripting.shell.util.Sanitizer
import io.github.kscripting.shell.util.Sanitizer.Companion.EMPTY_SANITIZER
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

object ShellExecutor {
    private val UTF_8 = StandardCharsets.UTF_8.name()

    fun evalAndGobble(
        osType: OsType,
        command: String,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        inputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outputSanitizer: Sanitizer = inputSanitizer.swapped(),
        outPrinter: List<PrintStream> = emptyList(),
        errPrinter: List<PrintStream> = emptyList()
    ): ProcessResult {
        val outStream = ByteArrayOutputStream(1024)
        val errStream = ByteArrayOutputStream(1024)

        var result: Int

        PrintStream(outStream, true, UTF_8).use { additionalOutPrinter ->
            PrintStream(errStream, true, UTF_8).use { additionalErrPrinter ->
                result = eval(
                    osType,
                    command,
                    workingDirectory,
                    envAdjuster,
                    waitTimeMinutes,
                    inheritInput,
                    inputSanitizer,
                    outputSanitizer,
                    outPrinter + additionalOutPrinter,
                    errPrinter + additionalErrPrinter
                )
            }
        }

        return ProcessResult(result, outStream.toString(UTF_8), errStream.toString(UTF_8))
    }

    fun eval(
        osType: OsType,
        command: String,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        inputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outputSanitizer: Sanitizer = inputSanitizer.swapped(),
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS
    ): Int {
        //NOTE: command is an argument to shell (bash/cmd), so it should stay not split by whitespace as a single string

        val sanitizedCommand = inputSanitizer.sanitize(command)

        val commandList = when (osType) {
            // For Windows: if the first character in args in `cmd /c <args>` is a quote, cmd will remove it as well
            // as the last quote character within args before processing the term, which removes our quotes.
            // Empty character before command preserves quotes correctly.
            OsType.WINDOWS -> listOf("cmd", "/c", " $sanitizedCommand")
            else -> listOf("bash", "-c", sanitizedCommand)
        }

        return ProcessRunner.runProcess(
            commandList,
            workingDirectory = workingDirectory,
            envAdjuster = envAdjuster,
            waitTimeMinutes = waitTimeMinutes,
            inheritInput = inheritInput,
            inputSanitizer = inputSanitizer,
            outputSanitizer = outputSanitizer,
            outPrinter = outPrinter,
            errPrinter = errPrinter
        )
    }
}
