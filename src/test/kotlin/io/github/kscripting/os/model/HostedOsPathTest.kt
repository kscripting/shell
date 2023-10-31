package io.github.kscripting.os.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.kscripting.os.instance.CygwinOs
import io.github.kscripting.os.instance.MsysOs
import io.github.kscripting.os.instance.WindowsOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HostedOsPathTest {

    @BeforeAll
    fun beforeAll() {
        //Installation cygwin/msys base path: cygpath -w /, cygpath ~
        GlobalContext.registerOrReplace(OsType.CYGWIN, CygwinOs("/home/admin", "C:\\Programs\\Cygwin\\"))
        GlobalContext.registerOrReplace(OsType.MSYS, MsysOs("/home/admin", "C:\\Programs\\Msys\\"))
        GlobalContext.registerOrReplace(OsType.WINDOWS, WindowsOs("C:\\Users\\Admin\\.kscript"))
    }

    @Test
    fun `Test Cygwin to Windows`() {
        assertThat(OsPath(OsType.CYGWIN, "/cygdrive/c/home/admin/.kscript").toNative().path.getOrThrow())
            .isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(OsPath(OsType.CYGWIN, "~/.kscript").toNative().path.getOrThrow())
            .isEqualTo("C:\\Programs\\Cygwin\\home\\admin\\.kscript")

        assertThat(OsPath(OsType.CYGWIN, "/usr/local/bin/sdk").toNative().path.getOrThrow())
            .isEqualTo("C:\\Programs\\Cygwin\\usr\\local\\bin\\sdk")

        assertThat(OsPath(OsType.CYGWIN, "../home/admin/.kscript").toNative().path.getOrThrow())
            .isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to Cygwin`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript").toHosted(OsType.CYGWIN).path.getOrThrow())
            .isEqualTo("/cygdrive/c/home/admin/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "..\\home\\admin\\.kscript").toHosted(OsType.CYGWIN).path.getOrThrow())
            .isEqualTo("../home/admin/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "C:\\Programs\\Cygwin\\home\\admin\\.kscript").toHosted(OsType.CYGWIN).path.getOrThrow())
            .isEqualTo("~/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "C:\\Programs\\Cygwin\\usr\\local\\sdk").toHosted(OsType.CYGWIN).path.getOrThrow())
            .isEqualTo("/usr/local/sdk")
    }

    @Test
    fun `Test MSys to Windows`() {
        assertThat(OsPath(OsType.MSYS, "/c/home/admin/.kscript").toNative().path.getOrThrow())
            .isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(OsPath(OsType.MSYS, "~/.kscript").toNative().path.getOrThrow())
            .isEqualTo("C:\\Programs\\Msys\\home\\admin\\.kscript")

        assertThat(OsPath(OsType.MSYS, "/usr/local/bin/sdk").toNative().path.getOrThrow())
            .isEqualTo("C:\\Programs\\Msys\\usr\\local\\bin\\sdk")

        assertThat(OsPath(OsType.MSYS, "../home/admin/.kscript").toNative().path.getOrThrow())
            .isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to MSys`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript").toHosted(OsType.MSYS).path.getOrThrow())
            .isEqualTo("/c/home/admin/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "..\\home\\admin\\.kscript").toHosted(OsType.MSYS).path.getOrThrow())
            .isEqualTo("../home/admin/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "C:\\Programs\\Msys\\home\\admin\\.kscript").toHosted(OsType.MSYS).path.getOrThrow())
            .isEqualTo("~/.kscript")

        assertThat(OsPath(OsType.WINDOWS, "C:\\Programs\\Msys\\usr\\local\\sdk").toHosted(OsType.MSYS).path.getOrThrow())
            .isEqualTo("/usr/local/sdk")
    }
}
