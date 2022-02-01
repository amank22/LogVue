package com.voxfinite.logvue.processor

import com.voxfinite.logvue.api.models.LogLevel2
import com.voxfinite.logvue.adb.AndroidLogStreamer
import com.voxfinite.logvue.adb.LogCatErrors
import com.voxfinite.logvue.adb.LogErrorNoSession
import com.voxfinite.logvue.api.LogEventParser
import io.sentry.Sentry
import io.sentry.SpanStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.voxfinite.logvue.api.models.ErrorContent
import com.voxfinite.logvue.api.models.LogCatMessage2
import com.voxfinite.logvue.api.models.LogItem
import com.voxfinite.logvue.models.SessionInfo
import com.voxfinite.logvue.storage.Db
import com.voxfinite.logvue.utils.*

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
        EventTypePredictor.clear()
    }

    fun createNewSession(sessionInfo: SessionInfo) {
        pause()
        filterQuery = null
        Db.createNewSession(sessionInfo)
        EventTypePredictor.clear()
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
                onMessage(listOf(EventCompanion.noContent("Record logs using the start button above")))
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
        val parsers = PluginsHelper.parsers()
        launch {
            val successStream = stream.filter { it.isSuccess }.map { it.getOrNull() }
                .filterNotNull()
                .mapNotNull { logCatMessage2s ->
                    generateLogItems(logCatMessage2s, parsers)
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

    private fun generateLogItems(
        logCatMessage2s: ArrayList<LogCatMessage2>,
        parsers: MutableList<LogEventParser>
    ): ArrayList<LogItem> {
        val localList = arrayListOf<LogItem>()
        logCatMessage2s.forEach { message ->
            parsers.forEach inner@ { parser ->
                val filters = parser.filters()
                val mFilter = message.header.tag
                if (!filters.contains(mFilter) || !parser.validate(message)) {
                    return@inner
                }
                try {
                    localList.add(parser.parse(message))
                } catch (e: Exception) {
                    e.reportException()
                }
            }
        }
        return localList
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
                    val fl = filterLogs(indexedCollection, list, parser, fQuery)
                    sentryTransaction.status = SpanStatus.OK
                    fl
                } catch (e: Exception) {
                    e.reportException()
                    sentryTransaction.throwable = e
                    sentryTransaction.status = SpanStatus.INTERNAL_ERROR
                    listOf(EventCompanion.errorContent(ErrorContent("Unable to filter current query", e)))
                } finally {
                    sentryTransaction.finish()
                }
            }
            if (filterResult.isEmpty() && !isNewStream) {
                onMessage(listOf(EventCompanion.noContent("No results found for this query")))
            } else {
                onMessage(filterResult)
            }
        }
    }

    fun pause() {
        streamer.stop()
    }
}
