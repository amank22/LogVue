package com.voxfinite.logvue.utils.plugins

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import java.io.InputStream

@OptIn(ExperimentalComposeUiApi::class)
class PluginsResourceLoader : ResourceLoader {
    override fun load(resourcePath: String): InputStream {
        PluginsHelper.pluginManager.resolvedPlugins.forEach {
            val classLoader = it.pluginClassLoader
            val resource = try {
                classLoader.getResourceAsStream(resourcePath)
            } catch (ignore : Exception) {
                null
            }
            if (resource != null) {
                return resource
            }
        }
        // TODO(https://github.com/JetBrains/compose-jb/issues/618): probably we shouldn't use
        //  contextClassLoader here, as it is not defined in threads created by non-JVM
        val contextClassLoader = Thread.currentThread().contextClassLoader!!
        val resource = contextClassLoader.getResourceAsStream(resourcePath)
            ?: (::PluginsResourceLoader.javaClass).getResourceAsStream(resourcePath)
        return requireNotNull(resource) { "Resource $resourcePath not found" }
    }
}
