package utils

import java.util.*

object SystemTools {

    fun getOS(): OS {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        return when {
            os.contains("window") -> OsWindows
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> OsLinux
            os.contains("os x") || os.contains("mac") -> OsMac
            else -> throw UnsupportedOperationException("Operating system $os is not supported")
        }
    }

}

sealed interface OS
object OsWindows : OS
object OsLinux : OS
object OsMac : OS