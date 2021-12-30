package inputs.adb.ddmlib

import com.android.ddmlib.MultiLineReceiver

class PackagesReceiver(val onValue: (packages: List<String>) -> Unit) : MultiLineReceiver() {
    var isResultPending = false

    override fun isCancelled(): Boolean = isResultPending

    override fun processNewLines(lines: Array<out String>?) {
        if (!lines.isNullOrEmpty()) {
            val packages = lines.mapNotNull { it.split(Regex(":"), 2).lastOrNull() }.toList()
            onValue(packages)
        }
        isResultPending = true
    }
}