package io.github.kscripting.os

import org.apache.commons.lang3.SystemUtils

class OsBuilder {

    fun build(): Os {
        TODO()

    }


    val native: OsType = guessNativeType()

//    fun findByOsTypeString(osTypeString: String): OsTypeNew? =
//        GlobalOsType.find { osTypeString.startsWith(it.os.osTypePrefix, true) }

    private fun guessNativeType(): OsType = when {
        SystemUtils.IS_OS_MAC -> OsType.MACOS
        SystemUtils.IS_OS_WINDOWS -> OsType.WINDOWS
        SystemUtils.IS_OS_FREE_BSD -> OsType.FREEBSD
        else -> OsType.LINUX
    }

}
