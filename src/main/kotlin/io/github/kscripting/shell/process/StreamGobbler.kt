package io.github.kscripting.shell.process

import java.io.InputStream

class StreamGobbler(private val inputStream: InputStream) {
    private val stringBuilder = StringBuilder()
    private lateinit var thread: Thread

    val output: String
        get() {
            if (!this::thread.isInitialized) {
                return ""
            }

            thread.join()
            return stringBuilder.toString()
        }

    fun start(): StreamGobbler {
        thread = Thread { readInputStreamSequentially() }
        thread.start()

        return this
    }

    private fun readInputStreamSequentially() {
        val buffer = ByteArray(1024)
        var length: Int

        while (inputStream.read(buffer).also { length = it } != -1) {
            val readContent = String(buffer, 0, length)
            stringBuilder.append(readContent)
        }
    }
}
