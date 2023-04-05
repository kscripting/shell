package io.github.kscripting.shell

fun main() {
    val executor = ShellExecutor2.builder().build()

    executor.execute()
}