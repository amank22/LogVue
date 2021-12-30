package models

import java.io.Serializable

data class SessionInfo(
    val description: String,
    val appPackage: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
        const val DESC_MAX_LENGTH = 20
    }

    init {
        check(description.length <= DESC_MAX_LENGTH) {
            "Session description should be less than $DESC_MAX_LENGTH characters"
        }
    }
}
