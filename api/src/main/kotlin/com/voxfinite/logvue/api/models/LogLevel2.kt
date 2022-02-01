package com.voxfinite.logvue.api.models

/**
 * Log Level enum.
 */
enum class LogLevel2(
    /**
     * Returns the numerical value of the priority.
     */
    //$NON-NLS-1$
    val priority: Int,
    /**
     * Returns a non translated string representing the LogLevel.
     */
    val stringValue: String,
    /**
     * Returns the letter identifying the priority of the [LogLevel2].
     */
    val priorityLetter: Char
) {
    VERBOSE(2, "verbose", 'V'),  //$NON-NLS-1$
    DEBUG(3, "debug", 'D'),  //$NON-NLS-1$
    INFO(4, "info", 'I'),  //$NON-NLS-1$
    WARN(5, "warn", 'W'),  //$NON-NLS-1$
    ERROR(6, "error", 'E'),  //$NON-NLS-1$
    ASSERT(7, "assert", 'A');

    companion object {
        fun getByString(value: String): LogLevel2? {
            for (mode in values()) {
                if (mode.stringValue == value) {
                    return mode
                }
            }
            return null
        }

        /**
         * Returns the [LogLevel2] enum matching the specified letter.
         *
         * @param letter the letter matching a `LogLevel` enum
         * @return a `LogLevel` object or `null` if no match were found.
         */
        fun getByLetter(letter: Char): LogLevel2? {
            for (mode in values()) {
                if (mode.priorityLetter == letter) {
                    return mode
                }
            }
            return null
        }

        /**
         * Returns the [LogLevel2] enum matching the specified letter.
         *
         *
         * The letter is passed as a [String] argument, but only the first character
         * is used.
         *
         * @param letter the letter matching a `LogLevel` enum
         * @return a `LogLevel` object or `null` if no match were found.
         */
        fun getByLetterString(letter: String): LogLevel2? {
            return if (letter.isNotEmpty()) {
                getByLetter(letter[0])
            } else null
        }
    }
}
