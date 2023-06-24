package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.ShellExecutor
import io.github.kscripting.shell.model.*
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.util.Sanitizer
import java.io.InputStream

@Suppress("MemberVisibilityCanBePrivate")
object TestContext {
    val osType: OsType = OsType.find(System.getProperty("osType")) ?: OsType.native
    val nativeType = if (osType.isPosixHostedOnWindows()) OsType.WINDOWS else osType

    val projectPath: OsPath = OsPath.createOrThrow(nativeType, System.getProperty("projectPath")).convert(osType)
    val execPath: OsPath = projectPath.resolve("build/shell_test/bin")
    val testPath: OsPath = projectPath.resolve("build/shell_test/tmp")

    private val pathEnvName = if (osType.isWindowsLike()) "Path" else "PATH"
    private val systemPath: String = System.getenv()[pathEnvName]!!

    private val pathSeparator: String = if (osType.isWindowsLike() || osType.isPosixHostedOnWindows()) ";" else ":"
    private val envPath: String = "${execPath.convert(osType)}$pathSeparator$systemPath"

    val nl: String = System.getProperty("line.separator")

    val defaultInputSanitizer = Sanitizer(listOf("[bs]" to "\\", "[nl]" to nl, "[tb]" to "\t"))
    val defaultOutputSanitizer = defaultInputSanitizer.swapped()

    init {
        println("osType         : $osType")
        println("nativeType     : $nativeType")
        println("projectDir     : $projectPath")
        println("execDir        : ${execPath.convert(osType)}")
        println("Kotlin version : ${ShellExecutor.evalAndGobble("kotlin -version", osType, null).stdout}")
        println("Env path       : $envPath")

        execPath.createDirectories()
    }

    fun runProcess(
        command: String,
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null,
        inputStream: InputStream? = null,
        envAdjuster: EnvAdjuster
    ): ProcessResult {
        //In MSYS all quotes should be single quotes, otherwise content is interpreted e.g. backslashes.
        //(MSYS bash interpreter is also replacing double quotes into the single quotes: see: bash -xc 'kscript "println(1+1)"')
        val newCommand = when {
            osType.isPosixHostedOnWindows() -> command.replace('"', '\'')
            else -> command
        }

        fun internalEnvAdjuster(map: MutableMap<String, String>) {
            map[pathEnvName] = envPath
            envAdjuster(map)
        }

        return ShellExecutor.evalAndGobble(
            newCommand,
            osType,
            null,
            inputSanitizer = inputSanitizer ?: this.defaultInputSanitizer,
            outputSanitizer = outputSanitizer ?: this.defaultOutputSanitizer,
            inputStream = inputStream,
            envAdjuster = ::internalEnvAdjuster
        )
    }

    fun copyFile(source: String, target: OsPath) {
        val sourceFile = projectPath.resolve(source).toNativeFile()
        val targetFile = target.resolve(sourceFile.name).toNativeFile()

        sourceFile.copyTo(targetFile, overwrite = true)
        if (target == execPath) {
            targetFile.setExecutable(true)
        }
    }
}
