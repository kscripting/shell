package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.ShellExecutor
import io.github.kscripting.shell.model.*
import io.github.kscripting.shell.process.EnvAdjuster
import io.github.kscripting.shell.util.Sanitizer

object TestContext {
    private val osType: OsType = OsType.find(System.getProperty("osType")) ?: OsType.native
    private val nativeType = if (osType.isPosixHostedOnWindows()) OsType.WINDOWS else osType

    private val projectPath: OsPath = OsPath.createOrThrow(nativeType, System.getProperty("projectPath"))
    private val execPath: OsPath = projectPath.resolve("build/shell/bin")
    private val pathEnvName = if (osType.isWindowsLike()) "Path" else "PATH"
    private val systemPath: String = System.getenv()[pathEnvName]!!

    private val pathSeparator: String = if (osType.isWindowsLike() || osType.isPosixHostedOnWindows()) ";" else ":"
    private val envPath: String = "${execPath.convert(osType)}$pathSeparator$systemPath"

    val nl: String = System.getProperty("line.separator")

    private val inputSanitizer = Sanitizer(
        listOf("[bs]" to "\\", "[nl]" to nl, "[tb]" to "\t")
    )

    val projectDir: String = projectPath.convert(osType).stringPath()

    init {
        println("osType         : $osType")
        println("nativeType     : $nativeType")
        println("projectDir     : $projectDir")
        println("execDir        : ${execPath.convert(osType)}")
        println("Kotlin version : ${ShellExecutor.evalAndGobble("kotlin -version", osType, null).stdout}")
        println("Env path       : $envPath")

        execPath.createDirectories()
    }

    fun path(path: String): OsPath {
        return OsPath.createOrThrow(osType, path)
    }

    fun runProcess(
        command: String,
        envAdjuster: EnvAdjuster,
        inputSanitizer: Sanitizer? = null,
        outputSanitizer: Sanitizer? = null
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
            ::internalEnvAdjuster,
            inputSanitizer = inputSanitizer ?: this.inputSanitizer,
            outputSanitizer = outputSanitizer ?: this.inputSanitizer.swapped()
        )
    }

    fun copyToExecutablePath(source: String) {
        val sourceFile = projectPath.resolve(source).toNativeFile()
        val targetFile = execPath.resolve(sourceFile.name).toNativeFile()

        sourceFile.copyTo(targetFile, overwrite = true)
        targetFile.setExecutable(true)
    }

    fun execPath(): OsPath = execPath
}
