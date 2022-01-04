package processor

import models.LogItem
import models.SourceInternalContent
import storage.Db

object DbSink {

    fun save(value: LogItem?) {
        val currentSession = Db.currentSession()
        if (value == null || value.source is SourceInternalContent || currentSession == null) return
        currentSession[value.key()] = value
    }

    fun saveAll(list: List<LogItem>) {
        val map = hashMapOf<String, LogItem?>()
        list.forEach {
            map[it.key()] = it
        }
        saveAll(map)
    }

    fun saveAll(value: Map<String, LogItem?>) {
        val filteredMap = value.filterValues { it != null && it.source !is SourceInternalContent }
        val currentSession = Db.currentSession() ?: return
        currentSession.putAll(filteredMap)
    }
}
