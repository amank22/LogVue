package ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import storage.Db
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Path

@Composable
fun FileDialog(
    title: String,
    fileName: String = "file.txt",
    isLoad: Boolean = false,
    parent: Frame? = null,
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, title, if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
            val lastFolderPath = Db.configs["lastExportFolder"]
            if (!lastFolderPath.isNullOrBlank()) {
                this.directory = lastFolderPath
            }
            this.file = fileName
        }
    },
    dispose = FileDialog::dispose
)
