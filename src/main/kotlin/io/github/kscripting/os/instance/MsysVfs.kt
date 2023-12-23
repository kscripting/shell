package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs
import io.github.kscripting.os.util.createPosixOsPath
import io.github.kscripting.os.model.OsPath
import io.github.kscripting.os.util.toHostedConverter

class MsysVfs(override val nativeFsRoot: OsPath<WindowsVfs>, userHome: String) : HostedVfs, PosixVfs(OsTypeNew.MSYS) {
    override val userHome: OsPath<MsysVfs> = createOsPath(userHome)
    override fun createOsPath(path: String): OsPath<MsysVfs> = createPosixOsPath(this, path)

    override fun toNative(providedOsPath: OsPath<out Vfs>): OsPath<WindowsVfs> {
        val osPath = providedOsPath as OsPath<MsysVfs>
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

    override fun toHosted(osPath: OsPath<out Vfs>): OsPath<MsysVfs> {
        return toHostedConverter(this, osPath as OsPath<WindowsVfs>)
    }
}
