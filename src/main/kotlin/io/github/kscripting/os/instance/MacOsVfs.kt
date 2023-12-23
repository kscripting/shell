package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.createPosixOsPath
import io.github.kscripting.os.model.OsPath

class MacOsVfs(userHome: String) : PosixVfs(OsTypeNew.MACOS) {
    override val userHome: OsPath<MacOsVfs> = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath<MacOsVfs> = createPosixOsPath(this, path)
}
