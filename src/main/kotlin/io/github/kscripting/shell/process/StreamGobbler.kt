package io.github.kscripting.shell.process

import io.github.kscripting.shell.util.Sanitizer
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream


class StreamGobbler(
    private val outputSanitizer: Sanitizer,
    inputStream: InputStream,
    private val printStream: List<PrintStream>,
) {
    private val reader: BufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF8"))
    private var thread: Thread? = null

    fun start(): StreamGobbler {
        thread?.join()
        thread = Thread { readInputStreamSequentially() }
        thread?.start()

        return this
    }

    fun finish() {
        thread?.join()
        thread = null
    }

    private fun readInputStreamSequentially() {
        val charArray = CharArray(1024)
        var length: Int
        var rest = ""

        while (reader.read(charArray).also { length = it } != -1) {
            var content = rest + String(charArray, 0, length)
            rest = outputSanitizer.calculatePotentialMatch(content)
            content = outputSanitizer.sanitize(content.dropLast(rest.length))
            outputToPrintStreams(content)
        }

        if (rest.isNotEmpty()) {
            outputToPrintStreams(outputSanitizer.sanitize(rest))
        }
    }

    private fun outputToPrintStreams(content: String) {
        printStream.forEach {
            it.print(content)
        }
    }
}
