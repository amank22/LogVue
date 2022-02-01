package com.voxfinite.logvue.storage

import com.voxfinite.app.APP_NAME
import com.voxfinite.app.PLUGINS_PATH
import net.harawata.appdirs.AppDirsFactory
import org.mapdb.DB
import org.mapdb.DBException
import org.mapdb.DBMaker
import com.voxfinite.logvue.utils.DbCreationException
import com.voxfinite.logvue.utils.reportException
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermissions


object StorageHelper {

    internal fun createDiskDb(): DB {
        val dbFile = getDbFile()
        return try {
            DBMaker.fileDB(dbFile).fileMmapEnableIfSupported().checksumHeaderBypass().make()
        } catch (e: DBException.VolumeIOError) {
            DbCreationException("Mmap enabled db could not be created", e).reportException()
            try {
                DBMaker.fileDB(dbFile).fileChannelEnable().checksumHeaderBypass().make()
            } catch (ee: DBException.VolumeIOError) {
                DbCreationException("file channel enabled db could not be created", ee).reportException()
                DBMaker.fileDB(dbFile).checksumHeaderBypass().make()
            }
        }
    }

    fun getPluginsPath() : Path {
        val appDir = appDir()
        val pluginDir = File(appDir, "plugins")
        createDir(pluginDir)
        var pluginPath = PLUGINS_PATH
        if (pluginPath.isBlank()) {
            pluginPath = pluginDir.absolutePath
        }
        return Paths.get(pluginPath)
    }

    @Throws(IOException::class)
    private fun getDbFile(): File {
        val dataDir = appDir()
        val dbName = "sessions.db"
        return File(dataDir, dbName)
    }

    private fun appDir(): File {
        val appDirs = AppDirsFactory.getInstance()
        val dataDir = appDirs.getUserDataDir(APP_NAME, null, APP_NAME)
        val folder = File(dataDir)
        createDir(folder)
        return folder
    }

    private fun createDir(folder: File) {
        if (folder.exists() && folder.isDirectory) return
        if (folder.exists()) {
            folder.delete()
        }
        try {
            val isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
            if (isPosix) {
                val posixAttribute = PosixFilePermissions.asFileAttribute(
                    PosixFilePermissions.fromString("rwxr-x---")
                )
                Files.createDirectories(folder.toPath(), posixAttribute)
            } else {
                Files.createDirectories(folder.toPath())
            }
        } catch (e: IOException) {
            throw IOException("Cannot create app folder at path ${folder.canonicalPath}", e)
        }
    }

}