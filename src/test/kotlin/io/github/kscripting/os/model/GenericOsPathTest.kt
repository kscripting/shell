package io.github.kscripting.os.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.github.kscripting.os.instance.LinuxOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.Result.Companion.success

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericOsPathTest {
    private lateinit var emptyPath: OsPath
    private lateinit var simplePath: OsPath

    @BeforeAll
    fun beforeAll() {
        GlobalContext.registerOrReplace(OsType.LINUX, LinuxOs("/home/admin"))
        emptyPath = OsPath(OsType.LINUX, "", emptyList()).getOrThrow()
        simplePath = OsPath(OsType.LINUX, "", listOf("home", "admin")).getOrThrow()
    }

    @Test
    fun `Test Empty paths`() {
        assertThat(emptyPath.isEmpty).isTrue()

        assertThat(emptyPath.resolve(emptyPath)).isEqualTo(success(emptyPath))
        assertThat(emptyPath / emptyPath).isEqualTo(success(emptyPath))

        assertThat(simplePath / emptyPath).isEqualTo(success(simplePath))
        assertThat(emptyPath / simplePath).isEqualTo(success(simplePath))

        assertThat(emptyPath.toString()).isEqualTo("[LINUX]")
        assertThat(simplePath.toString()).isEqualTo("home/admin [LINUX]")

        assertThat(emptyPath.leaf).isEqualTo("")
        assertThat(simplePath.leaf).isEqualTo("admin")
    }
}
