package io.github.kscripting.shell

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.OsType
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.model.ShellType
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
    private val DEFAULT_SHELL_MAPPER: Map<OsType, ShellType> = mapOf(
        OsType.WINDOWS to ShellType.CMD,
        OsType.LINUX to ShellType.BASH,
        OsType.FREEBSD to ShellType.BASH,
        OsType.MACOS to ShellType.BASH,
        OsType.CYGWIN to ShellType.BASH,
        OsType.MSYS to ShellType.BASH,
    )

    fun evalAndGobble(
        command: String,
        osType: OsType = OsType.native,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        inputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outputSanitizer: Sanitizer = inputSanitizer.swapped(),
        outPrinter: List<PrintStream> = emptyList(),
        errPrinter: List<PrintStream> = emptyList(),
        shellMapper: Map<OsType, ShellType> = DEFAULT_SHELL_MAPPER
    ): ProcessResult {
        val outStream = ByteArrayOutputStream(1024)
        val errStream = ByteArrayOutputStream(1024)

        var result: Int

        PrintStream(outStream, true, UTF_8).use { additionalOutPrinter ->
            PrintStream(errStream, true, UTF_8).use { additionalErrPrinter ->
                result = eval(
                    command,
                    osType,
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
        command: String,
        osType: OsType = OsType.native,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        inputSanitizer: Sanitizer = EMPTY_SANITIZER,
        outputSanitizer: Sanitizer = inputSanitizer.swapped(),
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS,
        shellMapper: Map<OsType, ShellType> = DEFAULT_SHELL_MAPPER
    ): Int {
        val sanitizedCommand = inputSanitizer.sanitize(command)

        val commandList = when (val shellType = shellMapper.getValue(osType)) {
            //NOTE: usually command is an argument to shell (bash/cmd), so it should stay not split by whitespace as
            //a single string, but when there is no shell, we have to split the command
            ShellType.NONE -> sanitizedCommand.split("\\s+")
            ShellType.CMD ->
                // For Windows: if the first character in args in `cmd /c <args>` is a quote, cmd will remove it as well
                // as the last quote character within args before processing the term, which removes our quotes.
                // Empty character before command preserves quotes correctly.
                shellType.executorCommand.toList() + " $sanitizedCommand"
            else ->
                shellType.executorCommand.toList() + sanitizedCommand
        }

        return ProcessRunner.runProcess(
            commandList,
            workingDirectory = workingDirectory,
            envAdjuster = envAdjuster,
            waitTimeMinutes = waitTimeMinutes,
            inheritInput = inheritInput,
            outputSanitizer = outputSanitizer,
            outPrinter = outPrinter,
            errPrinter = errPrinter
        )
    }
}
