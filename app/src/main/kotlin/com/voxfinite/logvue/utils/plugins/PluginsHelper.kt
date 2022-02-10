package com.voxfinite.logvue.utils.plugins

import com.voxfinite.logvue.api.LogEventParser
import com.voxfinite.logvue.parsers.FirebaseParser
import com.voxfinite.logvue.storage.StorageHelper
import com.voxfinite.logvue.utils.AppLog
import org.pf4j.CompoundPluginDescriptorFinder
import org.pf4j.DefaultPluginManager
import org.pf4j.ManifestPluginDescriptorFinder
import org.pf4j.PropertiesPluginDescriptorFinder
import java.nio.file.Path
import kotlin.jvm.Throws

object PluginsHelper {

    internal class PluginManager(importPaths: List<Path>) : DefaultPluginManager(importPaths) {
        override fun createPluginDescriptorFinder(): CompoundPluginDescriptorFinder {
            return CompoundPluginDescriptorFinder() // Demo is using the Manifest file
                // PropertiesPluginDescriptorFinder is commented out just to avoid error log
                .add(PropertiesPluginDescriptorFinder())
                .add(ManifestPluginDescriptorFinder());
        }
    }

    internal val pluginManager by lazy {
        val pluginsPath = StorageHelper.getPluginsPath()
        AppLog.d(pluginsPath.toString())
        PluginManager(listOf(pluginsPath))
    }

    private val parsers by lazy {
        pluginManager.getExtensions(LogEventParser::class.java)
    }

    fun load() {
        // load the plugins
        pluginManager.loadPlugins()
        // enable a disabled plugin
        // pluginManager.enablePlugin("welcome-plugin")

        // start (active/resolved) the plugins
        pluginManager.startPlugins()
        parsers
    }

    fun parsers(): MutableList<LogEventParser> {
        val allParsers = mutableListOf<LogEventParser>()
        allParsers.addAll(getDefaultParsers())
        allParsers.addAll(parsers)
        return allParsers
    }

    @Throws(ClassCastException::class)
    fun tryResolveClass(className : String) : Class<*> {
        pluginManager.resolvedPlugins.forEach {
            val classLoader = it.pluginClassLoader
            val resolvedClass: Class<*>?
            try {
                resolvedClass = Class.forName(className, false, classLoader)
                return resolvedClass
            } catch (ignore : ClassNotFoundException) {
                // ignore
            }
        }
        return Class.forName(className, false, Thread.currentThread().contextClassLoader)
    }

    private fun getDefaultParsers(): MutableList<LogEventParser> = arrayListOf(FirebaseParser())

    fun stop() {
        // stop the plugins
        pluginManager.stopPlugins()
    }

}
