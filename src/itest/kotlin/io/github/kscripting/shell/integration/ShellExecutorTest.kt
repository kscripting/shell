package io.github.kscripting.shell.integration

import io.github.kscripting.shell.integration.tools.TestAssertion.verify
import io.github.kscripting.shell.integration.tools.TestContext
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ShellExecutorTest : TestBase {
    @Test
    @Tag("posix")
    @Tag("windows")
    fun `Simple echo of parameters works`() {
        verify("echo_program test", 0, "test[nl]", "")
    }

    companion object {
        init {
            TestContext.copyToExecutablePath("src/echo_program.sh")
            TestContext.copyToExecutablePath("src/echo_program.bat")
        }
    }
}
