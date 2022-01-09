package processor

import com.android.ddmlib.Log
import inputs.adb.AndroidLogStreamer
import inputs.adb.LogCatErrors
import inputs.adb.LogErrorNoSession
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.ErrorContent
import models.LogItem
import models.SessionInfo
import storage.Db
import utils.Helpers
import utils.failureOrNull
import utils.getOrNull
import utils.reportException


class MainProcessor {

    private val streamer = AndroidLogStreamer()
    private var filterQuery: String? = null

    suspend fun getSessions() = withContext(Dispatchers.IO) {
        Db.getAllSessions().asReversed()
    }

    fun getSessionInfo(sessionId: String) = Db.getSessionInfo(sessionId)

    fun isSameSession(sessionId: String) = sessionId == getCurrentSessionId()

    fun startOldSession(session: String) {
        pause()
        filterQuery = null
        Db.changeSession(session)
    }

    fun createNewSession(sessionInfo: SessionInfo) {
        pause()
        filterQuery = null
        Db.createNewSession(sessionInfo)
    }

    fun deleteSession(sessionId: String) {
        if (sessionId == getCurrentSessionId()) {
            pause()
            filterQuery = null
        }
        Db.deleteSession(sessionId)
    }

    fun getCurrentSessionId() = Db.sessionId()

    suspend fun fetchOldStream(filterQuery: String? = null, onMessage: (msg: List<LogItem>) -> Unit) =
        withContext(Dispatchers.IO) {
            this@MainProcessor.filterQuery = filterQuery
            val lastItems = Db.currentSession()
                ?.map { it.value }
            if (!lastItems.isNullOrEmpty()) {
                uiFlowSink(flowOf(lastItems), false, onMessage)
            } else {
                onMessage(listOf(LogItem.noContent("Record logs using the start button above")))
            }
        }

    suspend fun observeNewStream(
        onError: (logError: LogCatErrors) -> Unit,
        onMessage: (msg: List<LogItem>) -> Unit
    ) = withContext(Dispatchers.IO) {
        val sessionId = getCurrentSessionId()
        if (sessionId == null) {
            onError(LogErrorNoSession)
            return@withContext
        }
        val sessionInfo = getSessionInfo(sessionId)
        if (sessionInfo == null) {
            onError(LogErrorNoSession)
            return@withContext
        }
        val packageName = sessionInfo.appPackage
        val stream = streamer.stream(packageName)
        launch {
            val successStream = stream.filter { it.isSuccess }.map { it.getOrNull() }
                .filterNotNull()
                .mapNotNull { logCatMessage2s ->
                    logCatMessage2s.filter {
                        Helpers.validateFALogString(it.message) &&
                                it.header.logLevel != Log.LogLevel.ERROR
                    }.map {
                        Helpers.parseFALogs(it)
                    }
                }.buffer()
            launch {
                uiFlowSink(successStream, true, onMessage)
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

    private suspend fun uiFlowSink(
        logItemStream: Flow<List<LogItem>>,
        isNewStream: Boolean,
        onMessage: (msg: List<LogItem>) -> Unit
    ) {
        val indexedCollection by lazy(LazyThreadSafetyMode.NONE) { queryCollection() }
        val parser by lazy(LazyThreadSafetyMode.NONE) { sqlParser() }
        val fQuery = filterQuery?.trim()
        logItemStream.collect { list ->

            val filterResult = if (fQuery.isNullOrBlank() || fQuery == QUERY_PREFIX) {
                registerPropertiesInParser(list, parser, indexedCollection)
                list.sortedBy { it.localTime }
            } else {
                if (!isNewStream) {
                    indexedCollection.clear()
                }
                val sentryTransaction = Sentry.startTransaction("filterLogs", "filter", true)
                sentryTransaction.setData("query", filterQuery ?: "")
                try {
                    filterLogs(indexedCollection, list, parser, fQuery)
                } catch (e: Exception) {
                    e.reportException()
                    sentryTransaction.throwable = e
                    sentryTransaction.status = SpanStatus.INTERNAL_ERROR
                    listOf(LogItem.errorContent(ErrorContent("Unable to filter current query", e)))
                } finally {
                    sentryTransaction.finish()
                }
            }
            if (filterResult.isEmpty() && !isNewStream) {
                onMessage(listOf(LogItem.noContent("No results found for this query")))
            } else {
                onMessage(filterResult)
            }
        }
    }

    fun pause() {
        streamer.stop()
    }
}
