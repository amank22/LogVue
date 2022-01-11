package storage

import com.voxfinite.logvue.APP_NAME
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import java.io.IOException

object StorageHelper {

    @Throws(IOException::class)
    fun getDbFile(): File {
        val appDirs = AppDirsFactory.getInstance()
        val dataDir = appDirs.getUserDataDir(APP_NAME, null, APP_NAME)
        val dbName = "sessions.db"
        val dbFile = File(dataDir, dbName)
        if (!dbFile.exists()) {
            dbFile.createNewFile()
        }
        if (!checkFileValid(dbFile)) {
            throw IOException("Cannot create database file at path ${dbFile.canonicalPath}")
        }
        return dbFile
    }

    fun checkFileValid(file: File): Boolean {
        return ((file.canRead() && file.canWrite()))
    }

}
