package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import io.github.kscripting.os.util.createPosixOsPath
import io.github.kscripting.os.model.OsPath

class LinuxVfs(userHome: String) : PosixVfs(OsType.LINUX) {
    override val userHome: OsPath = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath = createPosixOsPath(this, path)
}
