package io.github.kscripting.os.instance

import io.github.kscripting.os.Os

interface HostedOs : Os {
    val nativeOs: Os
}
