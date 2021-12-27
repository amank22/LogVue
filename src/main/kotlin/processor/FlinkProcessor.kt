package processor

import inputs.adb.AdbStreamer
import inputs.adb.LogCatErrors
import inputs.adb.LogErrorUnknown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import models.LogItem
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import storage.Db
import utils.Helpers
import utils.Log

class FlinkProcessor(packageName: String) {

    private val streamer = AdbStreamer(packageName)

    private val env by lazy {
        val e = StreamExecutionEnvironment.getExecutionEnvironment()
        e.parallelism = 1
        e
    }

    suspend fun getSessions() = withContext(Dispatchers.IO) {
        Db.getAllSessions().asReversed()
    }

    fun startOldSession(session: String) {
        pause()
        Db.changeSession(session)
    }

    fun createNewSession() {
        pause()
        Db.createNewSession()
    }

    fun deleteSession(sessionId: String) {
        if (sessionId == getCurrentSessionId()) {
            pause()
        }
        Db.deleteSession(sessionId)
    }

    fun getCurrentSessionId() = Db.sessionId()

    suspend fun fetchOldStream(onMessage: (msg: LogItem) -> Unit) = withContext(Dispatchers.IO) {
        val filterFunction = ParamFilterFunction()
        val lastItems = Db.currentSession().filter {
            val value = it.value
            Db.parameterSet.addAll(value.properties.keys)
            filterFunction.filter(value)
        }.map { it.value }.sortedBy { it.localTime }
        val oldStream = if (lastItems.isNotEmpty()) {
            env.fromCollection(lastItems)
        } else {
            null
        }
        if (oldStream != null) {
            uiSink(oldStream, onMessage)
        } else {
            onMessage(LogItem.NoContent)
        }
    }

    suspend fun observeNewStream(
        onError: (logError: LogCatErrors) -> Unit,
        onMessage: (msg: LogItem) -> Unit
    ) = withContext(Dispatchers.IO) {
        val source = env.addSource(streamer)
        val newStream = source
            .filter { it.isSuccess }
            .map { it.getOrNull() ?: "" }
            .returns(String::class.java)
            .filter { Helpers.validateFALogString(it) }
//            .map { Helpers.cutLogString(it) }
            .returns(String::class.java)
            .map { Helpers.parseFALogs(it) }
            .returns(LogItem::class.java)
        newStream.addSink(DbSinkFunction())
        uiSink(newStream, onMessage)

        // Error Handling
        source
            .filter { it.isFailure }
            .map { (it.exceptionOrNull() as? LogCatErrors) ?: LogErrorUnknown() }
            .returns(LogCatErrors::class.java)
            .executeAndCollect()
            .forEachRemaining { onError(it) }
    }

    private fun uiSink(logItemStream: SingleOutputStreamOperator<LogItem>, onMessage: (msg: LogItem) -> Unit) {
        logItemStream
            .map {
                val pString = Helpers.createAnnotatedString(it.properties)
                it.propertiesAString = pString
                it
            }
            .returns(LogItem::class.java)
            .filter(ParamFilterFunction())
            .executeAndCollect().forEachRemaining(onMessage)
    }

    fun pause() {
        try {
            streamer.close()
        } catch (e: Exception) {
            Log.d("unnecessary", "keeping exception for now in pause")
        }
    }

}