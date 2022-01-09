package ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import ui.components.common.MultiLineRadioButton
import ui.components.common.SwitchItem
import utils.Helpers
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Composable
fun ExportDialog(
    sessionInfo: SessionInfo,
    logItems: List<LogItem>,
    onDismissRequest: () -> Unit
) {
    SimpleVerticalDialog("Export Session", onDismissRequest, PaddingValues()) {
        var exportFilteredLogs by remember { mutableStateOf(true) }
        var isFileSelectorOpen by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        SwitchItem(
            exportFilteredLogs, "Export filtered logs", Modifier.fillMaxWidth()
                .padding(16.dp),
            "Only filtered logs or full session including all the logs",
            painterResource("icons/ico_filter.svg")
        ) {
            exportFilteredLogs = it
        }
        Text("Parameter formats:", Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        var selectedFormat by remember { mutableStateOf<ParameterFormats>(FormatJsonPretty) }
        ParameterFormats(selectedFormat, modifier = Modifier.padding(horizontal = 6.dp)) {
            selectedFormat = it
        }
        Spacer(Modifier.height(24.dp))
        Button({
            isFileSelectorOpen = true
        }, Modifier.padding(horizontal = 16.dp)) {
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
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        formats.forEach {
            MultiLineRadioButton(
                selected = selected == it, title = it.text,
                modifier = Modifier.fillMaxWidth(),
                subTitle = it.subText,
                spacing = 0.dp
            ) {
                onSelected(it)
            }
        }
    }
}
