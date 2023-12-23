package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.createPosixOsPath
import io.github.kscripting.os.model.OsPath

class LinuxVfs(userHome: String) : PosixVfs(OsTypeNew.LINUX) {
    override val userHome: OsPath<LinuxVfs> = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath<LinuxVfs> = createPosixOsPath(this, path)
}
