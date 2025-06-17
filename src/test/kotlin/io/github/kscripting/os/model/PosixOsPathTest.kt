package io.github.kscripting.os.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import io.github.kscripting.os.OsType
import io.github.kscripting.os.instance.LinuxVfs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PosixOsPathTest {
    private val linuxVfs = LinuxVfs("userhome")

    @Test
    fun `Test Posix paths`() {
        assertThat(linuxVfs.createOsPath("/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(linuxVfs.createOsPath("/home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(linuxVfs.createOsPath("./home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(linuxVfs.createOsPath("")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(linuxVfs.createOsPath("file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(linuxVfs.createOsPath(".")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(linuxVfs.createOsPath("../home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(linuxVfs.createOsPath("..")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(linuxVfs.createOsPath("..//home////admin/.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(linuxVfs.createOsPath("..//home\\admin\\.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Posix paths`() {
        assertThat(linuxVfs.createOsPath("/home/admin/.kscript/../../")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(linuxVfs.createOsPath("./././../../script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(linuxVfs.createOsPath("/a/b/c/../d/script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertFailure { linuxVfs.createOsPath("/.kscript/../../") }.isInstanceOf(IllegalArgumentException::class)
            .hasMessage("Path after normalization goes beyond root element: '/'")
    }

    @Test
    fun `Test invalid Posix paths`() {
        assertFailure {
            linuxVfs.createOsPath("/ad\u0000asdf")
        }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("Invalid character '\u0000' in path '/ad\u0000asdf'")
    }

    @Test
    fun `Test Posix stringPath`() {
        assertThat(
            linuxVfs.createOsPath("/home/admin/.kscript").path
        ).isEqualTo("/home/admin/.kscript")
        assertThat(linuxVfs.createOsPath("/a/b/c/../d/script").path).isEqualTo("/a/b/d/script")
        assertThat(linuxVfs.createOsPath("./././../../script").path).isEqualTo("../../script")
        assertThat(linuxVfs.createOsPath("script/file.txt").path).isEqualTo("script/file.txt")
    }

    @Test
    fun `Test Posix resolve`() {
        assertThat(
            linuxVfs.createOsPath("/").resolve(linuxVfs.createOsPath("./.kscript/"))
                .path
        ).isEqualTo("/.kscript")

        assertThat(
            linuxVfs.createOsPath("/home/admin/").resolve(linuxVfs.createOsPath("./.kscript/")).path
        ).isEqualTo("/home/admin/.kscript")

        assertThat(
            linuxVfs.createOsPath("./home/admin/").resolve(linuxVfs.createOsPath("./.kscript/")).path
        ).isEqualTo("home/admin/.kscript")

        assertThat(
            linuxVfs.createOsPath("../home/admin/").resolve(linuxVfs.createOsPath("./.kscript/")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            linuxVfs.createOsPath("..").resolve(linuxVfs.createOsPath("./.kscript/")).path
        ).isEqualTo("../.kscript")

        assertThat(
            linuxVfs.createOsPath(".").resolve(linuxVfs.createOsPath("./.kscript/")).path
        ).isEqualTo(".kscript")

        assertFailure {
            linuxVfs.createOsPath("./home/admin").resolve(linuxVfs.createOsPath("/run"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Can not resolve absolute or relative path 'home/admin' using absolute path '/run'")
    }
}
