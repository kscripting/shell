package io.github.kscripting.shell.integration

import org.junit.jupiter.api.BeforeAll

interface TestBase {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
        }
    }
}
