package processor

import inputs.adb.AndroidLogStreamer
import inputs.adb.LogCatErrors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.LogItem
import models.SessionInfo
import storage.Db
import utils.Helpers
import utils.Log
import utils.failureOrNull
import utils.getOrNull

class MainProcessor {

    private val streamer = AndroidLogStreamer()
    private val paramFilter = ParamFilter()

    suspend fun getSessions() = withContext(Dispatchers.IO) {
        Db.getAllSessions().asReversed()
    }

    fun getSessionInfo(sessionId: String) = Db.getSessionInfo(sessionId)

    fun startOldSession(session: String) {
        pause()
        Db.changeSession(session)
    }

    fun createNewSession(sessionInfo: SessionInfo) {
        pause()
        Db.createNewSession(sessionInfo)
    }

    fun deleteSession(sessionId: String) {
        if (sessionId == getCurrentSessionId()) {
            pause()
        }
        Db.deleteSession(sessionId)
    }

    fun getCurrentSessionId() = Db.sessionId()

    suspend fun fetchOldStream(onMessage: (msg: List<LogItem>) -> Unit) = withContext(Dispatchers.IO) {
        val lastItems = Db.currentSession()?.filter {
            val value = it.value
            Db.parameterSet.addAll(value.properties.keys)
            paramFilter.filter(value)
        }?.map { it.value }?.sortedBy { it.localTime }
        if (!lastItems.isNullOrEmpty()) {
            uiFlowSink(flowOf(lastItems), onMessage)
        } else {
            onMessage(listOf(LogItem.NoContent))
        }
    }

    suspend fun observeNewStream(
        onError: (logError: LogCatErrors) -> Unit,
        onMessage: (msg: List<LogItem>) -> Unit
    ) = withContext(Dispatchers.IO) {
        val packageName = "com.goibibo.debug"
        val stream = streamer.stream(packageName)
        launch {
            val successStream = stream.filter { it.isSuccess }.map { it.getOrNull() }
                .filterNotNull()
                .mapNotNull { logCatMessage2s ->
                    logCatMessage2s.filter {
                        Helpers.validateFALogString(it.message) && it.header.logLevel != com.android.ddmlib.Log.LogLevel.ERROR
                    }.map {
                        Helpers.parseFALogs(it)
                    }
                }.buffer()
            launch {
                uiFlowSink(successStream, onMessage)
            }
            launch {
                successStream.collect { list ->
                    DbSink.saveAll(list)
                }
            }
        }
        launch {
            stream.filter { it.isFailure }
                .map { it.failureOrNull() }
                .filterNotNull()
                .collect { onError(it) }
        }
    }

    private suspend fun uiFlowSink(logItemStream: Flow<List<LogItem>>, onMessage: (msg: List<LogItem>) -> Unit) {
        logItemStream
            .map { list ->
                list.filter { paramFilter.filter(it) }
            }.collect { onMessage(it) }
    }

    fun pause() {
        try {
            streamer.stop()
        } catch (e: Exception) {
            Log.d("unnecessary", "keeping exception for now in pause")
        }
    }

}