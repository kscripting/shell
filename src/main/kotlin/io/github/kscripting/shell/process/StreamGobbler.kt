package io.github.kscripting.shell.process

import java.io.InputStream
import java.io.PrintStream

class StreamGobbler(
    private val inputStream: InputStream,
    private val printStream: List<PrintStream>,
) {
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
        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } != -1) {
            val readContent = String(buffer, 0, length)

            printStream.forEach {
                it.print(readContent)
            }
        }
    }
}
