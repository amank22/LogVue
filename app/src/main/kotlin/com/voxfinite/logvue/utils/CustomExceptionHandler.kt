package com.voxfinite.logvue.utils

class CustomExceptionHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.printStackTrace()
        setCrashed()
    }

    companion object {
        private const val DB_KEY = "lastTimeException"

        private fun setCrashed() {
            AppSettings.setFlag(DB_KEY, true)
        }

        fun setLastCrashConsumed() {
            AppSettings.setFlag(DB_KEY, false)
        }

        fun isLastTimeCrashed() = AppSettings.getFlag(DB_KEY)
    }
}
