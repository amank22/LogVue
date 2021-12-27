package inputs.adb

import com.malinskiy.adam.request.logcat.LogcatBuffer
import com.malinskiy.adam.request.logcat.LogcatFilterSpec
import com.malinskiy.adam.request.logcat.LogcatReadMode
import com.malinskiy.adam.request.shell.v1.ChanneledShellCommandRequest
import java.time.Instant

class StreamingLogcatRequest(
    since: Instant? = null,
    modes: List<LogcatReadMode> = listOf(LogcatReadMode.long),
    buffers: List<LogcatBuffer> = emptyList(),
    pid: Long? = null,
    lastReboot: Boolean? = null,
    filters: List<LogcatFilterSpec> = emptyList(),
    defaultFilterToSilent: Boolean = true
) : StreamingShellCommandRequest(
    cmd = command(since, modes, buffers, pid, lastReboot, defaultFilterToSilent, filters),
    socketIdleTimeout = Long.MAX_VALUE,
)

private fun command(
    since: Instant?,
    modes: List<LogcatReadMode>,
    buffers: List<LogcatBuffer>,
    pid: Long?,
    lastReboot: Boolean?,
    defaultFilterToSilent: Boolean,
    filters: List<LogcatFilterSpec>
): String {
    val s = "logcat" +
            (since?.let {
                " -T ${since.toEpochMilli()}.0"
            } ?: "") +
            " ${modes.joinToString(separator = " ") { "-v $it" }}" +
            if (buffers.isNotEmpty()) {
                " ${buffers.joinToString(separator = " ") { "-b $it" }}"
            } else {
                ""
            } +
            (pid?.let { " --pid=$it" } ?: "") +
            (lastReboot?.let { " -L" } ?: "") +
            (if (defaultFilterToSilent) " -s" else "") +
            " ${filters.joinToString(separator = " ") { "${it.tag}:${it.level.name}" }}"
                .trimEnd()
    println(s)
    return s
}