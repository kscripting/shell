package io.github.kscripting.shell.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test


class SanitizerTest {
    @Test
    fun `Test sanitize`() {
        val sanitizer = Sanitizer("<very important password>" to "<***>")
        assertThat(sanitizer.sanitize("This is <very important password>!!!")).isEqualTo("This is <***>!!!")
    }

    @Test
    fun `Test swapped`() {
        val sanitizer = Sanitizer("<very important password>" to "<***>").swapped()
        assertThat(sanitizer.sanitize("This is <***>!!!")).isEqualTo("This is <very important password>!!!")
    }

    @Test
    fun `Test calculatePotentialMatch - corner cases`() {
        assertThat(Sanitizer.EMPTY_SANITIZER.calculatePotentialMatch("abcd")).isEqualTo("")

        val sanitizer = Sanitizer("[" to "]")
        assertThat(sanitizer.calculatePotentialMatch("abcd[bs[")).isEqualTo("")
    }

    @Test
    fun `Test calculatePotentialMatch - normal cases`() {
        val sanitizer = Sanitizer("<bs>" to "\\", "<tb>" to "\t", "<nl>" to "\n")

        assertThat(sanitizer.calculatePotentialMatch("")).isEqualTo("")
        assertThat(sanitizer.calculatePotentialMatch("abcd")).isEqualTo("")
        assertThat(sanitizer.calculatePotentialMatch("abcd<bs")).isEqualTo("<bs")
        assertThat(sanitizer.calculatePotentialMatch("abcd<b")).isEqualTo("<b")
        assertThat(sanitizer.calculatePotentialMatch("abcd<")).isEqualTo("<")

        val longSanitizer = Sanitizer("<long sanitizer>" to "<l>", "<shorter>" to "<s>", "<nl>" to "<n>")
        assertThat(longSanitizer.calculatePotentialMatch("abcd<long sanitizer>")).isEqualTo("")
        assertThat(longSanitizer.calculatePotentialMatch("abcd<long ")).isEqualTo("<long ")
        assertThat(longSanitizer.calculatePotentialMatch("abcd<sho")).isEqualTo("<sho")
        assertThat(longSanitizer.calculatePotentialMatch("<shorter>ab<nl>cd<sho")).isEqualTo("<sho")
    }
}
