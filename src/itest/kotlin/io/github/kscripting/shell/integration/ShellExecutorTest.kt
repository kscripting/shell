package io.github.kscripting.shell.integration

import io.github.kscripting.os.model.readText
import io.github.kscripting.os.model.resolve
import io.github.kscripting.shell.integration.tools.ShellTestBase
import io.github.kscripting.shell.integration.tools.TestContext
import io.github.kscripting.shell.integration.tools.TestContext.execPath
import io.github.kscripting.shell.integration.tools.TestContext.testPath
import io.github.kscripting.shell.util.Sanitizer
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ShellExecutorTest : ShellTestBase {
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
        val path = testPath.resolve("unicodeOutput.txt")
        println(path)
        verify(
            "doEcho -f $path",
            0,
            path.readText().getOrThrow(),
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

//    @Test
//    @Tag("windows")
//    fun `Call command utilizing input stream (windows)`() {
//        verify("set /p INPUT=\"\" && echo %INPUT%", 0, "Input to READ[nl]", "", inputStream = "Input to READ".byteInputStream())
//    }

    companion object {
        init {
            TestContext.copyFile("src/doEcho", execPath)
            TestContext.copyFile("src/doEcho.bat", execPath)
            TestContext.copyFile("src/unicodeOutput.txt", testPath)
            Thread.sleep(1000) //It takes time to copy and set executable bit [tests fail without this]
        }
    }
}
