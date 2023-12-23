package io.github.kscripting.os.instance

import io.github.kscripting.os.OsTypeNew
import io.github.kscripting.os.Vfs
import io.github.kscripting.os.createPosixOsPath
import io.github.kscripting.os.model.OsPath

class CygwinVfs(override val nativeFsRoot: OsPath<WindowsVfs>, userHome: String) : HostedVfs,
    PosixVfs(OsTypeNew.CYGWIN) {

    override fun toNative(osPath: OsPath<out Vfs>): OsPath<out Vfs> {
        val newParts = mutableListOf<String>()
        var newRoot = ""

        if (osPath.isAbsolute) {
            if (osPath.pathParts[0].equals("cygdrive", true)) { //Paths referring /cygdrive
                newRoot = osPath.pathParts[1] + ":\\"
                newParts.addAll(osPath.pathParts.subList(2, osPath.pathParts.size))
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

    override val userHome: OsPath<CygwinVfs> = createPosixOsPath(this, userHome)
    override fun createOsPath(path: String): OsPath<CygwinVfs> = createPosixOsPath(this, path)
}
