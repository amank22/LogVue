package storage

import models.LogItem
import models.SessionInfo
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import storage.serializer.ObjectSerializer

object Db {

    private const val PREFIX = "Session-"

    private val diskDb by lazy {
        StorageHelper.createDiskDb()
    }

    private var session: HTreeMap<String, LogItem>? = null
    private var sessionId: String? = null
    private val LOCK = Any()

    val configs by lazy {
        diskDb.hashMap("configs", Serializer.STRING, Serializer.STRING).createOrOpen()
    }

    private val sessionInfoMap by lazy {
        diskDb.hashMap("sessionInfo", Serializer.STRING, ObjectSerializer<SessionInfo>()).createOrOpen()
    }

    init {
        if (!areNoSessionsCreated()) {
            getOrCreateSession(getPreviousSessionNumber())
        }
    }

    fun getSessionInfo(sessionId: String): SessionInfo? {
        return sessionInfoMap[sessionId]
    }

    fun updateSessionInfo(sessionId: String, sessionInfo: SessionInfo) {
        sessionInfoMap[sessionId] = sessionInfo
    }

    fun getAllSessions() = diskDb.getAllNames().filter { it.startsWith(PREFIX) }.sortedBy { getSessionNumber(it) }

    private fun getLastSessionNumber(): Int {
        val lastSessionId = getAllSessions().lastOrNull() ?: sessionIdFromNumber(0)
        return getSessionNumber(lastSessionId)
    }

    private fun getPreviousSessionNumber(): Int {
        val lastDbSessionId = configs["lastSessionId"]
        val lastSessionId = if (lastDbSessionId.isNullOrBlank() || !lastDbSessionId.startsWith(PREFIX)) {
            getAllSessions().lastOrNull() ?: sessionIdFromNumber(0)
        } else {
            lastDbSessionId
        }
        return getSessionNumber(lastSessionId)
    }

    private fun getSessionNumber(sessionId: String) = sessionId.split("-").lastOrNull()?.toIntOrNull() ?: 0

    private fun areNoSessionsCreated() = getAllSessions().isEmpty()

    private fun isThisTheOnlySession(sessionId: String): Boolean {
        val sessions = getAllSessions()
        if (sessions.size != 1) return false
        return sessions.first() == sessionId
    }

    fun createNewSession(sessionInfo: SessionInfo) {
        val sessionNumber = getLastSessionNumber()
        val sessionIdFromNumber = sessionIdFromNumber(sessionNumber + 1)
        changeSession(sessionIdFromNumber)
        sessionInfoMap[sessionIdFromNumber] = sessionInfo
    }

    fun deleteSession(sessionId: String) {
        if (sessionId == sessionId() && !isThisTheOnlySession(sessionId)) {
            val sessionNumber = getLastSessionNumber()
            changeSession(sessionIdFromNumber(sessionNumber + 1))
        } else if (sessionId == sessionId()) {
            changeSession(null)
        }
        val oldSession = diskDb.hashMap(sessionId, Serializer.STRING, ObjectSerializer<LogItem>())
            .open()
        oldSession.clear()
        sessionInfoMap.remove(sessionId)
        val recIds = arrayListOf<Long>()
        diskDb.nameCatalogParamsFor(sessionId).forEach { (t, u) ->
            if (t.endsWith("rootRecids")) {
                u.split(",").forEach { value ->
                    val recId = value.trim().toLongOrNull()
                    recId?.let { recIds.add(it) }
                }
                return@forEach
            }
        }
        recIds.forEach {
            diskDb.getStore().delete(it, Serializer.STRING)
        }
        val newCatalog = diskDb.nameCatalogLoad()
        val keys = newCatalog.keys.filter { it.startsWith(sessionId) }
        keys.forEach {
            newCatalog.remove(it)
        }
        diskDb.nameCatalogSave(newCatalog)
        diskDb.commit()
    }

    fun changeSession(sessionId: String?) {
        if (sessionId == null) {
            getOrCreateSession(null)
            return
        }
        val number = getSessionNumber(sessionId)
        getOrCreateSession(number)
    }

    private fun getOrCreateSession(sessionNumber: Int?) {
        synchronized(LOCK) {
            if (sessionNumber == null) {
                this.sessionId = null
                configs.remove("lastSessionId")
                this.session = null
                return
            }
            if (sessionNumber < 1) throw Exception("Session number must be greater than 1")
            val sessionId = sessionIdFromNumber(sessionNumber)
            val session = diskDb
                .hashMap(sessionId, Serializer.STRING, ObjectSerializer<LogItem>())
                .createOrOpen()
            this.sessionId = sessionId
            configs["lastSessionId"] = sessionId
            this.session = session
        }
    }

    private fun sessionIdFromNumber(sessionNumber: Int) = "$PREFIX$sessionNumber"

    fun currentSession() = synchronized(LOCK) {
        session
    }

    fun sessionId() = synchronized(LOCK) {
        sessionId
    }

    fun close() {
        diskDb.close()
    }
}
