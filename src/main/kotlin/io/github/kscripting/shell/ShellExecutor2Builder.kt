package io.github.kscripting.shell

class ShellExecutor2Builder {
    private var printCommandsPattern: String? = null

    fun withPrinter(printer: (String) -> String) {
        //e.g. withPrinter { ">> $it" }

    }

//    fun throwOnExitCode(thrower: (ProcessResult) -> ProcessResult) {
//
//    }
//
//    fun throwOnExitCode(thrower: (GobbledProcessResult) -> GobbledProcessResult) {
//
//    }

//    fun withResultProvider(provider: (String) -> ProcessResult) {
//
//    }
//
//    fun withResultProvider(provider: (String) -> GobbledProcessResult) {
//
//    }


    fun build(): ShellExecutor2 = ShellExecutor2()
}
