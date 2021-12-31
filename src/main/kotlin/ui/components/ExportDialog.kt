package ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import models.*
import processor.Exporter
import storage.Db
import ui.CustomTheme
import utils.Helpers
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Composable
fun ExportDialog(
    sessionInfo: SessionInfo, logItems: List<LogItem>,
    onDismissRequest: () -> Unit
) {
    StyledCustomVerticalDialog(onDismissRequest) {
        Column(Modifier.fillMaxHeight().padding(16.dp)) {
            var exportFilteredLogs by remember { mutableStateOf(true) }
            var isFileSelectorOpen by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            Text("Export Session", style = CustomTheme.typography.headings.h2)
            Spacer(Modifier.height(24.dp))
            SelectCheckBox(exportFilteredLogs, "Export filtered logs") {
                exportFilteredLogs = !exportFilteredLogs
            }
            Spacer(Modifier.height(16.dp))
            Text("Parameter formats:")
            Spacer(Modifier.height(8.dp))
            var selectedFormat by remember { mutableStateOf<ParameterFormats>(FormatJsonPretty) }
            ParameterFormats(selectedFormat) {
                selectedFormat = it
            }
            Spacer(Modifier.height(24.dp))
            Button({
                isFileSelectorOpen = true
            }) {
                Icon(painterResource("icons/ico-share.svg"), "Export session")
                Text("Export")
            }
            if (isFileSelectorOpen) {
                val fileName = sessionInfo.description.replace(" ", "_").capitalize(Locale.current)
                val appended = if (selectedFormat == FormatYaml) {
                    "_yaml"
                } else "_json"
                ExportFile("$fileName$appended.txt") { path ->
                    isFileSelectorOpen = false
                    scope.launch(Dispatchers.IO) {
                        val logs = getListForExport(exportFilteredLogs, logItems)
                        Exporter.exportList(sessionInfo, logs, path, selectedFormat)
                        Db.configs["lastExportFolder"] = path.parent.absolutePathString()
                        Helpers.openFileExplorer(path.parent)
                        Helpers.openFileExplorer(path)
                        onDismissRequest()
                    }
                }
            }
        }
    }
}

private fun getListForExport(
    exportFilteredLogs: Boolean,
    logItems: List<LogItem>
) = if (exportFilteredLogs) {
    logItems
} else {
    val session = Db.currentSession()
    session?.map { it.value }?.sortedBy { it.localTime } ?: emptyList()
}

@Composable
private fun ExportFile(fileName: String, onResult: (result: Path) -> Unit) {
    FileDialog("Choose file to save", fileName) { path ->
        if (path != null) {
            onResult(path)
        }
    }
}

@Composable
private fun ParameterFormats(
    selected: ParameterFormats,
    formats: List<ParameterFormats> = DefaultFormats,
    modifier: Modifier = Modifier,
    onSelected: (format: ParameterFormats) -> Unit
) {
    Column(modifier) {
        formats.forEach {
            SelectRadioButton(selected == it, it.text) {
                onSelected(it)
            }
        }
    }

}

@Composable
private fun SelectRadioButton(selected: Boolean, text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier.clickable(MutableInteractionSource(), null, onClick = { onClick() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected, onClick)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun SelectCheckBox(
    selected: Boolean, text: String, modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier.clickable(MutableInteractionSource(), null, onClick = { onClick() }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(selected, null)
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}