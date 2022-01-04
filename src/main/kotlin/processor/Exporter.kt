package processor

import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.*
import utils.Helpers
import java.io.PrintWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import kotlin.io.path.bufferedWriter

object Exporter {

    suspend fun exportList(
        sessionInfo: SessionInfo,
        list: List<LogItem>,
        filePath: Path,
        selectedFormat: ParameterFormats
    ) = withContext(Dispatchers.IO) {
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss.S")
        val buffer = filePath.bufferedWriter(options = arrayOf(StandardOpenOption.WRITE, StandardOpenOption.CREATE))
        PrintWriter(buffer).use { printWriter ->
            printWriter.append("Session: ")
            printWriter.append(sessionInfo.description)
            buffer.newLine()
            printWriter.append("App package: ")
            printWriter.append(sessionInfo.appPackage)
            val gsonBuilder = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            if (selectedFormat == FormatJsonPretty) {
                gsonBuilder.setPrettyPrinting()
            }
            val gson = gsonBuilder.create()
            val yamlWriter = YamlWriter(printWriter)
            list.stream().filter { it.source != SourceInternalContent }.forEach {
                printWriter.newLine()
                printWriter.newLine()
                printWriter.append("Event: ")
                printWriter.append(it.eventName)
                printWriter.newLine()
                printWriter.append("Time: ")
                val time = it.localTime
                val timeString = formatter.format(time)
                printWriter.append(timeString)
                printWriter.newLine()
                val params = it.properties
                if (params.isNotEmpty()) {
                    if (selectedFormat == FormatYaml) {
                        Helpers.convertToYaml(params, yamlWriter)
                    } else {
                        gson.toJson(params, printWriter)
                    }
                }
            }
        }
    }

}

private fun PrintWriter.newLine() = append(System.lineSeparator())