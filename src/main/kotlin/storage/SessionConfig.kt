package storage

import models.SessionInfo

/**
 * Operations for config related to session.
 * These are stored in sessionInfo so that it is tied to session and will be deleted along with it.
 */
object SessionConfig {

    private val currentSessionId
        get() = Db.sessionId()

    fun string(key: String, sessionId: String? = currentSessionId): String? = getConfig(key, sessionId)

    fun boolDefaultOn(key: String, sessionId: String? = currentSessionId): Boolean {
        return getConfig(key, sessionId)?.toBooleanStrictOrNull() ?: true
    }

    fun bool(key: String, sessionId: String? = currentSessionId, default: Boolean = false): Boolean {
        return getConfig(key, sessionId)?.toBooleanStrictOrNull() ?: default
    }

    fun int(key: String, sessionId: String? = currentSessionId): Long? {
        return getConfig(key, sessionId)?.toLongOrNull()
    }

    fun double(key: String, sessionId: String? = currentSessionId): Double? {
        return getConfig(key, sessionId)?.toDoubleOrNull()
    }

    /**
     * Updates config in db and returns updated [SessionInfo]
     * If [sessionId] is null or blank or there is no config in db, this is a no-op and return null.
     * If value is null, we will remove that key from db or put.
     * This function creates a copy of session info and updates it
     */
    fun set(key: String, value: Any?, sessionId: String? = currentSessionId): SessionInfo? {
        if (sessionId.isNullOrBlank()) return null
        val sInfo = Db.getSessionInfo(sessionId) ?: return null
        val sInfoConfig = HashMap(sInfo.configs ?: emptyMap())
        if (value == null) {
            sInfoConfig.remove(key)
        } else {
            sInfoConfig[key] = value.toString()
        }
        val copy = sInfo.copy(configs = sInfoConfig)
        Db.updateSessionInfo(sessionId, copy)
        return copy
    }

    private fun getConfig(key: String, sessionId: String?): String? {
        if (sessionId.isNullOrBlank()) return null
        return Db.getSessionInfo(sessionId)?.configs?.get(key)
    }
}
