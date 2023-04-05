package io.github.kscripting.shell

import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.OsType
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.process.ProcessRunner
import io.github.kscripting.shell.process.ProcessRunner.DEFAULT_ERR_PRINTERS
import io.github.kscripting.shell.process.ProcessRunner.DEFAULT_OUT_PRINTERS
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets

object ShellExecutor {

    fun evalAndGobble(
        osType: OsType,
        command: String,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        outPrinter: List<PrintStream> = emptyList(),
        errPrinter: List<PrintStream> = emptyList()
    ): ProcessResult {
        val outStream = ByteArrayOutputStream(1024)
        val errStream = ByteArrayOutputStream(1024)

        val utf8 = StandardCharsets.UTF_8.name()

        var result: Int

        PrintStream(outStream, true, utf8).use { additionalOutPrinter ->
            PrintStream(errStream, true, utf8).use { additionalErrPrinter ->
                result = eval(
                    osType,
                    command,
                    workingDirectory,
                    envAdjuster,
                    waitTimeMinutes,
                    inheritInput,
                    outPrinter + additionalOutPrinter,
                    errPrinter + additionalErrPrinter
                )
            }
        }

        return ProcessResult(result, outStream.toString(utf8), errStream.toString(utf8))
    }

    fun eval(
        osType: OsType,
        command: String,
        workingDirectory: OsPath? = null,
        envAdjuster: EnvAdjuster = {},
        waitTimeMinutes: Int = 10,
        inheritInput: Boolean = false,
        outPrinter: List<PrintStream> = DEFAULT_OUT_PRINTERS,
        errPrinter: List<PrintStream> = DEFAULT_ERR_PRINTERS
    ): Int {
        //NOTE: cmd is an argument to shell (bash/cmd), so it should stay not split by whitespace as a single string
        if (osType == OsType.WINDOWS) {
            // if the first character in args in `cmd /c <args>` is a quote, cmd will remove it as well as the
            // last quote character within args before processing the term, which removes our quotes.
            return ProcessRunner.runProcess(
                "cmd",
                "/c",
                " $command",
                workingDirectory = workingDirectory,
                envAdjuster = envAdjuster,
                waitTimeMinutes = waitTimeMinutes,
                inheritInput = inheritInput,
                outPrinter = outPrinter,
                errPrinter = errPrinter
            )
        }

        return ProcessRunner.runProcess(
            "bash",
            "-c",
            command,
            workingDirectory = workingDirectory,
            envAdjuster = envAdjuster,
            waitTimeMinutes = waitTimeMinutes,
            inheritInput = inheritInput,
            outPrinter = outPrinter,
            errPrinter = errPrinter
        )
    }
}
