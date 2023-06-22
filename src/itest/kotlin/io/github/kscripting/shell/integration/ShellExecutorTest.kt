package io.github.kscripting.shell.integration

import io.github.kscripting.shell.integration.tools.TestAssertion.verify
import io.github.kscripting.shell.integration.tools.TestContext
import io.github.kscripting.shell.integration.tools.TestContext.execPath
import io.github.kscripting.shell.model.readText
import io.github.kscripting.shell.util.Sanitizer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class ShellExecutorTest : TestBase {
    @Test
    @Tag("posix")
    @Tag("windows")
    fun `Simple echo of parameters works`() {
        verify("doEcho test", 0, "test[nl]", "")
    }

    @Test
    @Tag("posix")
    @Tag("windows")
    fun `Unicode characters output works`() {
        val path = execPath().resolve("unicodeOutput.txt")
        println(path)
        verify(
            "doEcho -f $path",
            0,
            path.readText(),
            "",
            inputSanitizer = Sanitizer.EMPTY_SANITIZER,
            outputSanitizer = Sanitizer.EMPTY_SANITIZER
        )
    }

    @Test
    @Tag("posix")
    fun `Call command utilizing input stream`() {
        verify("read INPUT; echo \$INPUT", 0, "Input to READ[nl]", "", inputStream = "Input to READ".byteInputStream())
    }

    companion object {
        init {
            TestContext.copyToExecutablePath("src/doEcho")
            TestContext.copyToExecutablePath("src/doEcho.bat")
            TestContext.copyToExecutablePath("src/unicodeOutput.txt")
            Thread.sleep(1000) //It takes time to copy and set executable bit [tests fail without this]
        }
    }
}
