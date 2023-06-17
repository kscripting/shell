package io.github.kscripting.shell.integration

import io.github.kscripting.shell.integration.tools.TestAssertion.verify
import io.github.kscripting.shell.integration.tools.TestContext
import io.github.kscripting.shell.integration.tools.TestContext.execPath
import io.github.kscripting.shell.integration.tools.TestContext.resolvePath
import io.github.kscripting.shell.model.readText
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

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
        verify("doEcho -f $path", 0, path.readText(), "")
    }

    companion object {
        init {
            TestContext.copyToExecutablePath("src/doEcho.sh")
            TestContext.copyToExecutablePath("src/doEcho.bat")
            TestContext.copyToExecutablePath("src/unicodeOutput.txt")
        }
    }
}
