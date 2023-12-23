package io.github.kscripting.os

import io.github.kscripting.os.model.GlobalOsType
import org.apache.commons.lang3.SystemUtils

class OsBuilder {

    fun build(): Os {
        TODO()

    }


    val native: OsTypeNew = guessNativeType()

//    fun findByOsTypeString(osTypeString: String): OsTypeNew? =
//        GlobalOsType.find { osTypeString.startsWith(it.os.osTypePrefix, true) }

    private fun guessNativeType(): OsTypeNew = when {
        SystemUtils.IS_OS_MAC -> OsTypeNew.MACOS
        SystemUtils.IS_OS_WINDOWS -> OsTypeNew.WINDOWS
        SystemUtils.IS_OS_FREE_BSD -> OsTypeNew.FREEBSD
        else -> OsTypeNew.LINUX
    }

}
