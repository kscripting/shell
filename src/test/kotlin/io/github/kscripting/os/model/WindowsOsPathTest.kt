package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.github.kscripting.os.OsType
import io.github.kscripting.os.instance.WindowsVfs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WindowsOsPathTest {
    private val windowsVfs = WindowsVfs("C:\\Users\\Admin\\.kscript")

    @Test
    fun `Test Windows paths`() {
        assertThat(windowsVfs.createOsPath("C:\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(windowsVfs.createOsPath("C:\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(windowsVfs.createOsPath("")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(windowsVfs.createOsPath("file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(windowsVfs.createOsPath(".")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(windowsVfs.createOsPath(".\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(windowsVfs.createOsPath("..\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(windowsVfs.createOsPath("..")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(windowsVfs.createOsPath("C:\\home\\\\\\\\admin\\.kscript\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(windowsVfs.createOsPath("C:/home\\admin/.kscript////")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Windows paths`() {
        assertThat(windowsVfs.createOsPath("C:\\home\\admin\\.kscript\\..\\..\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(windowsVfs.createOsPath(".\\.\\.\\..\\..\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(windowsVfs.createOsPath("C:\\a\\b\\c\\..\\d\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertFailure {
            windowsVfs.createOsPath("C:\\.kscript\\..\\..\\")
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: 'C:\\'")
    }

    @Test
    fun `Test invalid Windows paths`() {
        assertFailure { windowsVfs.createOsPath("C:\\adas?df") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character '?' in path part 'adas?df'")

        assertFailure { windowsVfs.createOsPath("home:\\vagrant") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character ':' in path part 'home:'")
    }

    @Test
    fun `Test Windows stringPath`() {
        assertThat(windowsVfs.createOsPath("C:\\home\\admin\\.kscript").path).isEqualTo("C:\\home\\admin\\.kscript")
        assertThat(windowsVfs.createOsPath("c:\\a\\b\\c\\..\\d\\script").path).isEqualTo("c:\\a\\b\\d\\script")
        assertThat(windowsVfs.createOsPath(".\\.\\.\\..\\..\\script").path).isEqualTo("..\\..\\script")
        assertThat(windowsVfs.createOsPath("script\\file.txt").path).isEqualTo("script\\file.txt")
    }
}
