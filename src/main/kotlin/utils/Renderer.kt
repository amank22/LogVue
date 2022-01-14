package utils

import org.jetbrains.skiko.GraphicsApi
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs

object Renderer {

//    private const val RENDER_API_KEY = "skiko.renderApi"

//    fun setRender() {
//        when(SystemTools.getOS()) {
//            OsWindows, OsLinux -> {
//                System.setProperty(RENDER_API_KEY, "OPENGL")
//            }
//            OsMac -> {
//
//            }
//        }
//    }

    internal fun parseRenderApi(text: String?): GraphicsApi {
        return when (text) {
            "SOFTWARE_COMPAT" -> GraphicsApi.SOFTWARE_COMPAT
            "SOFTWARE_FAST", "DIRECT_SOFTWARE", "SOFTWARE" -> GraphicsApi.SOFTWARE_FAST
            "OPENGL" -> GraphicsApi.OPENGL
            "DIRECT3D" -> {
                if (hostOs == OS.Windows) GraphicsApi.DIRECT3D
                else throw UnsupportedOperationException("$hostOs does not support DirectX rendering API.")
            }
            "METAL" -> {
                if (hostOs == OS.MacOS) GraphicsApi.METAL
                else throw UnsupportedOperationException("$hostOs does not support Metal rendering API.")
            }
            else -> bestRenderApiForCurrentOS()
        }
    }

    private fun bestRenderApiForCurrentOS(): GraphicsApi {
        return when (hostOs) {
            OS.MacOS -> GraphicsApi.METAL
            OS.Linux -> GraphicsApi.OPENGL
            OS.Windows -> GraphicsApi.DIRECT3D
            OS.JS, OS.Ios -> TODO("commonize me")
        }
    }
}
