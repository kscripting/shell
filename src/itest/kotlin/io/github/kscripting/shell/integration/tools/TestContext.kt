package io.github.kscripting.shell.integration.tools

import io.github.kscripting.os.Os
import io.github.kscripting.os.model.*
import io.github.kscripting.shell.ShellExecutor
import io.github.kscripting.shell.util.Sanitizer

@Suppress("MemberVisibilityCanBePrivate")
object TestContext {
    val osType: OsType<out Os> = OsType.find(System.getProperty("osType")) ?: OsType.native

    val projectPath: OsPath = OsPath.of(OsType.native, System.getProperty("projectPath")).toHosted(osType).getOrThrow()
    val execPath: OsPath = projectPath.resolve("build/shell_test/bin").getOrThrow()
    val testPath: OsPath = projectPath.resolve("build/shell_test/tmp").getOrThrow()

    val pathEnvVariableName = if (osType.isWindowsLike()) "Path" else "PATH"
    val pathEnvVariableValue: String = System.getenv()[pathEnvVariableName]!!
    val pathEnvVariableSeparator: String = if (osType.isWindowsLike() || osType.isPosixHostedOnWindows()) ";" else ":"
    val pathEnvVariableCalculatedPath: String = "${execPath.toHosted(osType).path}$pathEnvVariableSeparator$pathEnvVariableValue"

    val nl: String = when {
        osType.isPosixHostedOnWindows() -> "\n"
        else -> System.getProperty("line.separator")
    }

    val defaultInputSanitizer = Sanitizer(listOf("[bs]" to "\\", "[nl]" to nl, "[tb]" to "\t"))
    val defaultOutputSanitizer = defaultInputSanitizer.swapped()

    init {
        println("osType         : $osType")
        println("nativeType     : ${OsType.native}")
        println("projectDir     : $projectPath")
        println("execDir        : ${execPath.toHosted(osType)}")
        println("Kotlin version : ${ShellExecutor.evalAndGobble("kotlin -version", osType).stdout}")
        println("Env path       : $pathEnvVariableCalculatedPath")

        execPath.createDirectories()
    }

    fun copyFile(source: String, target: OsPath) {
        val sourceFile = projectPath.resolve(source).toNativeFile().getOrThrow()
        val targetFile = target.resolve(sourceFile.name).toNativeFile().getOrThrow()

        sourceFile.copyTo(targetFile, overwrite = true)
        if (target == execPath) {
            targetFile.setExecutable(true)
        }
    }
}
