package utils

import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.drapostolos.typeparser.GenericType
import com.github.drapostolos.typeparser.TypeParser
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import models.*
import org.snakeyaml.engine.v2.api.Dump
import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import processor.YamlWriter
import storage.Db
import java.awt.Desktop
import java.io.PrintWriter
import java.net.URI
import java.nio.file.Path
import java.util.*
import kotlin.io.path.absolutePathString

object Helpers {

    private val objectMapper by lazy {
        ItemObjectMapper()
    }

    private const val faPrefix = "Passing event to registered event handler (FE): "

    private val parser = TypeParser.newBuilder().build()

    private val settings by lazy {
        DumpSettings.builder().setDefaultScalarStyle(ScalarStyle.PLAIN)
            .setBestLineBreak(System.lineSeparator())
            .setMultiLineFlow(true)
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setExplicitStart(false)
            .build()
    }

    private val gson by lazy {
        val gsonBuilder = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        gsonBuilder.setPrettyPrinting()
        gsonBuilder.create()
    }

    val isThemeLightMode = MutableStateFlow(Db.configs["isThemeLightMode"]?.toBooleanStrictOrNull() ?: true)

    fun switchThemes(isLightMode: Boolean) {
        isThemeLightMode.value = isLightMode
        Db.configs["isThemeLightMode"] = isLightMode.toString()
    }

    fun validateFALogString(rawText: String): Boolean {
        if (rawText.isBlank()) return false
        if (!rawText.startsWith(faPrefix)) return false
        return true
    }

    /*
    * Sample: Passing event to registered event handler (FE): lumos_home, Bundle[{analytics={request_id=a85e6056-448b-4bb3-beaf-14c2550d7499}, templateName=vaccination, screenName=home_notloggedin, utm_campaign=GI_VACCINATION_V2_LOW_B2C_IN_DEF, cardName=GI_VACCINATION_V2_LOW_B2C_IN_DEF, ga_screen_class(_sc)=HomeActivity, ga_screen_id(_si)=5665805600968775538, home=skywalker_v1, type=cardViewed, request_id=a85e6056-448b-4bb3-beaf-14c2550d7499}]
    */
    fun parseFALogs(msg: LogCatMessage2): LogItem {
        val rawText = msg.message
        val cut1 = rawText.removePrefix(faPrefix)
        val eventParamsCutter = cut1.split(Regex(","), 2)
        val eventName = eventParamsCutter[0].trim()
        val properties = hashMapOf<String, Any>()
        eventParamsCutter.getOrNull(1)?.trim()?.let {
            val objectItem = Item.ObjectItem(it.trim())
            val something = objectMapper.parse(objectItem) as HashMap<out String, Any>
            properties.putAll(something)
        }
        val time = msg.header.timestamp.toEpochMilli()
        return LogItem(
            source = SourceFA, eventName = eventName,
            properties = hashMapEntityOf(properties), localTime = time
        )
    }

    fun tryParseToType(str: String?): Any? {
        return try {
            tryParseInternal(str)
        } catch (ve: ValueException) {
            ve.value
        }
    }

    @Throws(ValueException::class)
    private fun tryParseInternal(str: String?): Any? {
        if (str == null) return null
        if (str.isBlank()) return str
        var parsed: Boolean
        parsed = tryParseType(str, Boolean::class.java)
        if (!parsed) {
            parsed = tryParseType(str, Long::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Int::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Double::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Float::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, Double::class.java)
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Boolean>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Int>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Long>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Float>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Array<Double>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<List<Double>>() {})
        }
        if (!parsed) {
            parsed = tryParseType(str, object : GenericType<Set<Double>>() {})
        }
        return str
    }

    @Throws(ValueException::class)
    private fun <T> tryParseType(str: String, clazz: Class<T>): Boolean {
        return tryOp {
            val value = parser.parse(str, clazz)
            throw ValueException(value) // anti-pattern ki ****
        }
    }

    @Throws(ValueException::class)
    private fun <T> tryParseType(str: String, genericType: GenericType<T>): Boolean {
        return tryOp {
            val value = parser.parse(str, genericType)
            throw ValueException(value)
        }
    }

    private fun tryOp(op: () -> Unit): Boolean {
        return try {
            op()
            true
        } catch (e: ValueException) {
            throw e
        } catch (e: Exception) {
            false
        }
    }

    fun convertToYamlString(properties: Map<String, Any>): String? {
        return try {
            val dump = Dump(settings)
            dump.dumpToString(properties)
        } catch (e: Exception) {
            e.reportException()
            null
        }
    }

    fun convertYamlToAnnotated(yaml: String): AnnotatedString {
        return buildAnnotatedString {
            withStyle(ParagraphStyle(lineHeight = 50.sp)) {
                yaml.lineSequence().forEach { line ->
                    val lineSplit = line.split(":", ignoreCase = false, limit = 2)
                    if (lineSplit.size < 2) return@forEach
                    lineSplit.firstOrNull()?.let { f ->
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(f)
                        }
                    }
                    append(":")
                    for (i in 1 until lineSplit.size) {
                        append(lineSplit[i])
                    }
                    append(System.lineSeparator())
                }
            }
        }
    }

    fun convertToYaml(properties: Map<String, Any>, printWriter: PrintWriter) {
        try {
            val dump = Dump(settings)
            dump.dump(properties, YamlWriter(printWriter))
        } catch (e: Exception) {
            e.reportException()
        }
    }

    fun convertToYaml(properties: Map<String, Any>, streamDataWriter: StreamDataWriter) {
        try {
            val dump = Dump(settings)
            dump.dump(properties, streamDataWriter)
        } catch (e: Exception) {
            e.reportException()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun createJsonString(properties: Map<String, Any>): String {
        return gson.toJson(properties)
    }

    /**
     * Get string for any object with a [maxLength].
     * It will add ellipsize dots (...) at the end to clip length to maxLength if [addEllipsize] is true else
     * it will just clip the text.
     * Example :
     * value = false
     * maxLength = 4
     * f...
     * value = true
     * maxLength = 4
     * true
     *
     * @param value Any object
     * @param maxLength max length of returned string. (...) added will make string like (maxLength - 3 + ...).
     * Must be greater than 3 if [addEllipsize] is true
     * @param addEllipsize whether to add ellipsize (...) or just clip to maxLength
     * @return clipped or full string or empty string if [value] is null
     */
    fun valueShortText(value: Any?, maxLength: Int = 20, addEllipsize: Boolean = true): String {
        if (value == null) return ""
        if (addEllipsize) {
            require(maxLength > 3)
        }
        val valueStr = value.toString().trim()
        val takeValue = if (valueStr.length <= maxLength) {
            valueStr
        } else {
            if (addEllipsize) {
                valueStr.take(maxLength - 3) + "..."
            } else {
                valueStr.take(maxLength)
            }
        }
        return takeValue
    }

    fun isWindows(): Boolean {
        val os = System.getProperty("os.name").lowercase(Locale.getDefault())
        // windows
        return os.indexOf("win") >= 0
    }

    fun openFileExplorer(path: Path) {
        try {
            val pathString = path.absolutePathString()
            val command = if (isWindows()) {
                "Explorer.exe $pathString"
            } else {
                "open $pathString"
            }
            Runtime.getRuntime().exec(command)
        } catch (e: Exception) {
            e.reportException()
        }
    }

    fun openInBrowser(url: String) {
        openInBrowser(URI.create(url))
    }

    fun openInBrowser(uri: URI) {
        val osName by lazy(LazyThreadSafetyMode.NONE) { System.getProperty("os.name").lowercase(Locale.getDefault()) }
        val desktop = Desktop.getDesktop()
        when {
            Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) -> desktop.browse(uri)
            "mac" in osName -> Runtime.getRuntime().exec("open $uri")
            "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
            else -> throw IllegalArgumentException("cannot open $uri")
        }
    }

    /**
     * This tries to predict the event type but there is no guarantee of very high accuracy.
     * If shown to user, specify that this is just mere a possibility
     * @return event type or not sure if nothing can be predicted
     */
    fun predictEventType(logItem: LogItem): PredictedEventType {
        with(logItem) {
            predictionEventNameMap.forEach loop@{ (mKey, mValue) ->
                if (eventName.contains(mKey, true)) {
                    return mValue
                }
            }
            return findTypeInEventValue() ?: EventTypeNotSure
        }
    }

    private fun LogItem.findTypeInEventValue(): PredictedEventType? {
        properties.forEach { (_, u) ->
            if (u !is String) return@forEach
            val value = u.toString()
            var type: PredictedEventType? = null
            predictionPropertiesMap.forEach loop@{ (mKey, mValue) ->
                if (value.contains(mKey, true)) {
                    type = mValue
                    return@loop
                }
            }
            if (type != null) {
                return type
            }
        }
        return null
    }
}

fun <K, V> hashMapEntityOf(mapToWrap: MutableMap<K, V>): HashMapEntity<K, V> = HashMapEntity(mapToWrap)
