package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.*
import io.github.kscripting.os.instance.LinuxOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PosixOsPathTest {

    @BeforeAll
    fun beforeAll() {
        GlobalContext.registerOrReplace(OsType.LINUX, LinuxOs("userhome"))
    }

    @Test
    fun `Test Posix paths`() {
        assertThat(OsPath.of(OsType.LINUX, "/").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "/home/admin/.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "./home/admin/.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "file.txt").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath.of(OsType.LINUX, ".").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "../home/admin/.kscript").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "..").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath.of(OsType.LINUX, "..//home////admin/.kscript/").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath.of(OsType.LINUX, "..//home\\admin\\.kscript/").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Posix paths`() {
        assertThat(OsPath.of(OsType.LINUX, "/home/admin/.kscript/../../").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath.of(OsType.LINUX, "./././../../script").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath.of(OsType.LINUX, "/a/b/c/../d/script").getOrThrow()).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        OsPath.of(OsType.LINUX, "/.kscript/../../").let {
            assertThat(it.isFailure).isTrue()
            assertThat(it.exceptionOrNull()).isNotNull().isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Path after normalization goes beyond root element: '/'")
        }
    }

    @Test
    fun `Test invalid Posix paths`() {
        assertFailure { OsPath.of(OsType.LINUX, "/ad*asdf").getOrThrow() }
            .isInstanceOf(IllegalArgumentException::class.java).hasMessage("Invalid character '*' in path '/ad*asdf'")
    }

    @Test
    fun `Test Posix stringPath`() {
        assertThat(OsPath.of(OsType.LINUX, "/home/admin/.kscript").path.getOrThrow()).isEqualTo("/home/admin/.kscript")
        assertThat(OsPath.of(OsType.LINUX, "/a/b/c/../d/script").path.getOrThrow()).isEqualTo("/a/b/d/script")
        assertThat(OsPath.of(OsType.LINUX, "./././../../script").path.getOrThrow()).isEqualTo("../../script")
        assertThat(OsPath.of(OsType.LINUX, "script/file.txt").path.getOrThrow()).isEqualTo("script/file.txt")
    }

    @Test
    fun `Test Posix resolve`() {
        assertThat(
            OsPath.of(OsType.LINUX, "/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo("/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "/home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo("/home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "./home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo("home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "../home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "..").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo("../.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, ".").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path.getOrThrow()
        ).isEqualTo(".kscript")

        assertFailure {
            OsPath.of(OsType.LINUX, "./home/admin").resolve(OsPath.of(OsType.WINDOWS, ".\\run")).getOrThrow()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Paths from different OS's: 'LINUX' path can not be resolved with 'WINDOWS' path")

        assertFailure {
            OsPath.of(OsType.LINUX, "./home/admin").resolve(OsPath.of(OsType.LINUX, "/run")).getOrThrow()
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Can not resolve absolute or relative path 'home/admin' using absolute path '/run'")
    }
}
