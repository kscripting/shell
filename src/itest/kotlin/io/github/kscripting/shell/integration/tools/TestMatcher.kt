package io.github.kscripting.shell.integration.tools

import io.github.kscripting.shell.integration.tools.TestContext.nl
import org.opentest4j.AssertionFailedError

abstract class TestMatcher<T>(protected val expectedValue: T, private val expressionName: String) {
    abstract fun matches(value: T, sanitizer: Sanitizer = emptySanitizer): Boolean

    fun checkAssertion(assertionName: String, value: T, sanitizer: Sanitizer = emptySanitizer) {
        if (matches(value, sanitizer)) {
            return
        }

        throw AssertionFailedError(
            """|
               |Expected that '$assertionName' value:
               |'${sanitizer.sanitizeOutput(value.toString())}'
               |$expressionName
               |'${expectedValue.toString()}'
               |
               |""".trimMargin()
        )
    }

    companion object {
        val emptySanitizer = Sanitizer(emptyMap())
    }
}

class GenericEquals<T : Any>(expectedValue: T) : TestMatcher<T>(expectedValue, "is equal to") {
    override fun matches(value: T, sanitizer: Sanitizer): Boolean = (value == expectedValue)
}

class AnyMatch : TestMatcher<String>("", "has any value") {
    override fun matches(value: String, sanitizer: Sanitizer): Boolean = true
}

class Equals(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "is equal to") {
    override fun matches(value: String, sanitizer: Sanitizer): Boolean = value.equals(sanitizer.sanitizeInput(expectedString), ignoreCase)
}

class StartsWith(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "starts with") {
    override fun matches(value: String, sanitizer: Sanitizer): Boolean = value.startsWith(sanitizer.sanitizeInput(expectedString), ignoreCase)
}

class Contains(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "contains") {
    override fun matches(value: String, sanitizer: Sanitizer): Boolean = value.contains(sanitizer.sanitizeInput(expectedString), ignoreCase)
}
