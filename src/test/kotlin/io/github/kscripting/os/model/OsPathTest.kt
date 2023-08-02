package io.github.kscripting.os.model

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class OsPathTest {
    // ************************************************** LINUX PATHS **************************************************
    @Test
    fun `Test Linux paths`() {
        assertThat(OsPath.of(OsType.LINUX, "/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "/home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "./home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath.of(OsType.LINUX, ".")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.LINUX, "../home/admin/.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.LINUX, "..")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath.of(OsType.LINUX, "..//home////admin/.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath.of(OsType.LINUX, "..//home\\admin\\.kscript/")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Linux paths`() {
        assertThat(OsPath.of(OsType.LINUX, "/home/admin/.kscript/../../")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath.of(OsType.LINUX, "./././../../script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath.of(OsType.LINUX, "/a/b/c/../d/script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertThat { OsPath.of(OsType.LINUX, "/.kscript/../../") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: '/'")
    }

    @Test
    fun `Test invalid Linux paths`() {
        assertThat { OsPath.of(OsType.LINUX, "/ad*asdf") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java).hasMessage("Invalid character '*' in path '/ad*asdf'")
    }

    @Test
    fun `Test Linux stringPath`() {
        assertThat(
            OsPath.of(OsType.LINUX, "/home/admin/.kscript").path
        ).isEqualTo("/home/admin/.kscript")
        assertThat(OsPath.of(OsType.LINUX, "/a/b/c/../d/script").path).isEqualTo("/a/b/d/script")
        assertThat(OsPath.of(OsType.LINUX, "./././../../script").path).isEqualTo("../../script")
        assertThat(OsPath.of(OsType.LINUX, "script/file.txt").path).isEqualTo("script/file.txt")
    }

    @Test
    fun `Test Linux resolve`() {
        assertThat(
            OsPath.of(OsType.LINUX, "/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "/home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("/home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "./home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "../home/admin/").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, "..").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("../.kscript")

        assertThat(
            OsPath.of(OsType.LINUX, ".").resolve(OsPath.of(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo(".kscript")

        assertThat {
            OsPath.of(OsType.LINUX, "./home/admin").resolve(OsPath.of(OsType.WINDOWS, ".\\run"))
        }.isFailure().isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Paths from different OS's: 'LINUX' path can not be resolved with 'WINDOWS' path")

        assertThat {
            OsPath.of(OsType.LINUX, "./home/admin").resolve(OsPath.of(OsType.LINUX, "/run"))
        }.isFailure().isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Can not resolve absolute or relative path 'home/admin' using absolute path '/run'")
    }

    // ************************************************* WINDOWS PATHS *************************************************

    @Test
    fun `Test Windows paths`() {
        assertThat(OsPath.of(OsType.WINDOWS, "C:\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.WINDOWS, "C:\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, "")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.WINDOWS, "file.txt")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, ".")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
        }

        assertThat(OsPath.of(OsType.WINDOWS, ".\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, "..\\home\\admin\\.kscript")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, "..")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
        }

        //Duplicated separators are accepted
        assertThat(OsPath.of(OsType.WINDOWS, "C:\\home\\\\\\\\admin\\.kscript\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }

        //Both types of separator are accepted
        assertThat(OsPath.of(OsType.WINDOWS, "C:/home\\admin/.kscript////")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
        }
    }

    @Test
    fun `Normalization of Windows paths`() {
        assertThat(OsPath.of(OsType.WINDOWS, "C:\\home\\admin\\.kscript\\..\\..\\")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, ".\\.\\.\\..\\..\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
        }

        assertThat(OsPath.of(OsType.WINDOWS, "C:\\a\\b\\c\\..\\d\\script")).let {
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
        }

        assertThat { OsPath.of(OsType.WINDOWS, "C:\\.kscript\\..\\..\\") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: 'C:\\'")
    }

    @Test
    fun `Test invalid Windows paths`() {
        assertThat { OsPath.of(OsType.WINDOWS, "C:\\adas?df") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character '?' in path 'C:\\adas?df'")

        assertThat { OsPath.of(OsType.WINDOWS, "home:\\vagrant") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character ':' in path 'home:\\vagrant'")
    }

    @Test
    fun `Test Windows stringPath`() {
        assertThat(
            OsPath.of(OsType.WINDOWS, "C:\\home\\admin\\.kscript").path
        ).isEqualTo("C:\\home\\admin\\.kscript")
        assertThat(
            OsPath.of(OsType.WINDOWS, "c:\\a\\b\\c\\..\\d\\script").path
        ).isEqualTo("c:\\a\\b\\d\\script")
        assertThat(
            OsPath.of(OsType.WINDOWS, ".\\.\\.\\..\\..\\script").path
        ).isEqualTo("..\\..\\script")
        assertThat(OsPath.of(OsType.WINDOWS, "script\\file.txt").path).isEqualTo("script\\file.txt")
    }

    // ****************************************** WINDOWS <-> CYGWIN <-> MSYS ******************************************

    @Test
    fun `Test Windows to Cygwin`() {
        assertThat(
            OsPath.of(OsType.WINDOWS, "C:\\home\\admin\\.kscript").convert(OsType.CYGWIN).path
        ).isEqualTo("/cygdrive/c/home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.WINDOWS, "..\\home\\admin\\.kscript").convert(OsType.CYGWIN).path
        ).isEqualTo("../home/admin/.kscript")
    }

    @Test
    fun `Test Cygwin to Windows`() {
        assertThat(
            OsPath.of(OsType.CYGWIN, "/cygdrive/c/home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath.of(OsType.CYGWIN, "../home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to MSys`() {
        assertThat(
            OsPath.of(OsType.WINDOWS, "C:\\home\\admin\\.kscript").convert(OsType.MSYS).path
        ).isEqualTo("/c/home/admin/.kscript")

        assertThat(
            OsPath.of(OsType.WINDOWS, "..\\home\\admin\\.kscript").convert(OsType.MSYS).path
        ).isEqualTo("../home/admin/.kscript")
    }

    @Test
    fun `Test MSys to Windows`() {
        assertThat(
            OsPath.of(OsType.MSYS, "/c/home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath.of(OsType.MSYS, "../home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }
}
