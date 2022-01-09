package storage

import models.LogItem
import models.SessionInfo
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import storage.serializer.ObjectSerializer

object Db {

    private const val PREFIX = "Session-"

    private val db by lazy {
        DBMaker.fileDB("sessions.db").fileMmapEnableIfSupported().checksumHeaderBypass().make()
    }

    private var session: HTreeMap<String, LogItem>? = null
    private var sessionId: String? = null
    private val LOCK = Any()

    val configs by lazy {
        db.hashMap("configs", Serializer.STRING, Serializer.STRING).createOrOpen()
    }

    private val sessionInfoMap by lazy {
        db.hashMap("sessionInfo", Serializer.STRING, ObjectSerializer<SessionInfo>()).createOrOpen()
    }

    init {
        if (!areNoSessionsCreated()) {
            getOrCreateSession(getPreviousSessionNumber())
        }
    }

    fun getSessionInfo(sessionId: String): SessionInfo? {
        return sessionInfoMap[sessionId]
    }

    fun getAllSessions() = db.getAllNames().filter { it.startsWith(PREFIX) }.sortedBy { getSessionNumber(it) }

    fun getLastSessionNumber(): Int {
        val lastSessionId = getAllSessions().lastOrNull() ?: sessionIdFromNumber(0)
        return getSessionNumber(lastSessionId)
    }

    fun getPreviousSessionNumber(): Int {
        val lastDbSessionId = configs["lastSessionId"]
        val lastSessionId = if (lastDbSessionId.isNullOrBlank() || !lastDbSessionId.startsWith(PREFIX)) {
            getAllSessions().lastOrNull() ?: sessionIdFromNumber(0)
        } else {
            lastDbSessionId
        }
        return getSessionNumber(lastSessionId)
    }

    fun getSessionNumber(sessionId: String) = sessionId.split("-").lastOrNull()?.toIntOrNull() ?: 0

    fun areNoSessionsCreated() = getAllSessions().isEmpty()

    fun isThisTheOnlySession(sessionId: String): Boolean {
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
        val oldSession = db.hashMap(sessionId, Serializer.STRING, ObjectSerializer<LogItem>())
            .open()
        oldSession.clear()
        sessionInfoMap.remove(sessionId)
        val recIds = arrayListOf<Long>()
        db.nameCatalogParamsFor(sessionId).forEach { (t, u) ->
            if (t.endsWith("rootRecids")) {
                u.split(",").forEach { value ->
                    val recId = value.trim().toLongOrNull()
                    recId?.let { recIds.add(it) }
                }
                return@forEach
            }
        }
        recIds.forEach {
            db.getStore().delete(it, Serializer.STRING)
        }
        val newCatalog = db.nameCatalogLoad()
        val keys = newCatalog.keys.filter { it.startsWith(sessionId) }
        keys.forEach {
            newCatalog.remove(it)
        }
        db.nameCatalogSave(newCatalog)
        db.commit()
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
            val session = db
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
        db.close()
    }
}
