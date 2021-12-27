package inputs.adb

object UnicodeReader {
    private const val CarriageReturn = 0xD
    private const val LineFeed = 0xA

    fun getLineBreak(startIndex: Int, unicodeBytes: ByteArray): Int {
        val len: Int = unicodeBytes.size
        var pos = startIndex
        while (pos < len - 1) {
            if (unicodeBytes[pos].toInt() == CarriageReturn && unicodeBytes[pos + 2].toInt() == LineFeed && unicodeBytes[pos + 1].toInt() == 0 && unicodeBytes[pos + 3].toInt() == 0) return pos
            pos += 2
        }
        return -1
    }

    fun getAllLines(unicodeBytes: ByteArray, onNewLine: (str: String) -> Unit) {
        var pos = 0
        var lastPos = 0
        while (pos > -1) {
            pos = getLineBreak(pos, unicodeBytes)
            onNewLine(String(unicodeBytes, lastPos, pos - lastPos))
            lastPos = pos
            pos += 4
        }
        if (lastPos < unicodeBytes.size - 2) {
            onNewLine(String(unicodeBytes, lastPos, unicodeBytes.size - lastPos))
        }
    }

}