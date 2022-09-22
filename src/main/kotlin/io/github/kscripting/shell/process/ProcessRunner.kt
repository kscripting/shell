package io.github.kscripting.shell.process

import io.github.kscripting.shell.model.OsPath
import io.github.kscripting.shell.model.ProcessResult
import io.github.kscripting.shell.model.toNativeFile
import java.util.concurrent.TimeUnit

typealias EnvironmentAdjuster = (MutableMap<String, String>) -> Unit

object ProcessRunner {
    fun runProcess(vararg command: String, workingDirectory: OsPath? = null, environmentAdjuster: EnvironmentAdjuster = {}): ProcessResult {
        return runProcess(command.asList(), workingDirectory, environmentAdjuster)
    }

    fun runProcess(
        command: List<String>,
        workingDirectory: OsPath? = null,
        environmentAdjuster: EnvironmentAdjuster = {}
    ): ProcessResult {
        try {
            // simplify with https://stackoverflow.com/questions/35421699/how-to-invoke-external-command-from-within-kotlin-code
            val process = ProcessBuilder(command).directory(workingDirectory?.toNativeFile()).apply {
                environmentAdjuster(environment())
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
            return ProcessResult(command.joinToString(" "), process.exitValue(), inputStreamReader.output, errorStreamReader.output)
        } catch (e: Exception) {
            throw IllegalStateException("Error executing command: '$command'", e)
        }
    }

    private fun prepareMinimalEnvironment(environment: MutableMap<String, String>, env: Map<String, String>) {
        // see https://youtrack.jetbrains.com/issue/KT-20785
        // on Windows also other env variables (like KOTLIN_OPTS) interfere with executed command, so they have to be cleaned

        //NOTE: It would be better to prepare minimal env only with environment variables that are required,
        //but it means that we should track, what are default env variables in different OSes

        //Env variables set by Unix scripts (from kscript and Kotlin)
        environment.remove("KOTLIN_RUNNER")

        //Env variables set by Windows scripts (from kscript and Kotlin)
        environment.remove("_KOTLIN_RUNNER")
        environment.remove("KOTLIN_OPTS")
        environment.remove("JAVA_OPTS")
        environment.remove("_version")
        environment.remove("_KOTLIN_HOME")
        environment.remove("_BIN_DIR")
        environment.remove("_KOTLIN_COMPILER")
        environment.remove("JAR_PATH")
        environment.remove("COMMAND")
        environment.remove("_java_major_version")
        environment.remove("ABS_KSCRIPT_PATH")

        environment.putAll(env)
    }
}
