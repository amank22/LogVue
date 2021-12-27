package storage

import models.Filter
import models.ItemSource
import models.LogItem
import models.SourceInternalContent
import org.mapdb.DBMaker
import org.mapdb.HTreeMap
import org.mapdb.Serializer
import storage.serializer.ObjectSerializer

object Db {

    private const val PREFIX = "Session-"

    private val db by lazy {
        DBMaker.fileDB("sessions.db").fileMmapEnableIfSupported().checksumHeaderBypass().make()
    }

    private lateinit var session: HTreeMap<String, LogItem>
    private lateinit var sessionId: String
    private val LOCK = Any()

    private val filters by lazy {
        val filterDb = DBMaker.fileDB("filters").fileMmapEnableIfSupported().checksumHeaderBypass().make()
        filterDb.hashMap("filters", Serializer.STRING, ObjectSerializer<HashSet<Filter>>())
            .counterEnable().createOrOpen()
    }

    val parameterSet by lazy {
        val memoryDb = DBMaker.memoryDB().closeOnJvmShutdown().cleanerHackEnable().make()
        memoryDb.hashSet("allParamSet", Serializer.STRING).createOrOpen()
    }

    val configs by lazy {
        db.hashMap("configs", Serializer.STRING, Serializer.STRING).createOrOpen()
    }

    init {
        if (areNoSessionsCreated()) {
            createNewSession()
        } else {
            getOrCreateSession(getPreviousSessionNumber())
        }
    }

    fun getSessionFilters(): HashSet<Filter> {
        return filters.getOrPut(sessionId()) { hashSetOf() }
    }

    fun addFilterInCurrentSession(filter: Filter) {
        val oldFilters = getSessionFilters()
        oldFilters.add(filter)
        filters[sessionId()] = oldFilters
    }

    fun deleteFilterInCurrentSession(filter: Filter) {
        val oldFilters = getSessionFilters()
        oldFilters.remove(filter)
        filters[sessionId()] = oldFilters
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

    fun areNoSessionsCreated() = getLastSessionNumber() == 0

    fun createNewSession() {
        val sessionNumber = getLastSessionNumber()
        changeSession(sessionIdFromNumber(sessionNumber + 1))
    }

    fun deleteSession(sessionId : String) {
        if (sessionId == sessionId()) {
            val sessionNumber = getLastSessionNumber()
            changeSession(sessionIdFromNumber(sessionNumber + 1))
        }
        val oldSession = db.hashMap(sessionId, Serializer.STRING, ObjectSerializer<LogItem>())
            .open()
        oldSession.clear()
        filters.remove(sessionId)
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
        db.getAllNames().forEach {
            println(it)
        }
        db.commit()
    }

    fun changeSession(sessionId: String): HTreeMap<String, LogItem> {
        val number = getSessionNumber(sessionId)
        parameterSet.clear()
        getOrCreateSession(number)
        return session
    }

    fun getOrCreateSession(sessionNumber: Int) {
        if (sessionNumber < 1) throw Exception("Session number must be greater than 1")
        synchronized(LOCK) {
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