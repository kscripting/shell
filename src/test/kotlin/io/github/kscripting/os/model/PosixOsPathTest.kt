package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.github.kscripting.os.instance.LinuxOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PosixOsPathTest {

    @BeforeAll
    fun beforeAll() {
        GlobalContext.registerOrReplace(GlobalOsType.LINUX, LinuxOs(GlobalOsType.LINUX, "userhome"))
    }

    @Test
    fun `Test Posix paths`() {
        assertThat(OsPath(GlobalOsType.LINUX, "/")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(GlobalOsType.LINUX, "/home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, "./home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, "")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(GlobalOsType.LINUX, "file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, ".")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath(GlobalOsType.LINUX, "../home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, "..")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath(GlobalOsType.LINUX, "..//home////admin/.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath(GlobalOsType.LINUX, "..//home\\admin\\.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Posix paths`() {
        assertThat(OsPath(GlobalOsType.LINUX, "/home/admin/.kscript/../../")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, "./././../../script")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath(GlobalOsType.LINUX, "/a/b/c/../d/script")).let {
            it.prop(OsPath::osType).isEqualTo(GlobalOsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertFailure { OsPath(GlobalOsType.LINUX, "/.kscript/../../") }.isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Path after normalization goes beyond root element: '/'")
    }

    @Test
    fun `Test invalid Posix paths`() {
        assertFailure {
            OsPath(
                GlobalOsType.LINUX,
                "/ad*asdf"
            )
        }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("Invalid character '*' in path part 'ad*asdf'")
    }

    @Test
    fun `Test Posix stringPath`() {
        assertThat(
            OsPath(GlobalOsType.LINUX, "/home/admin/.kscript").path
        ).isEqualTo("/home/admin/.kscript")
        assertThat(OsPath(GlobalOsType.LINUX, "/a/b/c/../d/script").path).isEqualTo("/a/b/d/script")
        assertThat(OsPath(GlobalOsType.LINUX, "./././../../script").path).isEqualTo("../../script")
        assertThat(OsPath(GlobalOsType.LINUX, "script/file.txt").path).isEqualTo("script/file.txt")
    }

    @Test
    fun `Test Posix resolve`() {
        assertThat(
            OsPath(GlobalOsType.LINUX, "/").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/"))
                .path
        ).isEqualTo("/.kscript")

        assertThat(
            OsPath(GlobalOsType.LINUX, "/home/admin/").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/")).path
        ).isEqualTo("/home/admin/.kscript")

        assertThat(
            OsPath(GlobalOsType.LINUX, "./home/admin/").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/")).path
        ).isEqualTo("home/admin/.kscript")

        assertThat(
            OsPath(GlobalOsType.LINUX, "../home/admin/").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath(GlobalOsType.LINUX, "..").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/")).path
        ).isEqualTo("../.kscript")

        assertThat(
            OsPath(GlobalOsType.LINUX, ".").resolve(OsPath(GlobalOsType.LINUX, "./.kscript/")).path
        ).isEqualTo(".kscript")

        assertFailure {
            OsPath(GlobalOsType.LINUX, "./home/admin").resolve(OsPath(GlobalOsType.WINDOWS, ".\\run"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Paths from different OS's: 'LINUX' path can not be resolved with 'WINDOWS' path")

        assertFailure {
            OsPath(GlobalOsType.LINUX, "./home/admin").resolve(OsPath(GlobalOsType.LINUX, "/run"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Can not resolve absolute or relative path 'home/admin' using absolute path '/run'")
    }
}
