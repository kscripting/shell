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
        GlobalContext.registerOrReplace(
            GlobalOsType.CYGWIN,
            CygwinOs(GlobalOsType.CYGWIN, GlobalOsType.WINDOWS, "/home/admin", "C:\\Programs\\Cygwin\\")
        )
        GlobalContext.registerOrReplace(
            GlobalOsType.MSYS,
            MsysOs(GlobalOsType.MSYS, GlobalOsType.WINDOWS, "/home/admin", "C:\\Programs\\Msys\\")
        )
        GlobalContext.registerOrReplace(
            GlobalOsType.WINDOWS,
            WindowsOs(GlobalOsType.WINDOWS, "C:\\Users\\Admin\\.kscript")
        )
    }

    @Test
    fun `Test Cygwin to Windows`() {
        assertThat(
            OsPath(
                GlobalOsType.CYGWIN, "/cygdrive/c/home/admin/.kscript"
            ).toNative().path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath(
                GlobalOsType.CYGWIN, "~/.kscript"
            ).toNative().path
        ).isEqualTo("C:\\Programs\\Cygwin\\home\\admin\\.kscript")

        assertThat(
            OsPath(
                GlobalOsType.CYGWIN, "/usr/local/bin/sdk"
            ).toNative().path
        ).isEqualTo("C:\\Programs\\Cygwin\\usr\\local\\bin\\sdk")

        assertThat(
            OsPath(GlobalOsType.CYGWIN, "../home/admin/.kscript").toNative().path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to Cygwin`() {
        assertThat(
            OsPath(GlobalOsType.WINDOWS, "C:\\home\\admin\\.kscript").toHosted(GlobalOsType.CYGWIN).path
        ).isEqualTo("/cygdrive/c/home/admin/.kscript")

        assertThat(
            OsPath(GlobalOsType.WINDOWS, "..\\home\\admin\\.kscript").toHosted(GlobalOsType.CYGWIN).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath(
                GlobalOsType.WINDOWS, "C:\\Programs\\Cygwin\\home\\admin\\.kscript"
            ).toHosted(GlobalOsType.CYGWIN).path
        ).isEqualTo("~/.kscript")

        assertThat(
            OsPath(
                GlobalOsType.WINDOWS, "C:\\Programs\\Cygwin\\usr\\local\\sdk"
            ).toHosted(GlobalOsType.CYGWIN).path
        ).isEqualTo("/usr/local/sdk")
    }

    @Test
    fun `Test MSys to Windows`() {
        assertThat(
            OsPath(GlobalOsType.MSYS, "/c/home/admin/.kscript").toNative().path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath(GlobalOsType.MSYS, "~/.kscript").toNative().path
        ).isEqualTo("C:\\Programs\\Msys\\home\\admin\\.kscript")

        assertThat(
            OsPath(GlobalOsType.MSYS, "/usr/local/bin/sdk").toNative().path
        ).isEqualTo("C:\\Programs\\Msys\\usr\\local\\bin\\sdk")

        assertThat(
            OsPath(GlobalOsType.MSYS, "../home/admin/.kscript").toNative().path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to MSys`() {
        assertThat(
            OsPath(
                GlobalOsType.WINDOWS, "C:\\home\\admin\\.kscript"
            ).toHosted(GlobalOsType.MSYS).path
        ).isEqualTo("/c/home/admin/.kscript")

        assertThat(
            OsPath(
                GlobalOsType.WINDOWS, "..\\home\\admin\\.kscript"
            ).toHosted(GlobalOsType.MSYS).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath(
                GlobalOsType.WINDOWS, "C:\\Programs\\Msys\\home\\admin\\.kscript"
            ).toHosted(GlobalOsType.MSYS).path
        ).isEqualTo("~/.kscript")

        assertThat(
            OsPath(GlobalOsType.WINDOWS, "C:\\Programs\\Msys\\usr\\local\\sdk").toHosted(GlobalOsType.MSYS).path
        ).isEqualTo("/usr/local/sdk")
    }
}
