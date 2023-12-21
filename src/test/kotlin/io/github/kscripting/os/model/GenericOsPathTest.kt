package io.github.kscripting.os.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.github.kscripting.os.instance.LinuxOs
import net.igsoft.typeutils.globalcontext.GlobalContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GenericOsPathTest {
    private var simplePath: OsPath

    init {
        GlobalContext.registerOrReplace(GlobalOsType.LINUX, LinuxOs(GlobalOsType.LINUX, "/home/admin"))
        simplePath = OsPath(GlobalOsType.LINUX, "", listOf("home", "admin"))
    }

    @Test
    fun `Test Empty paths`() {
        assertThat(simplePath.toString()).isEqualTo("home/admin [LINUX]")
        assertThat(simplePath.leaf).isEqualTo("admin")

        //println(simplePath.flatMap { it.resolve("mk", "km") }.flatMap { it.parent })

    }
}
