package io.github.kscripting.os

import io.github.kscripting.os.instance.LinuxOs
import net.igsoft.typeutils.marker.NamedAutoTypedMarker

interface OsProvider {
    val native: Os
    fun provide(marker: NamedAutoTypedMarker<out Os> = native.marker): Os

    companion object {
        private val defaultOsProvider = DefaultOsProvider()
        private val providerStack = java.util.ArrayDeque<OsProvider>()

        fun push(osProvider: OsProvider) {
            synchronized(providerStack) {
                providerStack.push(osProvider)
            }
        }

        fun poll() {
            synchronized(providerStack) {
                providerStack.poll()
            }
        }


        val current: OsProvider get() {
            synchronized(providerStack) {
                return providerStack.peek() ?: defaultOsProvider
            }
        }
    }
}

@Synchronized
fun osContext(block: (OsProvider) -> Unit) {
    block(OsProvider.current)
}

@Synchronized
fun osContext(osProvider: OsProvider, block: (OsProvider) -> Unit) {
    OsProvider.push(osProvider)
    block(osProvider)
    OsProvider.poll()
}


class DefaultOsProvider : OsProvider {
    override val native: Os = TODO()

    override fun provide(marker: NamedAutoTypedMarker<out Os>): Os {
        TODO()
    }
}


class MockProvider : OsProvider {
    override val native: Os
        get() = TODO("Not yet implemented")

    override fun provide(marker: NamedAutoTypedMarker<out Os>): Os {
        TODO("Not yet implemented")
    }
}

fun main() {
    osContext {
        val os = it.provide(LinuxOs.marker)
    }

    osContext(MockProvider()) {
        val os = it.provide(LinuxOs.marker)

        osContext {
            val subOs = it.provide(LinuxOs.marker)
        }
    }

    OsProvider.current.provide(LinuxOs.marker)
}
