package io.github.kscripting.shell

import java.util.*

abstract class Verifier<T>(value: T)

/**
 * Asserts on the given value with an optional name.
 *
 * ```
 * assertThat(true, name = "true").isTrue()
 * ```
 */
fun <T> verifyThat(
    actual: T,
    name: String? = null,
    displayActual: (T) -> String = { "display(it)" }
): Assert<T> = ValueAssert(
    value = actual,
    name = name,
    context = AssertingContext { displayActual(actual) }
)

sealed class Assert<out T>(val name: String?, internal val context: AssertingContext) {


    /**
     * Allows checking the actual value of an assert. This can be used to build your own custom assertions.
     * ```
     * fun Assert<Int>.isTen() = given { actual ->
     *     if (actual == 10) return
     *     expected("to be 10 but was:${show(actual)}")
     * }
     * ```
     */
    @Suppress("TooGenericExceptionCaught")
    inline fun given(assertion: (T) -> Unit) {
        if (this is ValueAssert) {
            try {
                assertion(value)
            } catch (e: Throwable) {
                notifyFailure(e)
            }
        }
    }

    @PublishedApi
    internal fun <R> failing(error: Throwable, name: String? = this.name): Assert<R> {
        return FailingAssert(error, name, context)
    }

    /**
     * Asserts on the given value with an optional name.
     *
     * ```
     * assertThat(true, name = "true").isTrue()
     * ```
     */
    abstract fun <R> assertThat(actual: R, name: String? = this.name): Assert<R>
}

@PublishedApi
internal class ValueAssert<out T>(val value: T, name: String?, context: AssertingContext) :
    Assert<T>(name, context) {

    override fun <R> assertThat(actual: R, name: String?): Assert<R> {
        val newContext = if (context.originatingSubject != null || this.value == actual) {
            context
        } else {
            context.copy(originatingSubject = this.value)
        }
        return ValueAssert(actual, name, newContext)
    }
}



internal class FailingAssert<out T>(val error: Throwable, name: String?, context: AssertingContext) :
    Assert<T>(name, context) {
    override fun <R> assertThat(actual: R, name: String?): Assert<R> = FailingAssert(error, name, context)
}


internal data class AssertingContext(
    val originatingSubject: Any? = null,
    val displayOriginatingSubject: () -> String
)

fun notifyFailure(e: Throwable) {
    FailureContext.fail(e)
}

/**
 * Assertions are run in a failure context which captures failures to report them.
 */
internal object FailureContext {

    fun fail(error: Throwable) {

    }
}

fun Assert<String?>.isEqualTo(other: String?, ignoreCase: Boolean = false) = given { actual ->
    if (actual.equals(other, ignoreCase)) return
    //fail(other, actual)
}

fun main() {
    verifyThat("").isEqualTo("")
    verifyThat(Date())//.isEqualTo()
}
