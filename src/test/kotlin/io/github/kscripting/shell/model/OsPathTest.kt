package io.github.kscripting.shell.model

import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.Test

class OsPathTest {
    // ************************************************** LINUX PATHS **************************************************
    @Test
    fun `Test Linux paths`() {
        assertThat(OsPath.createOrThrow(OsType.LINUX, "/")).let {
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::isRelative).isFalse()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "/home/admin/.kscript")).let {
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "./home/admin/.kscript")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::isAbsolute).isFalse()
            it.prop(OsPath::osType).isEqualTo(OsType.ANY)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "file.txt")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, ".")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.ANY)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "../home/admin/.kscript")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "..")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        //Duplicated separators are accepted
        assertThat(OsPath.createOrThrow(OsType.LINUX, "..//home////admin/.kscript/")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        //Both types of separator are accepted
        assertThat(OsPath.createOrThrow(OsType.LINUX, "..//home\\admin\\.kscript/")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        //Home dir is correctly handled
        assertThat(OsPath.createOrThrow(OsType.LINUX, "~/admin/.git")).let {
            it.prop(OsPath::root).isEqualTo("~")
            it.prop(OsPath::pathParts).isEqualTo(listOf("admin", ".git"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }
    }

    @Test
    fun `Normalization of Linux paths`() {
        assertThat(OsPath.createOrThrow(OsType.LINUX, "/home/admin/.kscript/../../")).let {
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "./././../../script")).let {
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat(OsPath.createOrThrow(OsType.LINUX, "/a/b/c/../d/script")).let {
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.LINUX)
        }

        assertThat { OsPath.createOrThrow(OsType.LINUX, "/.kscript/../../") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: '/'")
    }

    @Test
    fun `Test invalid Linux paths`() {
        assertThat { OsPath.createOrThrow(OsType.LINUX, "/ad*asdf") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java).hasMessage("Invalid character '*' in path '/ad*asdf'")
    }

    @Test
    fun `Test Linux stringPath`() {
        assertThat(OsPath.createOrThrow(OsType.LINUX, "/home/admin/.kscript").path)
            .isEqualTo("/home/admin/.kscript")
        assertThat(OsPath.createOrThrow(OsType.LINUX, "/a/b/c/../d/script").path).isEqualTo("/a/b/d/script")
        assertThat(OsPath.createOrThrow(OsType.LINUX, "./././../../script").path).isEqualTo("../../script")
        assertThat(OsPath.createOrThrow(OsType.LINUX, "script/file.txt").path).isEqualTo("script/file.txt")
    }

    @Test
    fun `Test Linux resolve`() {
        assertThat(
            OsPath.createOrThrow(OsType.LINUX, "/").resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/"))
                .path
        ).isEqualTo("/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.LINUX, "/home/admin/")
                .resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("/home/admin/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.LINUX, "./home/admin/")
                .resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("home/admin/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.LINUX, "../home/admin/")
                .resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo("../home/admin/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.LINUX, "..").resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/"))
                .path
        ).isEqualTo("../.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.LINUX, ".").resolve(OsPath.createOrThrow(OsType.LINUX, "./.kscript/")).path
        ).isEqualTo(".kscript")

        assertThat {
            OsPath.createOrThrow(OsType.LINUX, "./home/admin").resolve(OsPath.createOrThrow(OsType.WINDOWS, ".\\run"))
        }.isFailure().isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Paths from different OS's: 'LINUX' path can not be resolved with 'WINDOWS' path")

        assertThat {
            OsPath.createOrThrow(OsType.LINUX, "./home/admin").resolve(OsPath.createOrThrow(OsType.LINUX, "/run"))
        }.isFailure().isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Can not resolve absolute or relative path 'home/admin' using absolute path '/run'")
    }

    // ************************************************* WINDOWS PATHS *************************************************

    @Test
    fun `Test Windows paths`() {
        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:\\")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\admin\\.kscript")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.ANY)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "file.txt")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("file.txt"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, ".")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.ANY)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, ".\\home\\admin\\.kscript")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "..\\home\\admin\\.kscript")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "home", "admin", ".kscript"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "..")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf(".."))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        //Duplicated separators are accepted
        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\\\\\\\admin\\.kscript\\")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        //Both types of separator are accepted
        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:/home\\admin/.kscript////")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home", "admin", ".kscript"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }
    }

    @Test
    fun `Normalization of Windows paths`() {
        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\admin\\.kscript\\..\\..\\")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("home"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, ".\\.\\.\\..\\..\\script")).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(listOf("..", "..", "script"))
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat(OsPath.createOrThrow(OsType.WINDOWS, "C:\\a\\b\\c\\..\\d\\script")).let {
            it.prop(OsPath::root).isEqualTo("C:\\")
            it.prop(OsPath::pathParts).isEqualTo(listOf("a", "b", "d", "script"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.WINDOWS)
        }

        assertThat { OsPath.createOrThrow(OsType.WINDOWS, "C:\\.kscript\\..\\..\\") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Path after normalization goes beyond root element: 'C:\\'")
    }

    @Test
    fun `Test invalid Windows paths`() {
        assertThat { OsPath.createOrThrow(OsType.WINDOWS, "C:\\adas?df") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character '?' in path 'C:\\adas?df'")

        assertThat { OsPath.createOrThrow(OsType.WINDOWS, "home:\\vagrant") }.isFailure()
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid character ':' in path 'home:\\vagrant'")
    }

    @Test
    fun `Test Windows stringPath`() {
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\admin\\.kscript").path
        ).isEqualTo("C:\\home\\admin\\.kscript")
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "c:\\a\\b\\c\\..\\d\\script").path
        ).isEqualTo("c:\\a\\b\\d\\script")
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, ".\\.\\.\\..\\..\\script").path
        ).isEqualTo("..\\..\\script")
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "script\\file.txt").path
        ).isEqualTo("script\\file.txt")
    }

    // ****************************************** WINDOWS <-> CYGWIN <-> MSYS ******************************************

    @Test
    fun `Test Windows to Cygwin`() {
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\admin\\.kscript").convert(OsType.CYGWIN).path
        ).isEqualTo("/cygdrive/c/home/admin/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "..\\home\\admin\\.kscript").convert(OsType.CYGWIN).path
        ).isEqualTo("../home/admin/.kscript")
    }

    @Test
    fun `Test Cygwin to Windows`() {
        assertThat(
            OsPath.createOrThrow(OsType.CYGWIN, "/cygdrive/c/home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.CYGWIN, "../home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    @Test
    fun `Test Windows to MSys`() {
        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "C:\\home\\admin\\.kscript").convert(OsType.MSYS).path
        ).isEqualTo("/c/home/admin/.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.WINDOWS, "..\\home\\admin\\.kscript").convert(OsType.MSYS).path
        ).isEqualTo("../home/admin/.kscript")
    }

    @Test
    fun `Test MSys to Windows`() {
        assertThat(
            OsPath.createOrThrow(OsType.MSYS, "/c/home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("c:\\home\\admin\\.kscript")

        assertThat(
            OsPath.createOrThrow(OsType.MSYS, "../home/admin/.kscript").convert(OsType.WINDOWS).path
        ).isEqualTo("..\\home\\admin\\.kscript")
    }

    // ************************************************* Special cases *************************************************

    @Test
    fun `Special cases`() {
        //Empty path
        assertThat(OsPath.createOrThrow(OsType.LINUX)).let {
            it.prop(OsPath::root).isEqualTo("")
            it.prop(OsPath::pathParts).isEqualTo(emptyList())
            it.prop(OsPath::isRelative).isTrue()
            it.prop(OsPath::isAbsolute).isFalse()
            it.prop(OsPath::osType).isEqualTo(OsType.ANY)
        }
    }

    // ************************************* Shorthand for creating composite paths ************************************

    @Test
    fun `Concatenate paths`() {
        val p = OsPath.createOrThrow(OsType.MSYS, "/c/home")
        val p1 = OsPath.createOrThrow(OsType.MSYS, "admin/.kscript")

        assertThat(p / p1).let {
            it.prop(OsPath::root).isEqualTo("/")
            it.prop(OsPath::pathParts).isEqualTo(listOf("c", "home", "admin", ".kscript"))
            it.prop(OsPath::isAbsolute).isTrue()
            it.prop(OsPath::osType).isEqualTo(OsType.MSYS)
        }
    }
}
