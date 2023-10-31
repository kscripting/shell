package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.*
import io.github.kscripting.os.instance.CygwinOs
import io.github.kscripting.os.instance.MsysOs
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
        assertThat(OsPath(OsType.WINDOWS, "C:\\").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, "file.txt").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath(OsType.WINDOWS, ".").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(OsType.WINDOWS, ".\\home\\admin\\.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "..\\home\\admin\\.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath(OsType.WINDOWS, "..").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\\\\\\\admin\\.kscript\\").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath(OsType.WINDOWS, "C:/home\\admin/.kscript////").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Windows paths`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript\\..\\..\\").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath(OsType.WINDOWS, ".\\.\\.\\..\\..\\script").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath(OsType.WINDOWS, "C:\\a\\b\\c\\..\\d\\script").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        OsPath(OsType.WINDOWS, "C:\\.kscript\\..\\..\\").let {
            assertThat(it.isFailure).isTrue()
            assertThat(it.exceptionOrNull()).isNotNull().isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Path after normalization goes beyond root element: 'C:\\'")
        }
    }

    @Test
    fun `Test invalid Windows paths`() {
        assertFailure { OsPath(OsType.WINDOWS, "C:\\adas?df").getOrThrow() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character '?' in path 'C:\\adas?df'")

        assertFailure { OsPath(OsType.WINDOWS, "home:\\vagrant").getOrThrow() }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character ':' in path 'home:\\vagrant'")
    }

    @Test
    fun `Test Windows stringPath`() {
        assertThat(OsPath(OsType.WINDOWS, "C:\\home\\admin\\.kscript").path.getOrThrow()).isEqualTo("C:\\home\\admin\\.kscript")
        assertThat(OsPath(OsType.WINDOWS, "c:\\a\\b\\c\\..\\d\\script").path.getOrThrow()).isEqualTo("c:\\a\\b\\d\\script")
        assertThat(OsPath(OsType.WINDOWS, ".\\.\\.\\..\\..\\script").path.getOrThrow()).isEqualTo("..\\..\\script")
        assertThat(OsPath(OsType.WINDOWS, "script\\file.txt").path.getOrThrow()).isEqualTo("script\\file.txt")
    }
}
