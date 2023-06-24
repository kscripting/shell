package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.ShellExecutor
import io.github.kscripting.shell.model.*
import io.github.kscripting.shell.util.Sanitizer

@Suppress("MemberVisibilityCanBePrivate")
object TestContext {
    val osType: OsType = OsType.find(System.getProperty("osType")) ?: OsType.native

    val projectPath: OsPath = OsPath.createOrThrow(OsType.native, System.getProperty("projectPath")).convert(osType)
    val execPath: OsPath = projectPath.resolve("build/shell_test/bin")
    val testPath: OsPath = projectPath.resolve("build/shell_test/tmp")

    val pathEnvVariableName = if (osType.isWindowsLike()) "Path" else "PATH"
    val pathEnvVariableValue: String = System.getenv()[pathEnvVariableName]!!
    val pathEnvVariableSeparator: String = if (osType.isWindowsLike() || osType.isPosixHostedOnWindows()) ";" else ":"
    val pathEnvVariableCalculatedPath: String = "${execPath.convert(osType)}$pathEnvVariableSeparator$pathEnvVariableValue"

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
        println("execDir        : ${execPath.convert(osType)}")
        println("Kotlin version : ${ShellExecutor.evalAndGobble("kotlin -version", osType).stdout}")
        println("Env path       : $pathEnvVariableCalculatedPath")

        execPath.createDirectories()
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
