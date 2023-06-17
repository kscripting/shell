package io.github.kscripting.shell.integration.tools

import org.opentest4j.AssertionFailedError

abstract class TestMatcher<T>(protected val expectedValue: T, private val expressionName: String) {
    abstract fun matches(value: T): Boolean

    fun checkAssertion(assertionName: String, value: T) {
        if (matches(value)) {
            return
        }

        throw AssertionFailedError(
            """|
               |Expected that '$assertionName' value:
               |'${value.toString()}'
               |$expressionName
               |'${expectedValue.toString()}'
               |
               |""".trimMargin()
        )
    }
}

class GenericEquals<T : Any>(expectedValue: T) : TestMatcher<T>(expectedValue, "is equal to") {
    override fun matches(value: T): Boolean = (value == expectedValue)
}

class AnyMatch : TestMatcher<String>("", "has any value") {
    override fun matches(value: String): Boolean = true
}

class Equals(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "is equal to") {
    override fun matches(value: String): Boolean =
        value.equals(expectedString, ignoreCase)
}

class StartsWith(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "starts with") {
    override fun matches(value: String): Boolean =
        value.startsWith(expectedString, ignoreCase)
}

class Contains(private val expectedString: String, private val ignoreCase: Boolean) :
    TestMatcher<String>(expectedString, "contains") {
    override fun matches(value: String): Boolean =
        value.contains(expectedString, ignoreCase)
}
