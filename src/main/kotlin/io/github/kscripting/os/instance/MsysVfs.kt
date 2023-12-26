package io.github.kscripting.os.instance

import io.github.kscripting.os.OsType
import io.github.kscripting.os.Vfs
import io.github.kscripting.os.util.createPosixOsPath
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.util.toHostedConverter

class MsysVfs(override val nativeFsRoot: OsPath, userHome: String) : HostedVfs, PosixVfs(OsType.MSYS) {
    override val userHome: OsPath = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath = createPosixOsPath(this, path)

    override fun toNative(providedOsPath: OsPath): OsPath {
        val osPath = providedOsPath
        val newParts = mutableListOf<String>()
        var newRoot = ""

        if (osPath.isAbsolute) {
            if (osPath.pathParts[0].length == 1 && (osPath.pathParts[0][0].code in 65..90 || osPath.pathParts[0][0].code in 97..122)) { //Paths referring with drive letter at the beginning
                newRoot = osPath.pathParts[0] + ":\\"
                newParts.addAll(osPath.pathParts.subList(1, osPath.pathParts.size))
            } else if (osPath.root == "~") { //Paths starting with ~
                newRoot = this.nativeFsRoot.root
                newParts.addAll(this.nativeFsRoot.pathParts)
                newParts.addAll(this.userHome.pathParts)
                newParts.addAll(osPath.pathParts)
            } else { //Any other path like: /usr/bin
                newRoot = this.nativeFsRoot.root
                newParts.addAll(this.nativeFsRoot.pathParts)
                newParts.addAll(osPath.pathParts)
            }
        } else {
            newParts.addAll(osPath.pathParts)
        }

        return OsPath(nativeFsRoot.vfs, newRoot, newParts)
    }

    override fun toHosted(osPath: OsPath): OsPath {
        return toHostedConverter(this, osPath)
    }
}
