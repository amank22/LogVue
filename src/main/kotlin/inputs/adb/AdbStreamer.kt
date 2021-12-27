package inputs.adb

import kotlinx.coroutines.runBlocking
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction

class AdbStreamer(packageName: String) : RichParallelSourceFunction<Result<String>>() {

    private val command = LoggerCommand(packageName)

    override fun run(ctx: SourceFunction.SourceContext<Result<String>>) {
        runBlocking {
            val process = command.stream(this)
            for (value in process) {
                value.fold({
                    if (it.isNotBlank()) {
                        ctx.collect(Result.success(it))
                    }
                }, {
                    ctx.collect(Result.failure(it))
                })
            }
        }
    }

    override fun cancel() {
        command.close()
    }

    override fun close() {
        super.close()
        command.close()
    }

}