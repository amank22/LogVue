package inputs.adb

import kotlinx.coroutines.runBlocking
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction

class AdbStreamerAdam(packageName: String) : RichParallelSourceFunction<Result<String>>() {

    private val command = LoggerCommandAdam(packageName)

    override fun run(ctx: SourceFunction.SourceContext<Result<String>>) {
        runBlocking {
            val result = command.init(this)
            result.fold({
                command.stream {
                    if (!it.isNullOrBlank()) {
                        ctx.collect(Result.success(it))
                    }
                }
            }, {
                ctx.collect(Result.failure(it))
            })
        }
    }

    override fun cancel() {
        command.close()
    }

}