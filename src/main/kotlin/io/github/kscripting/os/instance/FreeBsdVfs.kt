package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.util.createPosixOsPath
import io.github.kscripting.os.model.OsPath

class FreeBsdVfs(userHome: String) : PosixVfs(OsTypeNew.FREEBSD) {
    override val userHome: OsPath<FreeBsdVfs> = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath<FreeBsdVfs> = createPosixOsPath(this, path)
}
