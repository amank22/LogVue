package storage

import com.voxfinite.logvue.APP_NAME
import net.harawata.appdirs.AppDirsFactory
import org.mapdb.DB
import org.mapdb.DBException
import org.mapdb.DBMaker
import utils.DbCreationException
import utils.reportException
import java.io.File
import java.io.IOException
import java.nio.file.Files
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

    @Throws(IOException::class)
    private fun getDbFile(): File {
        val appDirs = AppDirsFactory.getInstance()
        val dataDir = appDirs.getUserDataDir(APP_NAME, null, APP_NAME)
        val folder = File(dataDir)
        if (!folder.exists() || !folder.isDirectory) {
            if (folder.exists()) {
                folder.delete()
            }
            try {
                Files.createDirectory(
                    folder.toPath(), PosixFilePermissions.asFileAttribute(
                        PosixFilePermissions.fromString("rwxr-x---")
                    )
                )
            } catch (e: IOException) {
                throw IOException("Cannot create app folder at path ${folder.canonicalPath}", e)
            }
        }
        val dbName = "sessions.db"
        return File(dataDir, dbName)
    }

}