package io.github.kscripting.os.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.kscripting.os.instance.CygwinVfs
import io.github.kscripting.os.instance.MsysVfs
import io.github.kscripting.os.instance.WindowsVfs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HostedOsPathTest {
    private val windowsVfs = WindowsVfs("C:\\Users\\Admin\\.kscript")
    private val cygwinVfs = CygwinVfs(windowsVfs.createOsPath("C:\\Programs\\Cygwin\\"), "/home/admin")
    private val msysVfs = MsysVfs(windowsVfs.createOsPath("C:\\Programs\\Msys\\"), "/home/admin")

    @Test
    fun `Test Cygwin to Windows`() {
        assertThat(
            cygwinVfs.createOsPath("/cygdrive/c/home/admin/.kscript").toNative().path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            cygwinVfs.createOsPath("~/.kscript").toNative().path
        ).isEqualTo("C:\\Programs\\Cygwin\\home\\admin\\.kscript")

        assertThat(
            cygwinVfs.createOsPath("/usr/local/bin/sdk").toNative().path
        ).isEqualTo("C:\\Programs\\Cygwin\\usr\\local\\bin\\sdk")

        assertThat(
            cygwinVfs.createOsPath("../home/admin/.kscript").toNative().path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to Cygwin`() {
        assertThat(
            cygwinVfs.toHosted(windowsVfs.createOsPath("C:\\home\\admin\\.kscript")).path
        ).isEqualTo("/cygdrive/c/home/admin/.kscript")

        assertThat(
            cygwinVfs.toHosted(windowsVfs.createOsPath("..\\home\\admin\\.kscript")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            cygwinVfs.toHosted(windowsVfs.createOsPath("C:\\Programs\\Cygwin\\home\\admin\\.kscript")).path
        ).isEqualTo("~/.kscript")

        assertThat(
            cygwinVfs.toHosted(windowsVfs.createOsPath("C:\\Programs\\Cygwin\\usr\\local\\sdk")).path
        ).isEqualTo("/usr/local/sdk")
    }

    @Test
    fun `Test MSys to Windows`() {
        assertThat(
            msysVfs.createOsPath("/c/home/admin/.kscript").toNative().path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            msysVfs.createOsPath("~/.kscript").toNative().path
        ).isEqualTo("C:\\Programs\\Msys\\home\\admin\\.kscript")

        assertThat(
            msysVfs.createOsPath("/usr/local/bin/sdk").toNative().path
        ).isEqualTo("C:\\Programs\\Msys\\usr\\local\\bin\\sdk")

        assertThat(
            msysVfs.createOsPath("../home/admin/.kscript").toNative().path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to MSys`() {
        assertThat(
            msysVfs.toHosted(windowsVfs.createOsPath("C:\\home\\admin\\.kscript")).path
        ).isEqualTo("/c/home/admin/.kscript")

        assertThat(
            msysVfs.toHosted(windowsVfs.createOsPath("..\\home\\admin\\.kscript")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            msysVfs.toHosted(windowsVfs.createOsPath("C:\\Programs\\Msys\\home\\admin\\.kscript")).path
        ).isEqualTo("~/.kscript")

        assertThat(
            msysVfs.toHosted(windowsVfs.createOsPath("C:\\Programs\\Msys\\usr\\local\\sdk")).path
        ).isEqualTo("/usr/local/sdk")
    }
}
