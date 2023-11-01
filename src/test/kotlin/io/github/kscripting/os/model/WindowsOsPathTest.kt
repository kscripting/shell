package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.github.kscripting.os.instance.WindowsOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WindowsOsPathTest {
    @BeforeAll
    fun beforeAll() {
        GlobalContext.registerOrReplace(OsType.WINDOWS, WindowsOs("C:\\Users\\Admin\\.kscript"))
    }

    @Test
    fun `Test Windows paths`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, "file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath(OsType.WINDOWS, ".")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, ".\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "..\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "..")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\\\\\\\admin\\.kscript\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath(OsType.WINDOWS, "C:/home\\admin/.kscript////")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Windows paths`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript\\..\\..\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath(OsType.WINDOWS, ".\\.\\.\\..\\..\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath(OsType.WINDOWS, "C:\\a\\b\\c\\..\\d\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertFailure {
            OsPath(
                OsType.WINDOWS,
                "C:\\.kscript\\..\\..\\"
            )
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: 'C:\\'")
    }

    @Test
    fun `Test invalid Windows paths`() {
        assertFailure { OsPath(OsType.WINDOWS, "C:\\adas?df") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character '?' in path part 'adas?df'")

        assertFailure { OsPath(OsType.WINDOWS, "home:\\vagrant") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character ':' in path part 'home:'")
    }

    @Test
    fun `Test Windows stringPath`() {
        assertThat(
            OsPath(
                OsType.WINDOWS,
                "C:\\home\\admin\\.kscript"
            ).path
        ).isEqualTo("C:\\home\\admin\\.kscript")
        assertThat(
            OsPath(
                OsType.WINDOWS,
                "c:\\a\\b\\c\\..\\d\\script"
            ).path
        ).isEqualTo("c:\\a\\b\\d\\script")
        assertThat(OsPath(OsType.WINDOWS, ".\\.\\.\\..\\..\\script").path).isEqualTo("..\\..\\script")
        assertThat(OsPath(OsType.WINDOWS, "script\\file.txt").path).isEqualTo("script\\file.txt")
    }
}
