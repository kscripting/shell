package io.github.kscripting.shell.process

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PrintStream


class StreamGobbler(
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

        while (reader.read(charArray).also { length = it } != -1) {
            val content = String(charArray, 0, length)

            printStream.forEach {
                it.print(content)
            }
        }
    }
}
